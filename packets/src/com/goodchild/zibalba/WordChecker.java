package com.goodchild.zibalba;


import com.goodchild.zibalba.models.BoardPosModel;

import java.io.*;
import java.util.*;

/**
 * Created by Engin Mercan on 20.01.2014.
 */
public class WordChecker {

    private static int wordPositions[] = new int[29*29*29+1];
    private static int wordPositions2[] = new int[29*29];

    private static final int BOARD_SIZE = 450806;
    private static byte buffer[] = new byte[BOARD_SIZE+1];
    private static Random r = new Random();
    private static ArrayList<byte[]>boards[] = new ArrayList[8];

    private static String str   = "abc0defg1h2ijklmno3prs4tu5vyz";
    private static String str2  = "abcçdefgğhıijklmnoöprsştuüvyz";
    private static int arr2[] = new int[256];
    private static int wordCounts[] = new int[20];
    private static int wordStarts[][] = new int[20][];

    void createList(String path){
        String line = "";
        int count[] = new int[str.length()];
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            int total = 0;
            FileOutputStream out = new FileOutputStream("words.bin");
            while ((line = br.readLine()) != null) {
                boolean ok = true;

                for(int i=0; i<line.length() && ok; i++){
                    if(line.charAt(i) >= 255) ok = false;
                    if(ok && arr2[line.charAt(i)] == -1) ok = false;
                }
                if(!ok) continue;;

                out.write(line.length() );
                for(int i=0; i<line.length(); i++){
                    out.write(arr2[line.charAt(i)]);
                    count[arr2[line.charAt(i)]]++;
                    total++;
                }
                total++;
            }
            br.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i=0; i<count.length; i++){
            count[i] = (int)Math.ceil(count[i]/10000.0f);
            System.out.print(count[i] + ", ");
        }
        System.out.println("");

