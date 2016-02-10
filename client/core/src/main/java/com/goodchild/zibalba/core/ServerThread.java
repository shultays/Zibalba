package com.goodchild.zibalba.core;

import com.goodchild.zibalba.Constants;
import com.goodchild.zibalba.packets.CreateTablePacket;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.ServerStatePacket;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;


/**
 * Created by Engin Mercan on 15.01.2014.
 */
public class ServerThread extends Thread {
    Socket server;
    DataInputStream in;
    DataOutputStream out;
    Listener listener;
    Gson gson;

    public static interface Listener{
        public void packetReceived(int packetID, Packet packet, String s);
    }
    public ServerThread(Listener listener){
        gson = new Gson();
        this.listener = listener;
        this.start();
    }

    public void run(){

        try {
            server = new Socket("127.0.0.1", Constants.PORT);
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            int packetID = -1;
            String packetData = null;
            Packet packet = null;
            try {
                packetID = in.readInt();
                packetData = in.readUTF();
                packet = getPacket(packetID, packetData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(packetID != -1){
                listener.packetReceived(packetID, packet, packetData);
            }
        }
    }

    public Packet getPacket(int packetID, String data){
        switch (packetID){
            case ServerStatePacket.PACKET_ID:
            return gson.fromJson(data, ServerStatePacket.class);
        }
        return null;
    }

    public void sendPacket(int packetID, Packet packet) {
        try {
            out.writeInt(packetID);
            String packetJson = gson.toJson(packet);
            out.writeUTF(packetJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
