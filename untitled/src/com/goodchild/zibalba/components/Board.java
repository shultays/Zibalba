package com.goodchild.zibalba.components;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.goodchild.zibalba.Constants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.ServerThread;
import com.goodchild.zibalba.WordChecker;
import com.goodchild.zibalba.models.BoardPosModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.packets.Packet;
import com.goodchild.zibalba.packets.TableCommitPacket;

import java.util.*;

/**
 * Created by Engin on 1/16/14.
 */
public class Board extends View implements View.OnTouchListener {
    int row = 6;
    int column = 6;

    Bitmap cell;
    Rect temp = new Rect();
    Rect temp2 = new Rect();
    Rect temp3 = new Rect();

    boolean selected[][];
    long selectedStart[][];
    Vector<BoardPosModel>selectedPos = new Vector<BoardPosModel>();

    Queue<Vector<BoardPosModel>> selectedPosQueue = new LinkedList<Vector<BoardPosModel>>();


    int board[][];
    boolean animating[][];
    long animateStart[][];
    int afterAnimateBoard[][];

    boolean rejected[][];
    long rejectedStart[][];

    Random r = new Random();
    private ServerThread server;
    private boolean ignoreTouch = false;

    public Board(Context context) {
        super(context);
        initialize();
    }

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public Board(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void initialize(TableModel model){
        this.row = model.row;
        this.column = model.column;
        initialize();
        for(int i=0; i<column; i++){
            for(int j=0; j<row; j++){
                board[i][j] = model.board[i][j];
            }
        }
    }

    private void initialize() {
        cell = BitmapFactory.decodeResource(getResources(),
                R.drawable.cellbg);
        selected = new boolean[column][row];
        selectedStart = new long[column][row];
        board = new int[column][row];

        animating = new boolean[column][row];
        animateStart = new long[column][row];
        afterAnimateBoard = new int[column][row];

        rejected = new boolean[column][row];
        rejectedStart = new long[column][row];

        for(int i=0; i<column; i++){
            for(int j=0; j<row; j++){
                afterAnimateBoard[i][j] = -1;
                board[i][j] = r.nextInt(Constants.CHAR_LIST.length());
            }
        }
        if(isInEditMode()){
            newInput = "ÖÇŞĞY";
        }
        setOnTouchListener(this);
    }
    Paint paint = new Paint();

    int oldW=-1, oldH=-1;
    int w, h, x, y, p;
    float size, size2;

    String oldInput = "";
    String newInput = "";
    float[] matrix = {
            1, 0, 0, 0, 0, //red
            0, 1, 0, 0, 0, //green
            0, 0, 1, 0, 0, //blue
            0, 0, 0, 1, 0 //alpha
    };

    float scale = 1.0f;

    public void onDraw(Canvas canvas){
        long time = System.currentTimeMillis();
        canvas.drawColor(0x66000000);
        boolean dirty = false;
        int width = getWidth();
        int height = getHeight();

        if(width != oldW || height != oldH){
            oldW = width;
            oldH = height;
            w = width/column;

            if(height < w*(column+2.2f)){
                w *= ((float)height)/((w*(column+2.2f) + p*column));
            }

            p = (int) ((w*column*0.1f)/(column-1));
            w = w-p;

            x = (width-w*column-p*(column-1))/2;
            y = (int) ((height-w*0.8f-p-w*column-p*(column-1))/2+w+p);

            size = 16.f;

            while(true){
                paint.setTextSize(size);
                paint.getTextBounds("W", 0, 1, temp2);
                if(temp2.width()>w*0.6)break;
                size+=1f;
            }

            paint.setTextAlign(Paint.Align.CENTER);
            paint.getTextBounds("W", 0, 1, temp2);


        }

        paint.setTextSize(size);

        for(int i=0; i<column; i++){
            for(int j=0; j<row; j++){
                canvas.save();
                temp.set(x+i*(w+p), y+j*(w+p), x+i*(w+p)+w, y+j*(w+p)+w);
                if(selected[i][j]){
                    matrix[0] = 0.9f;
                    matrix[6] = 1.0f;
                    matrix[12] = 0.9f;
                    paint.setColorFilter(new ColorMatrixColorFilter(matrix));

                    float t = (float) (Math.sin((time-selectedStart[i][j])/60.0f)*16);
                    canvas.rotate(t, temp.centerX(), temp.centerY());

                    dirty = true;
                }
                if(rejected[i][j]){
                    if(time-rejectedStart[i][j] > 400){
                        rejected[i][j] = false;
                    }else{

                        float t = (float) (Math.sin((time-selectedStart[i][j])/60.0f)*16);
                        float friction = 1-(time-rejectedStart[i][j])/400.0f;
                        canvas.rotate(t * friction, temp.centerX(), temp.centerY());

                        matrix[0] = 1.0f;
                        matrix[6] = 0.8f;
                        matrix[12] = 0.8f;
                        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
                        dirty = true;
                    }
                }
                if(animating[i][j]){
                    int diff = (int) (time-animateStart[i][j]);
                    if(diff>0){
                        float t = (float) Math.cos((diff/500.0)*Math.PI);
                        if(time-animateStart[i][j] > 500.0){
                            animating[i][j] = false;
                            t=-1;
                        }
                        if(t<0 && afterAnimateBoard[i][j] != -1){
                            board[i][j] = afterAnimateBoard[i][j];
                            afterAnimateBoard[i][j] = -1;
                        }
                        canvas.scale(Math.abs(t), 1.0f, temp.centerX(), temp.centerY());
                    }
                    dirty = true;
                }

                canvas.drawBitmap(cell, null, temp, paint);
                if(selected[i][j] || rejected[i][j]){
                    paint.setColorFilter(null);
                }

                paint.setColor(Color.BLACK);
                canvas.drawText(Constants.CHAR_LIST.charAt(board[i][j])+"", temp.centerX(), temp.centerY()+(temp.height()-temp2.height())*0.6f, paint);


                canvas.restore();
            }

            if(dirty){
                postInvalidate();
            }
        }

        if(oldInput.compareTo(newInput) != 0){
            oldInput = newInput;

            size2 = size*1.5f;

            while(true){
                paint.setTextSize(size2);
                paint.getTextBounds(newInput, 0, newInput.length(), temp3);
                if(temp3.width()<width*0.8f)break;
                size2-=2f;
            }
        }

        paint.setTextSize(size2);
        paint.setColor(Color.WHITE);
        canvas.drawText(newInput, width/2, y/2, paint);
    }
    float oldX, oldY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean changed = false;
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            selectedPos.clear();
            oldX = event.getX();
            oldY = event.getY();
            changed = true;
            ignoreTouch = false;
        }
        if(ignoreTouch) return true;
        float diffX = oldX-event.getX();
        float diffY = oldY-event.getY();
        float diff = (float) Math.sqrt(diffX*diffX+diffY*diffY);

