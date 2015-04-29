package com.kauron.dungeonmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class PowerAdapter extends ArrayAdapter<Power> {
    PowerAdapter(Context context, ArrayList<Power> powers) {
        super(context, R.layout.power_row, powers);
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View mView = mInflater.inflate(R.layout.power_row, parent, false);

        final Power attack = getItem(position);
        if ( attack != null ) {
            ((TextView) mView.findViewById(R.id.name)).setText(attack.getName());
            ((TextView) mView.findViewById(R.id.keywords)).setText(attack.getKeywords());
            ((TextView) mView.findViewById(R.id.frequency)).setText(attack.getFrequencyString());
            ((TextView) mView.findViewById(R.id.range_and_distance)).setText(attack.getRangeString() + " " + attack.getDistance());
            mView.setBackgroundColor(attack.getFreqColor(getContext()));
            if (attack.isUsed())
                mView.getBackground().setAlpha(0);
            else
                mView.getBackground().setAlpha((position % 2) * 50 + 205);
        }
        return mView;
    }
}