        for(int i=0; i<count.length; i++){
            while(count[i]-->0) System.out.print(i + ", ");
        }
    }

    public static void loadBoards(){
        for(int i=5; i<=12; i++){
            int pos = i-5;
            int len = i*i;
            boards[pos] = new ArrayList<byte[]>();

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(new File("boards"+i+".txt")));

                String line;
                while ((line = br.readLine()) != null) {
                    if(line.length() >= len){
                        byte board[] = new byte[len];
                        boolean error = false;
                        for(int j=0; j<len; j++){
                            board[j] = (byte) arr2[line.charAt(j)];
                            if(board[j] == -1){
                                error = true;
                                break;
                            }
                        }
                        if(!error){
                            boards[pos].add(board);
                        }else{
                            System.out.println("err" + i +" " + boards[pos].size() + " " + line);
                        }
                    }else{
                        System.out.println("err" + i +" " + boards[pos].size() + " " + line);
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("board " + i + "x" + i + " " + boards[pos].size());
        }
    }

    public static void init(InputStream inputStream) {

        for(int i=0; i<arr2.length; i++) arr2[i] = -1;

        for(int i=0; i<str.length(); i++){
            arr2[str.charAt(i)] = i;
        }

        try {
            int t = inputStream.read(buffer, 0, BOARD_SIZE);
            buffer[BOARD_SIZE] = 0;
            System.out.println("<<"+t + " " + inputStream.read());
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadBoards();

        int c = 0;
        int pos = -1;
        int start = -1;
        for(int i=0; i<29*29; i++){
            wordPositions2[i] = -1;
        }
        for(int i=0; i<29*29*29; i++){
           wordPositions[i] = -1;
        }
        while(c<BOARD_SIZE){
            int len = buffer[c];
            wordCounts[len]++;
            int currentStart = buffer[c+1]+buffer[c+2]*29+buffer[c+3]*29*29;
            wordPositions2[buffer[c+1] + buffer[c+2]*29] = 1;
            if(start != currentStart){
                pos++;
                start = currentStart;
                wordPositions[start] = c;
            }
            c += len+1;
        }

        int index[] = new int[wordCounts.length];
        for(int i=0; i<wordCounts.length; i++){
            wordStarts[i] = new int[wordCounts[i]];
        }
        c = 0;
        while(c<BOARD_SIZE){
            int len = buffer[c];
            wordStarts[len][index[len]] = c;
            index[len]++;
            c += len+1;
        }
    }

    public static void getBoard(int board[][]) {
        int size = board.length;
        int pos = size-5;
        Random r = new Random();

        byte selected[] = boards[pos].get(r.nextInt(boards[pos].size()));

        boolean flipVertical = r.nextBoolean();
        boolean flipHorizontal = r.nextBoolean();
        boolean swapCoors = r.nextBoolean();

        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                int a = i;
                int b = j;
                if(flipVertical) a = size-1-a;
                if(flipHorizontal) b = size-1-b;
                if(swapCoors){
                    int t = a;
                    a = b;
                    b = t;
                }
                int k = a+b*size;
                board[i][j] = selected[k];
            }
        }

        //calculateScore(board);
    }

    private static int WORD_SCORES[] = new int[]{0, 0, 0, 1, 5, 9, 15, 22, 30, 39, 49, 60, 72, 84, 98, 112, 127, 143, 160, 178};

    public static int wordToScore(int word[]){
        return WORD_SCORES[word.length];
    }

    public static void generateBoard2(int board[][]) {
        int column = board.length;
        int row = board[0].length;

        Vector<BoardPosModel> left = new Vector<BoardPosModel>();
        for(int i=0;i<column; i++){
            for(int j=0; j<row; j++){
                board[i][j] = -1;
                left.add(new BoardPosModel(i, j));
            }
        }

        int start = row + 2;
        for(int len = start; len>=3; len--){

            int tryCount = 1+(start-len);

            while(tryCount --> 0 && left.size() > 3){

                boolean set[][] = new boolean[column][row];
                Vector<BoardPosModel> used = new Vector<BoardPosModel>();
                BoardPosModel p = left.get(r.nextInt(left.size()));
                used.add(p);
                int k = len-1;

                int c = wordStarts[len][r.nextInt(wordCounts[len])]+1;
                int initc = c;

                board[p.x][p.y] = buffer[c++];
                set[p.x][p.y] = true;
                while(k-->0){
                    int x1 = p.x-1;
                    x1=x1<0?0:x1;
                    int x2 = p.x+1;
                    x2=x2>=column?column-1:x2;
                    int y1 = p.y-1;
                    y1=y1<0?0:y1;
                    int y2 = p.y+1;
                    y2=y2>=row?row-1:y2;

                    int cnt = 0;
                    for(int a=x1;a<=x2; a++){
                        for(int b=y1;b<=y2; b++){
                            if(!set[a][b] && (board[a][b] == -1 || board[a][b] == buffer[c]) && !(a==p.x && b == p.y)) cnt++;
                        }
                    }
                    if(cnt == 0){
                        break;
                    }
                    int use = r.nextInt(cnt);

                    for(int a=x1;a<=x2; a++){
                        for(int b=y1;b<=y2; b++){

                            if(!(!set[a][b] && (board[a][b] == -1 || board[a][b] == buffer[c]) && !(a==p.x && b == p.y))) continue;
                            if(use--==0){
                                p = new BoardPosModel(a, b);
                                used.add(p);
                                board[a][b] = buffer[c++];
                                set[a][b] = true;
                                break;
                            }
                        }
                    }
                }

                if(used.size() != len){
                    for(BoardPosModel pos : used){
                        board[pos.x][pos.y] = -1;
                    }
                }else{
                    /*System.out.println("put : " + len);
                    c=initc;
                    for(int i=0; i<buffer[c-1]; i++){
                        System.out.print(str.charAt(buffer[c+i]));
                    }
                    System.out.println("");*/
                    for(BoardPosModel pos : used){
                        for(int i=0; i<left.size(); i++){
                            if(left.get(i).x == pos.x && left.get(i).y == pos.y){
                                left.remove(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
        //System.out.println(left.size()+"<<");
        for(int i=0; i<column; i++){
            for(int j=0; j<row; j++){
                if(board[i][j] == -1)
                    board[i][j] = getLetter();
            }
        }
    }

    public static boolean check(String str){
        int arr[] = new int[str.length()];
        for(int i=0; i<str.length(); i++){
            arr[i] = arr2[str.charAt(i)];
        }
        return check(arr);
    }



    public static boolean check(int arr[]){
        if(arr.length < 3) return false;

        int start = arr[0]+arr[1]*29+arr[2]*29*29;
        if(wordPositions[start] == -1) return false;

        int c = wordPositions[start];
        while(c<BOARD_SIZE){
            boolean ok = true;
            if(buffer[c] != arr.length) ok = false;

            for(int i=0; ok && i<arr.length; i++){
                int t = buffer[c+1+i]-arr[i];
                if(t>0)return false;
                else if(t<0) ok = false;
            }
            if(ok) return true;

            c+=buffer[c]+1;
        }

        return false;
    }

    public static int getLetter() {
        return Constants.PROBABILITIES[r.nextInt(Constants.PROBABILITIES.length)];
    }



    /*

     */

    private static int rec_board[][] = new int[12][12];
    private static boolean rec_used[][] = new boolean[12][12];

    private static int rec_word[] = new int[16];
    private static int rec_wordl;
    private static int rec_found[] = new int[10000];
    private static int rec_foundl = 0;
    private static int rec_n = 0;

    static void calculateScore(int[][] board){
        rec_n = board.length;

        for(int i=0; i<rec_n; i++){
            for(int j=0; j<rec_n; j++){
                rec_board[i][j] = board[i][j];
                rec_used[i][j] = false;
                //System.out.print(str2.charAt(rec_board[i][j]));
            }
            //System.out.println("");
        }
        rec_wordl = 0;
        rec_found[0] = BOARD_SIZE;
        rec_foundl = 1;

        for(int i=0; i<rec_n; i++){
            for(int j=0; j<rec_n; j++){
                rec(i, j, -1);
            }
        }
        System.out.println(rec_foundl+"<");

        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int i=0; i<rec_foundl; i++){
            list.add(rec_found[i]);
        }

        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer x, Integer y) {
                int l = buffer[x]+1;
                for(int i=0; i<l+1; i++){
                    int diff = buffer[x+i]-buffer[y+i];
                    if(diff != 0) return diff;
                }
                return  0;
            }
        });
        int score = 0;
        for(int i=1; i<list.size(); i++){
            if(list.get(i-1).intValue() != list.get(i).intValue()){
                int c = list.get(i);
                for(int j=0; j<buffer[c]; j++){
                    System.out.print(str2.charAt(buffer[c+1+j]));
                }
                System.out.println("<");
                score += WORD_SCORES[buffer[c]];
            }
        }
        System.out.println(score + "");
    }

    private static void rec(int x, int y, int start){
        if(x<0||y<0||x>=rec_n||y>=rec_n) return;
        if(rec_used[x][y]) return;

        rec_used[x][y] = true;
        rec_word[rec_wordl++] = rec_board[x][y];
        boolean hasHope = false;

        if(rec_wordl == 1) hasHope = true;
        else if(rec_wordl == 2){
            if(wordPositions2[rec_word[0]+rec_word[1]*29] != -1) hasHope = true;
        }else{

            int c = wordPositions[rec_word[0]+rec_word[1]*29+rec_word[2]*29*29];

            if(rec_wordl == 3) start = c;
            while(c<BOARD_SIZE){
                if(buffer[c+1] != rec_word[0] || buffer[c+2] != rec_word[1] || buffer[c+3] != rec_word[2]) break;


                if(buffer[c] >= rec_wordl){
                    int i;
                    for(i=0; i<rec_wordl; i++){
                        if(buffer[c+i+1] != rec_word[i]){
                            if(buffer[c+i+1] < rec_word[i]) start = c;
                            break;
                        }
                    }
                    boolean valid = (i==rec_wordl);
                    hasHope |= valid;
                    if(valid && buffer[c] == rec_wordl){
                        rec_found[rec_foundl++] = c;
                    }
                }
                c+= buffer[c]+1;
            }
        }

        if(hasHope){
            rec(x-1, y, start);
            rec(x+1, y, start);
            rec(x, y-1, start);
            rec(x, y+1, start);

            rec(x-1, y-1, start);
            rec(x+1, y-1, start);
            rec(x-1, y+1, start);
            rec(x+1, y+1, start);
        }


        rec_used[x][y] = false;
        rec_wordl--;
    }
}
