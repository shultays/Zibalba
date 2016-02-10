package com.goodchild.zibalba.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.goodchild.zibalba.ClientConstants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.widgets.ZSepiaImageView;

/**
 * Created by Engin Mercan on 20.01.2014.
 */
public class TableSelectWidget extends RelativeLayout {
    TextView boardSize;
    TextView gameDuration;
    LinearLayout playerList;
    TextView maxPlayers;

    public TableSelectWidget(Context context) {
        super(context);
        initialize();
    }

    public TableSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TableSelectWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_select_table, this);

        boardSize = (TextView) findViewById(R.id.boardsize);
        gameDuration = (TextView) findViewById(R.id.timeLimit);
        playerList = (LinearLayout) findViewById(R.id.players);
        maxPlayers = (TextView) findViewById(R.id.maxPlayers);
    }

    public void setData(TableModel tableModel){
        boardSize.setText(tableModel.row + "x" + tableModel.column);
        gameDuration.setText(tableModel.gameDuration + " seconds");
        maxPlayers.setText(tableModel.maxPlayer + "");

        playerList.removeAllViews();
        for(int i=0; i<tableModel.players.size(); i++){
            RelativeLayout rl = new RelativeLayout(getContext());
            LayoutInflater.from(getContext()).inflate(R.layout.widget_select_table_player, rl);
            ((TextView)rl.findViewById(R.id.username)).setText(tableModel.players.get(i).name);

            if(tableModel.players.get(i).picturePath != null && tableModel.players.get(i).picturePath.length() > 0){
                ((ZSepiaImageView)rl.findViewById(R.id.usericon)).setImagePath(tableModel.players.get(i).picturePath);
            }else{
                ((ZSepiaImageView)rl.findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[tableModel.players.get(i).icon]);
            }


            playerList.addView(rl);
        }
    }
}
