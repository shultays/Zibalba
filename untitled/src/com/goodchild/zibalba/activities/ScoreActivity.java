package com.goodchild.zibalba.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.goodchild.zibalba.ClientConstants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.models.ClientModel;
import com.goodchild.zibalba.models.TableModel;
import com.goodchild.zibalba.widgets.ZSepiaImageView;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreActivity extends Activity {
    List<ClientModel> players;
    GridView playerList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Gson gson = new Gson();
        TableModel tableModel = gson.fromJson(getIntent().getStringExtra(ClientConstants.TABLE_MODEL_EXTRA), TableModel.class);


        Collections.sort(tableModel.players, new Comparator<ClientModel>() {
            @Override
            public int compare(ClientModel clientModel, ClientModel clientModel2) {
                return clientModel2.score-clientModel.score;
            }
        });

        ClientModel winner = tableModel.players.get(0);
        players = tableModel.players;
        players.remove(0);

        if(winner.picturePath != null && winner.picturePath.length() > 0){
            ((ZSepiaImageView)findViewById(R.id.winnerImage)).setImagePath(winner.picturePath);
        }else{
            ((ZSepiaImageView)findViewById(R.id.winnerImage)).setImageResource(ClientConstants.ICON_RESOURCES[winner.icon]);
        }

        ((TextView)findViewById(R.id.winnerName)).setText(winner.name);
        ((TextView)findViewById(R.id.winnerScore)).setText(winner.score + " points");

        playerList = (GridView) findViewById(R.id.playerList);
        playerList.setAdapter(new PlayerListAdapter(this, R.layout.widget_select_table_player, players));
    }

    private class PlayerListAdapter extends ArrayAdapter {

        public PlayerListAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int i, View convert, ViewGroup viewGroup) {
            RelativeLayout rl = (RelativeLayout) convert;
            if(rl == null){
                rl = new RelativeLayout(viewGroup.getContext());
                View view = new LinearLayout(viewGroup.getContext());
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.widget_player, (ViewGroup) view);
                view.findViewById(R.id.userscore).setVisibility(View.VISIBLE);
                view.setPadding(10, 10, 10, 10);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                rl.addView(view, params);
            }

            if(players.get(i).picturePath != null && players.get(i).picturePath.length() > 0){
                ((ZSepiaImageView)rl.findViewById(R.id.usericon)).setImagePath(players.get(i).picturePath);
            }else{
                ((ZSepiaImageView)rl.findViewById(R.id.usericon)).setImageResource(ClientConstants.ICON_RESOURCES[players.get(i).icon]);
            }
            ((TextView)rl.findViewById(R.id.username)).setText(players.get(i).name);
            ((TextView)rl.findViewById(R.id.userscore)).setText(players.get(i).score + "\n");
            return rl;
        }
    };

}
