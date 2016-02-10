package com.goodchild.zibalba.packets;

/**
 * Created by Engin Mercan on 15.01.2014.
 */
public interface Packet {
    public static final int INIT_PACKET_ID = 42;
    public static final int SERVER_STATE_PACKET_ID = 43;
    public static final int CREATE_TABLE_PACKET_ID = 44;
    public static final int JOIN_TABLE_PACKET_ID = 45;
    public static final int JOIN_TABLE_RESULT_PACKET_ID = 46;
    public static final int CREATE_TABLE_RESULT_PACKET_ID = 50;
    public static final int TABLE_STATE_PACKET_ID = 61;

    public static final int START_GAME_PACKET_ID = -62;
    public static final int START_GAME_RESULT_PACKET_ID = 62;


    public static final int TABLE_COMMIT_PACKET_ID = 70;
    public static final int TABLE_COMMIT_REPLY_PACKET_ID = 71;


    public static final int FINISH_GAME = -80;

    public static final int LEAVE_TABLE_PACKET_ID = -85;

    public static int ADMIN_NOTIFY_PACKET_ID = -95;

    public static int CONNECTION_INITED_PACKET_ID = -100;
    public static int CONNECTION_ERROR_PACKET_ID = -101;
    public static int CONNECTION_ERROR_REPEAT_PACKET_ID = -103;
    public static int CONNECTION_ERROR_ON_INIT_PACKET_ID = -102;
}
