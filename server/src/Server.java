import com.goodchild.zibalba.Constants;
import com.goodchild.zibalba.WordChecker;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.ServerStatePacket;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


//Engin Mercan
//250702022
//CSE471 Term Project

public class Server extends Thread{
	
	public Vector<Table> tables = new Vector<Table>();
    public Vector<ClientThread> clients = new Vector<ClientThread>();

    LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<Runnable>();

    ServerStatePacket serverStatePacket;
    Gson gson = new Gson();
	public static void main(String args[]) throws IOException{
        Server server = new Server();
	}

    public Server(){

        /*
        FileInputStream fstream = null;
        Vector<String> lines = new Vector<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("./words.txt")));
            String line;
            while((line = br.readLine()) != null){
                for(int i=0; i<line.length(); i++){
                    if("abc0defg1h2ijklmno3prs4tu5vyz".contains(line.charAt(i)+"") == false){
                        System.out.println(line+"<<<<");
                    }
                }
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final int arr[] = new int[255];
        for(int i = 0; i<"abc0defg1h2ijklmno3prs4tu5vyz".length(); i++){
            arr["abc0defg1h2ijklmno3prs4tu5vyz".charAt(i)] = i;
        }

        Collections.sort(lines, new Comparator<String>() {
            private  int charCompare(char c, char c2){
                return arr[c]-arr[c2];
            }
            @Override
            public int compare(String s, String s2) {
                int t = Math.min(s.length(), s2.length());
                for(int i=0; i<t; i++){
                    int t2 = charCompare(s.charAt(i), s2.charAt(i));
                    if(t2!=0) return t2;
                }
                if(s.length()>s2.length())return 1;
                else if(s.length()<s2.length())return -1;
                System.out.println("hmm " + s + " " + s2);
                return 1;
            }
        });

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File("file2.txt")));
            for(String s : lines){
                out.write(s+"\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);*/


        try {
            WordChecker.init(new FileInputStream("./words.bin"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
/*
        int board[][] = new int[6][6];
        WordChecker.generateBoard(board);
        System.exit(0);
*/
        ServerSocket server = null;
        try {
            server = new ServerSocket(Constants.PORT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        TableModel tableModel = new TableModel();
        System.out.println("Server is ready.");
        System.out.println("Waiting for players...");
        new Thread(new Runnable(){
            public void run(){
                while(true){
                    try {
                        put(new Runnable() {
                            @Override
                            public void run() {
                                resendState();
                            }
                        });
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        start();


        while(true){
            Socket newClient = null;
            try {
                newClient = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            final Socket finalNewClient = newClient;
            put(new Runnable() {
                @Override
                public void run() {
                    ClientThread ct = new ClientThread(finalNewClient, Server.this);
                    clients.add(ct);
                }
            });
        }
    }

    public void run(){
        while(true){
            Runnable runnable = null;
            try {
                runnable = queue.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            if(runnable != null){
                runnable.run();
            }
        }

    }
    private void resendState() {
        ServerStatePacket serverStatePacket = new ServerStatePacket();
        serverStatePacket.tables = new Vector<TableModel>();
        for(Table table : tables){
            synchronized (table){
                if(table.started == false)
                    serverStatePacket.tables.add(table.model);
            }
        }
        serverStatePacket.clients = new Vector<ClientModel>();
        for(ClientThread client : clients){
            if(client.active && client.table == null){
                serverStatePacket.clients.add(client.model);
            }
        }
/*
        for(int i=0; i<50; i++){
            TableModel tableModel = new TableModel();
            tableModel.id = 123;
            tableModel.gameDuration = 5;
            tableModel.row = 5;
            tableModel.column = 5;
            tableModel.players = new Vector<ClientModel>();
            for(int j=0; j<5; j++){
                ClientModel clientModel = new ClientModel();
                clientModel.name = "Client_"+j;
                clientModel.icon = (i*5+j)%48;
                tableModel.players.add(clientModel);
            }
            serverStatePacket.tables.add(tableModel);
        }
*/
        String message = gson.toJson(serverStatePacket, ServerStatePacket.class);
        System.out.println(message);
        for(int i=0; i<clients.size(); i++){
            ClientThread client = clients.get(i);
            if(client != null && client.active && client.table == null){
                client.sendPacket(Packet.SERVER_STATE_PACKET_ID, message);
            }
            if(client == null || client.active == false){
                clients.remove(i);
                i--;
            }
        }
    }

    public void put(Runnable runnable){
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
        }
    }

}