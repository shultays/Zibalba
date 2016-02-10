package com.goodchild.zibalba.packets;

import com.google.gson.Gson;

/**
 * Created by Engin Mercan on 17.01.2014.
 */
public class PacketConverter {
    Gson gson = new Gson();


    public Packet getPacket(int packetID, String data){
        if(packetID<0) return null;
        switch (packetID){
            case Packet.TABLE_STATE_PACKET_ID:
            case Packet.CREATE_TABLE_PACKET_ID:
                return gson.fromJson(data, TableStatePacket.class);
            case Packet.SERVER_STATE_PACKET_ID:
                return gson.fromJson(data, ServerStatePacket.class);
            case Packet.INIT_PACKET_ID:
                return gson.fromJson(data, InitPacket.class);
            case Packet.JOIN_TABLE_PACKET_ID:
                return gson.fromJson(data, IntegerPacket.class);
            case Packet.JOIN_TABLE_RESULT_PACKET_ID:
            case Packet.START_GAME_RESULT_PACKET_ID:
            case Packet.CREATE_TABLE_RESULT_PACKET_ID:
            case Packet.TABLE_COMMIT_REPLY_PACKET_ID:
                return gson.fromJson(data, BooleanPacket.class);
            case Packet.TABLE_COMMIT_PACKET_ID:
                return gson.fromJson(data, TableCommitPacket.class);

        }
        return null;
    }
}
