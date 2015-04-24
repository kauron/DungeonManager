package com.kauron.dungeonmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.File;

class AttackAdapter extends ArrayAdapter<Power> {
    AttackAdapter(Context context, Power[] powers) {
        super(context, R.layout.attack_row, powers);
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View mView = mInflater.inflate(R.layout.attack_row, parent, false);

        final Power attack = getItem(position);
        if ( attack != null ) {
            ((TextView) mView.findViewById(R.id.name)).setText(attack.getName());
            ((TextView) mView.findViewById(R.id.keywords)).setText(attack.getKeywords());
            ((TextView) mView.findViewById(R.id.frequency)).setText(attack.getFrequencyString());
            ((TextView) mView.findViewById(R.id.extra)).setText(attack.getRangeString() + " " + attack.getDistance());
            mView.setBackgroundColor(attack.getFreqColor(getContext()));
            if (attack.isUsed())
                mView.getBackground().setAlpha(0);
            else
                mView.getBackground().setAlpha((position % 2) * 127 + 128);
        }
        return mView;
    }
}
