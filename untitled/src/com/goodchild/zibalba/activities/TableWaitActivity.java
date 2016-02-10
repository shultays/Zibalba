package com.goodchild.zibalba.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.goodchild.zibalba.ClientConstants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.ServerThread;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.BooleanPacket;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.TableStatePacket;
import com.goodchild.zibalba.widgets.ZSepiaImageView;
import com.google.gson.Gson;

public class TableWaitActivity extends Activity implements ServerThread.Listener, View.OnClickListener {

    private ServerThread server;
    private ProgressDialog loading;
    private TableModel tableModel;

    private boolean acceptPackets = true;
    private Gson gson = new Gson();
    private boolean isAdmin = false;
    private View startGame;
    private SeekBar timeLimitBar;
    private SeekBar boardSizeBar;
    private SeekBar playerCountBar;
    private TextView timeLimit;
    private TextView boardSize;
    private TextView playerCount;
    private LinearLayout playerList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_wait);
        server = ServerThread.getInstance();
        if(server == null){
            finish();
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }

        startGame = findViewById(R.id.startGame);
        timeLimitBar = (SeekBar) findViewById(R.id.timeLimitBar);
        timeLimit = (TextView) findViewById(R.id.timeLimit);
        boardSizeBar = (SeekBar) findViewById(R.id.boardsizeBar);
        boardSize = (TextView) findViewById(R.id.boardsize);
        playerCountBar = (SeekBar) findViewById(R.id.playerCountBar);
        playerCount = (TextView) findViewById(R.id.playerCount);
        playerList = (LinearLayout) findViewById(R.id.playerList);

        TableModel tableModel = gson.fromJson(getIntent().getStringExtra(ClientConstants.TABLE_MODEL_EXTRA), TableModel.class);


        isAdmin = getIntent().getBooleanExtra(ClientConstants.IS_ADMIN_EXTRA, false);

        updateAdminRights();
        timeLimitBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                timeLimit.setText((30 + i * 15) + " seconds");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendTableUpdateMessage();
            }
        });
        timeLimitBar.setProgress(0);
        timeLimitBar.setProgress(2);
        timeLimitBar.setProgress((tableModel.gameDuration-30)/15);
        boardSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                boardSize.setText((5 + i) + "x" + (5 + i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendTableUpdateMessage();

            }
        });
        boardSizeBar.setProgress(0);
        boardSizeBar.setProgress(1);
        boardSizeBar.setProgress(tableModel.row-5);

        playerCountBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playerCount.setText((2+i)+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendTableUpdateMessage();
            }
        });
        playerCountBar.setProgress(1);
        playerCountBar.setProgress(0);
        playerCountBar.setProgress(tableModel.maxPlayer-2);

    }

    private void sendTableUpdateMessage() {
        TableStatePacket tableStatePacket = new TableStatePacket();
        tableStatePacket.table = new TableModel();
        tableStatePacket.table.column = tableStatePacket.table.row = boardSizeBar.getProgress()+5;
        tableStatePacket.table.gameDuration  = timeLimitBar.getProgress()*15+30;
        tableStatePacket.table.maxPlayer = playerCountBar.getProgress()+2;

        server.sendPacket(Packet.TABLE_STATE_PACKET_ID, tableStatePacket);
    }

    private void updateAdminRights() {
        startGame.setVisibility(isAdmin?View.VISIBLE:View.GONE);
        startGame.setOnClickListener(this);
        timeLimitBar.setEnabled(isAdmin);
        boardSizeBar.setEnabled(isAdmin);
        playerCountBar.setEnabled(isAdmin);

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
    @Override
    public void onBackPressed(){
        server.sendPacket(Packet.LEAVE_TABLE_PACKET_ID, null);

        super.onBackPressed();
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
                if(loading != null && loading.isShowing()) loading.dismiss();
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
            case Packet.ADMIN_NOTIFY_PACKET_ID:
            {
                isAdmin = true;
                updateAdminRights();
            }
            break;
            case Packet.TABLE_STATE_PACKET_ID:
            {
                TableStatePacket tableStatePacket = (TableStatePacket) packet;
                this.tableModel = tableStatePacket.table;
                if(playerList.getChildCount() != tableModel.players.size()){
                    playerList.removeAllViews();
                    for(ClientModel player : tableModel.players){
                        View view = new LinearLayout(this);
                        LayoutInflater.from(this).inflate(R.layout.widget_player, (ViewGroup) view);
                        if(player.picturePath != null && player.picturePath.length() > 0){
                            ((ZSepiaImageView)view.findViewById(R.id.usericon)).setImagePath(player.picturePath);
                        }else{
                            ((ZSepiaImageView)view.findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[player.icon]);
                        }
                        ((TextView)view.findViewById(R.id.username)).setText(player.name);
                        playerList.addView(view);
                    }
                }

                if(!isAdmin){
                    boardSizeBar.setProgress(tableModel.row-5);
                    timeLimitBar.setProgress((tableStatePacket.table.gameDuration-30)/15);
                    playerCountBar.setProgress(tableModel.maxPlayer-2);
                }
            }
            break;
            case Packet.START_GAME_RESULT_PACKET_ID:
            {
                BooleanPacket booleanPacket = (BooleanPacket) packet;
                if(booleanPacket.value){
                    Intent intent = new Intent(this, GameActivity.class);
                    intent.putExtras(getIntent());
                    intent.putExtra(ClientConstants.TABLE_MODEL_EXTRA, gson.toJson(tableModel, TableModel.class));
                    startActivity(intent);
                    finish();
                    acceptPackets = false;
                }else{

                }

                if(loading != null)loading.dismiss();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.startGame){
            server.sendPacket(Packet.START_GAME_PACKET_ID, null);
            loading = ProgressDialog.show(this, "",
                    "Starting Game...", true);
        }
    }
}
