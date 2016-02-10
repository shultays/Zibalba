
import com.goodchild.zibalba.Constants;
import com.goodchild.zibalba.WordChecker;
import com.goodchild.zibalba.models.BoardPosModel;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

//Engin Mercan
//250702022
//CSE471 Term Project


public class Table {

    Random r = new Random();

    Gson gson = new Gson();

    static int nextRoom = 0;
    TableModel model = new TableModel();

    Vector<ClientThread> playerThreads = new Vector<ClientThread>();
    Server server;

    boolean started = false;
    boolean finished = false;

    ClientThread admin;

    private long lastCommit;

    Table(Server server){
        this.server = server;
    	model.id = nextRoom;
    	nextRoom++;

        model.row = 5;
        model.column = 5;
        model.gameDuration = 60;
    }

    public synchronized void sendAll(int packetID){
        sendAll(packetID, null, Object.class);
    }

    private void printBoard(){
        for(int i=0; i<model.column; i++){
            for(int j=0; j<model.row; j++){
                System.out.print(Constants.CHAR_LIST.charAt(model.board[j][i]) + " ");
            }
            System.out.println("");
        }
    }

    public synchronized void sendAll(int packetID, Packet packet, Class type){
        for(ClientThread client : playerThreads){
            if(client != null && client.active){
                client.sendPacket(packetID, packet, type);
            }
        }
    }


    public synchronized void sendAll(int packetID, String string){
        for(ClientThread client : playerThreads){
            if(client != null && client.active){
                client.sendPacket(packetID, string);
            }
        }
    }


    public synchronized boolean addPlayer(ClientThread player){
        if(admin == null) admin = player;
        model.players.add(player.model);
        playerThreads.add(player);
        resendState();
        return true;
    }


    public synchronized void removePlayer(final ClientThread player) {
        model.players.remove(player.model);
        playerThreads.remove(player);
        resendState();

        server.put(new Runnable() {
            @Override
            public void run() {
                player.leaveTable();
            }
        });

        if(playerThreads.isEmpty()){
            server.put(new Runnable() {
                @Override
                public void run() {
                    player.leaveTable();
                    server.tables.remove(Table.this);
                }
            });
        }else if(admin == player){
            playerThreads.get(0).sendPacket(Packet.ADMIN_NOTIFY_PACKET_ID, null, Object.class);
        }
    }

    public synchronized void startGame() {
        model.board = new int[model.column][model.row];
        WordChecker.getBoard(model.board);
        resendState();

        BooleanPacket booleanPacket = new BooleanPacket();
        booleanPacket.value = true;
        sendAll(Packet.START_GAME_RESULT_PACKET_ID, booleanPacket, BooleanPacket.class);

        started = true;
        lastCommit = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int duration = model.gameDuration;
                long start = System.currentTimeMillis();
                while(duration --> 0){
                    tick();
                    try {
                        start += 1000;
                        Thread.sleep(start - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                endGame();
            }
        }).start();

    }

    private synchronized  void tick() {
        if(finished) return;
        if(System.currentTimeMillis() - lastCommit > 15000){
            resetBoard();
        }
    }

    private synchronized void resetBoard() {
        lastCommit = System.currentTimeMillis();
        TableCommitPacket tableCommitPacket = new TableCommitPacket();
        tableCommitPacket.newData = new int[model.column*model.row];
        tableCommitPacket.cells = new BoardPosModel[model.column*model.row];
        int z = 0;
        WordChecker.getBoard(model.board);
        for(int i=0; i<model.column; i++){
            for(int j=0; j<model.row; j++){
                tableCommitPacket.cells[z] = new BoardPosModel(i, j);
                tableCommitPacket.newData[z++] = model.board[i][j];
            }
        }

        sendAll(Packet.TABLE_COMMIT_PACKET_ID, tableCommitPacket, TableCommitPacket.class);
    }

    private synchronized void endGame() {
        if(!finished){
            finished = true;
            resendState();
            sendAll(Packet.FINISH_GAME);
            server.put(new Runnable() {
                @Override
                public void run() {
                    for(ClientThread client : playerThreads){
                        client.leaveTable();
                    }
                    server.tables.remove(Table.this);
                }
            });
        }
    }

    private synchronized void resendState() {
        TableStatePacket tableStatePacket = new TableStatePacket();
        tableStatePacket.table = model;
        sendAll(Packet.TABLE_STATE_PACKET_ID, tableStatePacket, TableStatePacket.class);
    }

    public synchronized void handlePacket(ClientThread clientThread, int packetID, Packet packet) {
        switch(packetID){
            case Packet.TABLE_STATE_PACKET_ID:
            {
                TableStatePacket tableStatePacket = (TableStatePacket) packet;
                model.column = tableStatePacket.table.column;
                model.row = tableStatePacket.table.row;
                model.maxPlayer = tableStatePacket.table.maxPlayer;
                model.gameDuration = tableStatePacket.table.gameDuration;
                resendState();
            }
            return;
            case Packet.LEAVE_TABLE_PACKET_ID:
                removePlayer(clientThread);
                return;
            case Packet.START_GAME_PACKET_ID:
                startGame();
                return;
            case Packet.TABLE_COMMIT_PACKET_ID:
            {
                TableCommitPacket tableCommitPacket = (TableCommitPacket) packet;
                boolean correct = true;

                for(int i=0; correct && i<tableCommitPacket.cells.length; i++){
                    if(model.board[tableCommitPacket.cells[i].x][tableCommitPacket.cells[i].y] != tableCommitPacket.data[i]){
                        correct = false;
                    }
                }

                if(correct) correct = WordChecker.check(tableCommitPacket.data);

                BooleanPacket booleanPacket = new BooleanPacket();
                booleanPacket.value = correct;
                clientThread.sendPacket(Packet.TABLE_COMMIT_REPLY_PACKET_ID, booleanPacket, BooleanPacket.class);

                if(correct){
                    clientThread.model.score += WordChecker.wordToScore(tableCommitPacket.data);
                    tableCommitPacket.players = model.players;
                    lastCommit = System.currentTimeMillis();
                    tableCommitPacket.newData = new int[tableCommitPacket.cells.length];
                    for(int i=0; i<tableCommitPacket.newData.length; i++){
                        tableCommitPacket.newData[i] = WordChecker.getLetter();
                        model.board[tableCommitPacket.cells[i].x][tableCommitPacket.cells[i].y] = tableCommitPacket.newData[i];
                    }
                    sendAll(Packet.TABLE_COMMIT_PACKET_ID, tableCommitPacket, TableCommitPacket.class);
                }
            }
            return;
        }
    }
}
