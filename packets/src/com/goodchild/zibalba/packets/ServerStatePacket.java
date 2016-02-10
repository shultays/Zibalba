package com.goodchild.zibalba.packets;

import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;

import java.util.List;
import java.util.Vector;

/**
 * Created by Engin Mercan on 15.01.2014.
 */
public class ServerStatePacket implements Packet {


    public List<TableModel> tables;
    public List<ClientModel> clients;
}
