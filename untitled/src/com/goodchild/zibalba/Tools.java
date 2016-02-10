package com.goodchild.zibalba;

import java.util.Random;

/**
 * Created by Engin Mercan on 23.01.2014.
 */
public class Tools {

    public static String getGuestName(){
        Random r = new Random();
        String s = ClientConstants.randomNames[r.nextInt(ClientConstants.randomNames.length)];
        if(s.length() < 7 && r.nextBoolean()){
            String s2 = "";
            do{
                s2 =  ClientConstants.randomNames[r.nextInt(ClientConstants.randomNames.length)];
            }while(s.compareTo(s2) == 0 || s2.length() > 10);
            s += " " + s2;
        }
        return s;
    }
}
