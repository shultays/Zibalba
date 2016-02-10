package com.goodchild.zibalba.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.goodchild.zibalba.ClientConstants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.ServerThread;
import com.goodchild.zibalba.components.Board;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.BooleanPacket;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.TableCommitPacket;
import com.goodchild.zibalba.packets.TableStatePacket;
import com.goodchild.zibalba.widgets.CountDown;
import com.goodchild.zibalba.widgets.ZSepiaImageView;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameActivity extends Activity implements ServerThread.Listener {


    ServerThread server;
    ProgressDialog loading;
    boolean acceptPackets = true;

    Board board;
    private int timeLeft;
    CountDown countDown;
    LinearLayout playerList;
    private TableModel lastTableModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        server = ServerThread.getInstance();
        if(server == null){
            finish();
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }
        playerList = (LinearLayout) findViewById(R.id.playerList);
        countDown = (CountDown) findViewById(R.id.countdown);
        board = (Board) findViewById(R.id.board);
        Gson gson = new Gson();
        TableModel tableModel = gson.fromJson(getIntent().getStringExtra(ClientConstants.TABLE_MODEL_EXTRA), TableModel.class);
        timeLeft = tableModel.gameDuration;
        board.initialize(tableModel);
        board.setServer(server);
        countDown.setNum(timeLeft);
        new CountDownTimer(timeLeft*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft--;
                countDown.setNum(timeLeft);
            }

            public void onFinish() {

            }
        }.start();
        for(int i=0; i<tableModel.players.size(); i++){

            View v = new LinearLayout(this);
            LayoutInflater.from(this).inflate(R.layout.widget_player, (ViewGroup) v);
            if(tableModel.players.get(i).picturePath != null && tableModel.players.get(i).picturePath.length() > 0){
                ((ZSepiaImageView)v.findViewById(R.id.usericon)).setImagePath(tableModel.players.get(i).picturePath);
            }else{
                ((ZSepiaImageView)v.findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[tableModel.players.get(i).icon]);
            }
            ((TextView)v.findViewById(R.id.username)).setText(tableModel.players.get(i).name);
            ((TextView)v.findViewById(R.id.userscore)).setText("0");
            ((TextView)v.findViewById(R.id.userscore)).setVisibility(View.VISIBLE);

            playerList.addView(v);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(server == null){
            finish();
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }
        server.setListener(this);
    }
    @Override
    public void onPause(){
        super.onPause();
        if(server.getListener() == this){
            server.setListener(null);
        }
    }

    @Override
    public void packetReceived() {
        while(acceptPackets){
            ServerThread.Message message = server.messageQueue.peek();
            if(message != null){
                server.messageQueue.remove();
                acceptPacket(message);
            }else{
                break;
            }
        }
    }

    public void acceptPacket(ServerThread.Message message){
        int packetID = message.packetID;
        Packet packet = message.packet;
        switch (packetID){
            case Packet.CONNECTION_ERROR_REPEAT_PACKET_ID:
            {
                finish();
            }
            break;
            case Packet.CONNECTION_ERROR_PACKET_ID:
            {
                if(isFinishing()) return;
                AlertDialog dialog = new AlertDialog(this){
                    @Override
                    public void onStop(){
                        finish();
                    }
                };
                dialog.setTitle("Error");
                dialog.setMessage("Server connection failed");
                dialog.show();
                acceptPackets = false;
            }
            break;
            case Packet.TABLE_COMMIT_PACKET_ID:
            {
                TableCommitPacket tableCommitPacket = (TableCommitPacket) packet;
                board.changeBoard(tableCommitPacket);
                if(tableCommitPacket.players != null && tableCommitPacket.players.size() > 0){
                    updateScores(tableCommitPacket.players);
                }

            }
            break;
            case Packet.FINISH_GAME:
            {

/*
                Bundle param = new Bundle();
                param.putInt("score", 20000);
                Request request = new Request(Session.getActiveSession(), "me/scores", param , HttpMethod.POST);
                request.setCallback(new Request.Callback()
                {
                    @Override
                    public void onCompleted(Response response){
                        System.out.println("!!!"+response.toString());
                    }

                });
                request.executeAsync();*/


                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtras(getIntent());
                intent.putExtra(ClientConstants.TABLE_MODEL_EXTRA, new Gson().toJson(lastTableModel, TableModel.class));
                startActivity(intent);
                finish();
            }
            break;
            case Packet.TABLE_COMMIT_REPLY_PACKET_ID:
            {
                BooleanPacket booleanPacket = (BooleanPacket) message.packet;

                board.popSelected(!booleanPacket.value);
            }
            break;
            case Packet.TABLE_STATE_PACKET_ID:
            {
                TableStatePacket tableStatePacket = (TableStatePacket) packet;
                lastTableModel = tableStatePacket.table;
                if(tableStatePacket.table.players != null && tableStatePacket.table.players.size() > 0){
                    updateScores(tableStatePacket.table.players);
                }
            }
            break;
        }
    }

    private void updateScores(List<ClientModel> players) {
        Collections.sort(players, new Comparator<ClientModel>() {
            @Override
            public int compare(ClientModel clientModel, ClientModel clientModel2) {
                return clientModel2.score-clientModel.score;
            }
        });

        for(int i=0; i<playerList.getChildCount(); i++){
            View v = playerList.getChildAt(i);
            if(i>=players.size()){
                v.setVisibility(View.GONE);
            }else{
                if(players.get(i).picturePath != null && players.get(i).picturePath.length() > 0){
                    ((ZSepiaImageView)v.findViewById(R.id.usericon)).setImagePath(players.get(i).picturePath);
                }else{
                    ((ZSepiaImageView)v.findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[players.get(i).icon]);
                }
                ((TextView)v.findViewById(R.id.username)).setText(players.get(i).name);
                ((TextView)v.findViewById(R.id.userscore)).setText(players.get(i).score+"");
            }
        }
    }

    @Override
    public void onBackPressed(){
        server.sendPacket(Packet.LEAVE_TABLE_PACKET_ID, null);

        super.onBackPressed();
    }
}
