import com.goodchild.zibalba.Constants;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.packets.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Vector;


//Engin Mercan
//250702022
//CSE471 Term Project


public class ClientThread implements Runnable{
    ClientModel model = new ClientModel();

    Socket socket = null;

    DataInputStream in;
    DataOutputStream out;

    boolean active;

    static int nextId = 0;
    Gson gson = new Gson();
    Server server;

    Table table;

    PacketConverter packetConverter = new PacketConverter();

    public ClientThread(Socket sock, Server server){
        this.server = server;
    	socket = sock;
    	try {
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
    	} catch (IOException e) {
		}
        model.id = nextId;
        nextId++;
        active = true;

        new Thread(this).start();
    }


    public boolean sendPacket(int packetID, Packet packet, Class type) {
        try {
            out.writeInt(packetID);
            String packetJson = gson.toJson(packet);
            out.writeUTF(packetJson);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean sendPacket(int packetID, String packetJson) {
        try {
            out.writeInt(packetID);
            out.writeUTF(packetJson);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            die();
            return false;
        }
        return true;
    }


    @Override
    public void run() {

        sendPacket(Packet.INIT_PACKET_ID, null, Object.class);

        while (true){
            int packetID = -1;
            String packetString;

            try {
                packetID = in.readInt();
                packetString = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
                die();
                return;
            }
            Packet packet = packetConverter.getPacket(packetID, packetString);

            switch (packetID){
                case Packet.INIT_PACKET_ID:
                {
                    InitPacket initPacket = (InitPacket) packet;
                    model.name = initPacket.model.name;
                    model.icon = initPacket.model.icon;
                    model.picturePath = initPacket.model.picturePath;
                }
                break;
                case Packet.JOIN_TABLE_PACKET_ID:
                    joinTableHandle((IntegerPacket) packet);
                    break;
                case Packet.CREATE_TABLE_PACKET_ID:
                {
                    createTableHandle((TableStatePacket) packet);
                }
                break;
                case Packet.TABLE_COMMIT_PACKET_ID:
                case Packet.START_GAME_PACKET_ID:
                case Packet.LEAVE_TABLE_PACKET_ID:
                case Packet.TABLE_STATE_PACKET_ID:
                    if(table != null) table.handlePacket(this, packetID, packet);
                    break;
            }
        }
    }

    private void joinTableHandle(final IntegerPacket packet) {
        server.put(new Runnable() {
            @Override
            public void run() {
                BooleanPacket reply = new BooleanPacket();
                for(Table t : server.tables){
                    synchronized (t){
                        if(t.model.id == packet.value){
                            if(t.started == false && t.model.maxPlayer != t.playerThreads.size()){
                                reply.value = true;
                                table = t;
                            }
                            break;
                        }
                    }
                }

                sendPacket(Packet.JOIN_TABLE_RESULT_PACKET_ID, reply, BooleanPacket.class);

                if(reply.value){
                    table.addPlayer(ClientThread.this);

                    TableStatePacket tablePacket = new TableStatePacket();
                    tablePacket.table = table.model;

                    sendPacket(Packet.TABLE_STATE_PACKET_ID, tablePacket, TableStatePacket.class);
                    model.score = 0;
                }
            }
        });
    }

    private void createTableHandle(final TableStatePacket packet) {
        server.put(new Runnable() {
            @Override
            public void run() {
                table = new Table(server);
                table.model.row = packet.table.row;
                table.model.column = packet.table.column;
                table.model.gameDuration = packet.table.gameDuration;
                table.model.maxPlayer = packet.table.maxPlayer;
                table.addPlayer(ClientThread.this);
                server.tables.add(table);

                BooleanPacket packet = new BooleanPacket();
                packet.value = true;

                sendPacket(Packet.CREATE_TABLE_RESULT_PACKET_ID, packet, BooleanPacket.class);

                TableStatePacket tablePacket = new TableStatePacket();
                tablePacket.table = table.model;

                sendPacket(Packet.TABLE_STATE_PACKET_ID, tablePacket, TableStatePacket.class);
                model.score = 0;
            }
        });
    }

    private void die() {
        active = false;
        if(table != null) table.removePlayer(ClientThread.this);
        table = null;
    }

    public void leaveTable(){
        table = null;
    }

}
