package com.kauron.dungeonmanager;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

class PlayerAdapter extends ArrayAdapter<Player> {

    PlayerAdapter(Context context, ArrayList<Player> players) {
        super(context, R.layout.player_row, players);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View mView = mInflater.inflate(R.layout.player_row, parent, false);

        final Player player = getItem(position);

        if ( player != null ) {
            ((TextView) mView.findViewById(R.id.name)).setText(player.getName());
            ((TextView) mView.findViewById(R.id.class_and_race)).setText(player.getClassName() + " " + player.getRaceName());
            ((TextView) mView.findViewById(R.id.levelText))
                    .setText(
                            getContext().getResources().getString(R.string.level) + " " + player.getLevel()
                    );
            int hp = player.getHp();
            int maxHp = player.getMaxHp();
            ProgressBar neg = (ProgressBar) mView.findViewById(R.id.negative_hp_bar);
            ProgressBar pos = (ProgressBar) mView.findViewById(R.id.positive_hp_bar);

            neg.setMax(maxHp / 2);
            pos.setMax(maxHp);

            neg.setProgress(hp < 0 ? -hp : 0);
            pos.setProgress(hp > 0 ?  hp : 0);

            int color = player.getStatusColor(getContext().getResources());
            neg.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            pos.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        return mView;
    }


}