        for(float t=w/2; t<diff; t+=w/2){
            float b = t/diff;
            float a = 1-b;

            float testX = oldX*a + event.getX()*b;
            float testY = oldY*a + event.getY()*b;
            changed |= handleTouch(testX, testY);
        }

        changed |= handleTouch(event.getX(), event.getY());
        oldX = event.getX();
        oldY = event.getY();
        if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP){
            boolean send = false;

            if(selectedPos.size() >= 3){
                TableCommitPacket tableCommitPacket = new TableCommitPacket();
                tableCommitPacket.cells = new BoardPosModel[selectedPos.size()];
                tableCommitPacket.data = new int[selectedPos.size()];
                for(int i=0; i<tableCommitPacket.cells.length; i++){
                    tableCommitPacket.cells[i] = selectedPos.get(i);
                    tableCommitPacket.data[i] = board[selectedPos.get(i).x][selectedPos.get(i).y];
                }
                if(WordChecker.check(tableCommitPacket.data)){
                    send = true;

                    server.sendPacket(Packet.TABLE_COMMIT_PACKET_ID, tableCommitPacket);
                    selectedPosQueue.add(selectedPos);
                }
            }

            if(!send){
                popSelected(selectedPos, true);
            }
            selectedPos = new Vector<BoardPosModel>();
            changed = true;

        }
        if(changed){
            newInput = "";
            for(BoardPosModel pos : selectedPos){
                newInput += Constants.CHAR_LIST.charAt(board[pos.x][pos.y]);
            }
            invalidate();
        }
        return true;
    }

    private boolean handleTouch(float mx, float my) {
        boolean changed = false;
        BoardPosModel hover = findInside(mx, my);
        if(hover != null){
            int x = hover.x;
            int y = hover.y;

            if(animating[x][y]){
                ;
            }else if(selected[x][y] == false){
                boolean dontAdd = false;
                if(selectedPos.size() > 0){
                    int oldX = selectedPos.lastElement().x;
                    int oldY = selectedPos.lastElement().y;
                    int diff = Math.abs(x-oldX)+Math.abs(y-oldY);
                    if(Math.abs(x-oldX) > 1 || Math.abs(y-oldY) > 1 || diff == 0){
                        dontAdd = true;
                    }
                }
                if(dontAdd == false){

                    selectedStart[x][y] = System.currentTimeMillis();
                    selected[x][y] = true;
                    rejected[x][y] = false;
                    selectedPos.add(hover);
                    changed = true;
                }
            }else{
                boolean currentSelection = false;
                for(int i=0; i<selectedPos.size(); i++){
                    if(x == selectedPos.get(i).x && y == selectedPos.get(i).y){
                        currentSelection = true;
                    }
                }
                while(currentSelection && selectedPos.size()>0){
                    int oldX = selectedPos.lastElement().x;
                    int oldY = selectedPos.lastElement().y;
                    if(oldX == x && oldY == y){
                        break;
                    }else{
                        selectedPos.removeElementAt(selectedPos.size()-1);
                        selected[oldX][oldY] = false;
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    BoardPosModel findInside(float mx, float my){
        float r =  (w*w*0.3f);
        for(int i=0; i<column; i++){
            for(int j=0; j<row; j++){
                temp.set(x+i*(w+p), y+j*(w+p), x+i*(w+p)+w, y+j*(w+p)+w);
                float dx = temp.centerX()-mx;
                float dy = temp.centerY()-my;
                if(dx*dx+dy*dy<r){
                    return new BoardPosModel(i, j);
                }
            }
        }
        return null;
    }

    boolean firstAnimate = true;

    public void changeBoard(TableCommitPacket packet) {
        long t = System.currentTimeMillis();
        int len = 100;

        boolean breakSelected = false;
        for(int i=0; i<selectedPos.size(); i++){
            for(int j=0; j<packet.cells.length; j++){
                if(selectedPos.get(i).x == packet.cells[j].x && selectedPos.get(i).y == packet.cells[j].y){
                    breakSelected = true;
                    break;
                }
            }
        }


        if (breakSelected){
            long time = System.currentTimeMillis();
            for(BoardPosModel pos : selectedPos){
                selected[pos.x][pos.y] = false;
                rejected[pos.x][pos.y] = true;
                rejectedStart[pos.x][pos.y] = time;
            }
            selectedPos.clear();
            ignoreTouch = true;
            newInput = "";
        }

        for(int i=0; i<packet.newData.length; i++){
            BoardPosModel pos = packet.cells[i];
            animateStart[pos.x][pos.y] = t;
            len-=4;
            if(len<10)len = 10;
            t += len;
            animating[pos.x][pos.y] = true;
            afterAnimateBoard[pos.x][pos.y] = packet.newData[i];
        }
        if(packet.newData.length == row*column){
            t = System.currentTimeMillis();
            int maxDelay = 1000;
            int minDelay = 0;
            boolean flipVertical = r.nextBoolean();
            boolean flipHorizontal = r.nextBoolean();
            boolean swapCoors = r.nextBoolean();
            int type = r.nextInt(8);
            if(firstAnimate){
                firstAnimate = false;
                type = 5;
            }
            for(int i=0; i<column; i++){
                for(int j=0; j<row; j++){
                    int a = i;
                    int b = j;
                    if(flipHorizontal) a = column-1-a;
                    if(flipVertical) b = row-1-b;
                    if(swapCoors){
                        int temp = a;
                        a = b;
                        b = temp;
                    }
                    int delay = 0;

                    float x = (float)a/(row-1);
                    float y = (float)b/(row-1);
                    if(type == 0){
                        delay = (int) (minDelay*x+maxDelay*(1-x));
                    }else if(type == 1){
                        delay = (int) (minDelay*(x+y)+maxDelay*(1-x+1-y))/2;
                    }else if(type == 2){
                        float diff = Math.abs(x-0.5f)*2;
                        delay = (int) (minDelay*diff+maxDelay*(1-diff));
                    }else if(type == 3){
                        float diff = Math.abs(x-0.5f)*2;
                        diff = 1-diff;
                        delay = (int) (minDelay*diff+maxDelay*(1-diff));
                    }else if(type == 4){
                        float diff = Math.abs(x-0.5f)+Math.abs(y-0.5f);
                        delay = (int) (minDelay*diff+maxDelay*(1-diff));
                    }else if(type == 5){
                        float diff = Math.abs(x-0.5f)+Math.abs(y-0.5f);
                        diff = 1-diff;
                        delay = (int) (minDelay*diff+maxDelay*(1-diff));
                    }else if(type == 6){
                        float diff = (x*row+y)/row;
                        delay = (int) (minDelay*diff+maxDelay*(1-diff));
                    }else if(type == 7){
                        float diff = r.nextFloat();
                        delay = (int) (minDelay*diff+maxDelay*(1-diff));
                    }
                    animateStart[i][j] = t+delay;
                }
            }

        }

        invalidate();
    }

    public void popSelected(Vector<BoardPosModel> poppedSelectedPos, boolean reject) {
        if(poppedSelectedPos == null) return;
        long time = System.currentTimeMillis();
        for(BoardPosModel pos:poppedSelectedPos){
            selected[pos.x][pos.y] = false;
            if(reject){
                rejected[pos.x][pos.y] = true;
                rejectedStart[pos.x][pos.y] = time;
            }
        }
        invalidate();
    }

    public void popSelected(boolean reject) {
        popSelected(selectedPosQueue.poll(), reject);
    }
    public void setServer(ServerThread server) {
        this.server = server;
    }
}
