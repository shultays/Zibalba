package com.goodchild.zibalba.packets;

import com.goodchild.zibalba.models.BoardPosModel;
import com.goodchild.zibalba.models.ClientModel;

import java.util.List;
import java.util.Vector;

/**
 * Created by Engin Mercan on 17.01.2014.
 */
public class TableCommitPacket implements Packet{
    public BoardPosModel cells[];
    public int newData[];

    public int[] data;
    public List<ClientModel> players = new Vector<ClientModel>();
}
