package com.goodchild.zibalba.models;


import com.goodchild.zibalba.Constants;

import java.util.List;
import java.util.Vector;

/**
 * Created by Engin Mercan on 15.01.2014.
 */
public class TableModel {

    public List<ClientModel> players = new Vector<ClientModel>();
    public int id;
    public int row, column;
    public int gameDuration;
    public int[][] board;
    public int maxPlayer;
}
