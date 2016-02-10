package com.goodchild.zibalba;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.goodchild.zibalba.Constants;
import com.goodchild.zibalba.packets.BooleanPacket;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.PacketConverter;
import com.goodchild.zibalba.packets.ServerStatePacket;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by Engin Mercan on 15.01.2014.
 */
public class ServerThread extends Thread {
    Socket server;
    DataInputStream in;
    DataOutputStream out;
    Listener listener;
    Gson gson;
    Context context;
    Handler handler;
    boolean running = true;


    PacketConverter packetConverter = new PacketConverter();

    static ServerThread instance;

    public Queue<Message> messageQueue = new LinkedList<Message>();

    public Listener getListener() {
        return listener;
    }

    public static class Message{
        public int packetID;
        String packetString;
        public Packet packet;

        public Message(int packetID, String packetData, Packet packet) {
            this.packet = packet;
            this.packetID = packetID;
            this.packetString = packetData;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if(listener != null && !running){
            final Message message = new Message(Packet.CONNECTION_ERROR_REPEAT_PACKET_ID, null, null);
            addMessage(message);
        }
        if(listener != null && messageQueue.size()>0){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ServerThread.this.listener.packetReceived();
                }
            });
        }
    }

    public static interface Listener{
        public void packetReceived();
    }
    public ServerThread(Context context, Listener listener){
        handler = new Handler();
        this.context = context;
        gson = new Gson();
        instance = this;
        this.listener = listener;
        this.start();
    }

    public void closeSocket(){
        running = false;
        try {
            if(in != null)in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(out != null)out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(server != null)server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run(){

        try {
            server = new Socket();
            server.connect(new InetSocketAddress(Constants.SERVER_IP, Constants.PORT), 5000);

            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
        } catch (Exception e) {
            running = false;
            Message message = new Message(Packet.CONNECTION_ERROR_ON_INIT_PACKET_ID, null, null);
            addMessage(message);
            return;
        }

        if(listener != null){
            Message message = new Message(Packet.CONNECTION_INITED_PACKET_ID, null, null);
            addMessage(message);
        }
        while(running){
            int packetID = -1;
            String packetData = null;
            Packet packet = null;
            try {
                packetID = in.readInt();
                packetData = in.readUTF();
                packet = packetConverter.getPacket(packetID, packetData);

            } catch (IOException e) {
                running = false;
                final Message message = new Message(Packet.CONNECTION_ERROR_PACKET_ID, null, null);
                addMessage(message);
                return;
            }
            if(packetID != -1){
                Message newMessage = new Message(packetID, packetData, packet);
                addMessage(newMessage);
            }
        }
    }

    private void addMessage(final Message message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                messageQueue.add(message);
                if(listener!=null)listener.packetReceived();
            }
        });
    }

    public static ServerThread getInstance(){
        return instance;
    }


    public void sendPacket(int packetID, Packet packet) {
        if(!running) return;
        try {
            out.writeInt(packetID);
            String packetJson = gson.toJson(packet);
            out.writeUTF(packetJson);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
