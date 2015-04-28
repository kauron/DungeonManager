package com.kauron.dungeonmanager;

import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Color;
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
            ((TextView) mView.findViewById(R.id.other)).setText(player.getClassName() + " " + player.getRaceName());
            ((TextView) mView.findViewById(R.id.level))
                    .setText(
                            getContext().getResources().getString(R.string.level) + " " + player.getLevel()
                    );
            int pg = player.getPg();
            int maxPg = player.getMaxPg();
            ProgressBar neg = (ProgressBar) mView.findViewById(R.id.negPgBar);
            ProgressBar pos = (ProgressBar) mView.findViewById(R.id.pgBar);

            neg.setMax(maxPg / 2);
            pos.setMax(maxPg);

            neg.setProgress(pg < 0 ? -pg : 0);
            pos.setProgress(pg > 0 ?  pg : 0);

            int color = player.getStatusColor(getContext());
            neg.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            pos.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        return mView;
    }


}
