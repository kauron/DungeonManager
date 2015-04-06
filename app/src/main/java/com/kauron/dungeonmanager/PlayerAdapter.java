package com.kauron.dungeonmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

class PlayerAdapter extends ArrayAdapter<Player> {

    PlayerAdapter(Context context, Player[] players) {
        super(context, R.layout.player_row, players);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View mView = mInflater.inflate(R.layout.player_row, parent, false);

        final Player player = getItem(position);

        if ( player != null ) {
            ((TextView) mView.findViewById(R.id.name)).setText(player.getName());
            ((TextView) mView.findViewById(R.id.other)).setText(player.getClassName() + " " + player.getRaceName());
            ((TextView) mView.findViewById(R.id.level))
                    .setText(
                            getContext().getResources().getString(R.string.level) + " " + player.getLevel()
                    );

            ProgressBar pg = (ProgressBar) mView.findViewById(R.id.progressBar);
            pg.setMax(player.getMaxPg());
            pg.setProgress(player.getPg());
        }

        return mView;
    }


}
