package com.goodchild.zibalba.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.goodchild.zibalba.*;
import com.goodchild.zibalba.components.TableSelectWidget;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.*;
import com.goodchild.zibalba.widgets.ZSepiaImageView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class TableSelectActivity extends Activity implements ServerThread.Listener, View.OnClickListener {
    ListView tableList;
    List<TableModel> tables = new Vector<TableModel>();
    ServerThread server;
    ProgressDialog loading;
    boolean acceptPackets = true;
    ArrayAdapter tableListAdapter;
    TableModel tableModel;
    Gson gson = new Gson();

    String name, picturePath;
    int icon;
    boolean facebookLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_select);
        server = new ServerThread(this, this);

        tableListAdapter = new TableListAdapter(this, R.layout.widget_select_table, tables);
        tableList = (ListView) findViewById(R.id.tableList);
        tableList.setAdapter(tableListAdapter);
        findViewById(R.id.createTable).setOnClickListener(this);

        try {
            WordChecker.init(getAssets().open("words.bin"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        name = getIntent().getStringExtra(ClientConstants.USER_NAME_EXTRA);
        picturePath = getIntent().getStringExtra(ClientConstants.USER_PICTURE_PATH_EXTRA);
        if(picturePath == null) picturePath = "";
        icon = getIntent().getIntExtra(ClientConstants.USER_ICON_EXTRA, 0);
        facebookLogin = getIntent().getBooleanExtra(ClientConstants.IS_FACEBOOK_LOGIN, false);

        if(facebookLogin){
            findViewById(R.id.facebookLogout).setOnClickListener(this);
            findViewById(R.id.editicon).setVisibility(View.GONE);


            Request request = new Request(Session.getActiveSession(), getIntent().getStringExtra(ClientConstants.FACEBOOK_APP_ID)+"/scores", null, HttpMethod.GET);
            request.setCallback(new Request.Callback()
            {
                @Override
                public void onCompleted(Response response)
                {
                    System.out.println("!!!!: " + response.toString());
                }
            });
            request.executeAsync();

        }else{
            findViewById(R.id.facebookLogout).setVisibility(View.GONE);
            findViewById(R.id.editicon).setOnClickListener(this);

            saveUserInfo();
        }
        ((TextView)findViewById(R.id.username)).setText(name);
        if(picturePath.length() > 0){
            ((ZSepiaImageView)findViewById(R.id.usericon)).setImagePath(picturePath);
        }else{
            ((ZSepiaImageView)findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[icon]);
        }

        loading = ProgressDialog.show(TableSelectActivity.this, "",
                "Connecting Server...", true);
        loading.show();
    }

    private void saveUserInfo() {

        SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ClientConstants.USER_NAME_VALUE, name);
        editor.putInt(ClientConstants.USER_ICON_VALUE, icon);
        editor.commit();
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

    int t = 1;
    public void acceptPacket(ServerThread.Message message){
        int packetID = message.packetID;
        Packet packet = message.packet;
        switch (packetID){
            case Packet.CONNECTION_ERROR_REPEAT_PACKET_ID:
            {
                finish();
                Intent intent = new Intent(TableSelectActivity.this, SplashActivity.class);
                startActivity(intent);
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
                        Intent intent = new Intent(TableSelectActivity.this, SplashActivity.class);
                        startActivity(intent);
                    }
                };
                dialog.setTitle("Error");
                dialog.setMessage("Server connection failed");
                dialog.show();
                acceptPackets = false;
            }
            break;
            case Packet.CONNECTION_ERROR_ON_INIT_PACKET_ID:
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
                dialog.setMessage("Couldn't connect to the server");
                dialog.show();
                acceptPackets = false;
            }
            break;

            case Packet.INIT_PACKET_ID:
            {
                InitPacket initPacket = new InitPacket();
                initPacket.model = new ClientModel();
                initPacket.model.name = name;
                initPacket.model.icon = icon;
                initPacket.model.picturePath = picturePath;
                server.sendPacket(Packet.INIT_PACKET_ID, initPacket);
                loading.dismiss();
            }
            break;
            case Packet.SERVER_STATE_PACKET_ID:
            {
                ServerStatePacket serverStatePacket = (ServerStatePacket) packet;

                tables.clear();
                tables.addAll(serverStatePacket.tables);

                tableListAdapter.notifyDataSetChanged();
            }
            break;

            case Packet.JOIN_TABLE_RESULT_PACKET_ID:
            case Packet.CREATE_TABLE_RESULT_PACKET_ID:
            {
                BooleanPacket booleanPacket = (BooleanPacket) packet;
                if(booleanPacket.value){
                    Intent intent = new Intent(this, TableWaitActivity.class);
                    intent.putExtras(getIntent());
                    intent.putExtra(ClientConstants.IS_ADMIN_EXTRA, packetID == Packet.CREATE_TABLE_RESULT_PACKET_ID);
                    intent.putExtra(ClientConstants.TABLE_MODEL_EXTRA, gson.toJson(tableModel, TableModel.class));
                    startActivity(intent);
                    acceptPackets = false;
                }else{
                    AlertDialog dialog = new AlertDialog(this){};
                    dialog.setTitle("Error");
                    dialog.setMessage("Couldn't connect to the table");
                    dialog.show();
                }
                loading.dismiss();
            }
            break;
        }
    }

    int score = 36000;
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.editicon){

            Random r = new Random();
            name =  Tools.getGuestName();
            icon = r.nextInt(ClientConstants.ICON_RESOURCES.length);

            InitPacket initPacket = new InitPacket();
            initPacket.model = new ClientModel();
            initPacket.model.name = name;
            initPacket.model.icon = icon;
            initPacket.model.picturePath = picturePath;
            server.sendPacket(Packet.INIT_PACKET_ID, initPacket);

            ((TextView)findViewById(R.id.username)).setText(name);
            ((ZSepiaImageView)findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[icon]);

            saveUserInfo();
        }else if(view.getId() == R.id.facebookLogout){
            Bundle param = new Bundle();
            param.putInt("score", score);
            score += 100;
            Request request = new Request(Session.getActiveSession(), "me/scores", param , HttpMethod.POST);
            request.setCallback(new Request.Callback()
            {
                @Override
                public void onCompleted(Response response){
                    System.out.println("!!!"+response.toString());
                }

            });
            request.executeAsync();
            /*
            Session session = Session.getActiveSession();
            session.closeAndClearTokenInformation();
            startActivity(new Intent(getApplicationContext(), SplashActivity.class));
            finish();*/
        }else if(view.getId() == R.id.createTable){
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_create_table);
            dialog.show();
            final SeekBar timeLimitBar = (SeekBar) dialog.findViewById(R.id.timeLimitBar);
            final TextView timeLimit = (TextView) dialog.findViewById(R.id.timeLimit);
            timeLimitBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    timeLimit.setText((30+i*15) + " seconds");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            timeLimitBar.setProgress(0);
            timeLimitBar.setProgress(4);

            final SeekBar boardSizeBar = (SeekBar) dialog.findViewById(R.id.boardsizeBar);
            final TextView boardSize = (TextView) dialog.findViewById(R.id.boardsize);
            boardSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    boardSize.setText((5+i)+"x"+(5+i));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            boardSizeBar.setProgress(0);
            boardSizeBar.setProgress(3);

            final SeekBar playerCountBar = (SeekBar) dialog.findViewById(R.id.playerCountBar);
            final TextView playerCount = (TextView) dialog.findViewById(R.id.playerCount);
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

                }
            });
            playerCountBar.setProgress(1);
            playerCountBar.setProgress(4);
            dialog.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TableStatePacket tableStatePacket = new TableStatePacket();
                    tableStatePacket.table = new TableModel();
                    tableStatePacket.table.row = tableStatePacket.table.column = boardSizeBar.getProgress()+5;
                    tableStatePacket.table.gameDuration = 30+timeLimitBar.getProgress()*15;
                    tableStatePacket.table.maxPlayer = playerCountBar.getProgress()+2;
                    tableModel = tableStatePacket.table;
                    server.sendPacket(Packet.CREATE_TABLE_PACKET_ID, tableStatePacket);
                    loading = ProgressDialog.show(TableSelectActivity.this, "",
                            "Creating new table...", true);
                    dialog.dismiss();
                }
            });

        }
    }

    public void onDestroy(){
        super.onDestroy();
        server.closeSocket();
    }
    @Override
    public void onResume(){
        super.onResume();
        if(server == null){
            finish();
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }
        acceptPackets = true;
        server.setListener(this);
    }
    @Override
    public void onPause(){
        super.onPause();
        acceptPackets = false;
        if(server.getListener() == this){
            server.setListener(null);
        }
    }

    private class TableListAdapter extends ArrayAdapter{

        public TableListAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TableSelectWidget table = (TableSelectWidget) view;
            if(table == null){
                table = new TableSelectWidget(viewGroup.getContext());
                table.setOnClickListener(tableClickListener);
            }
            table.setTag(new Integer(i));
            table.setData(tables.get(i));
            return table;
        }
    };

    public View.OnClickListener tableClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            IntegerPacket packet = new IntegerPacket();
            packet.value = tables.get((Integer)view.getTag()).id;
            server.sendPacket(Packet.JOIN_TABLE_PACKET_ID, packet);
            tableModel = tables.get((Integer)view.getTag());
            loading = ProgressDialog.show(TableSelectActivity.this, "",
                    "Joinning table...", true);
        }
    };
}
