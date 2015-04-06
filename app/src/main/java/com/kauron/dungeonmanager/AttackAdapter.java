package com.kauron.dungeonmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

class AttackAdapter extends ArrayAdapter<Power> {
    AttackAdapter(Context context, Power[] powers) {
        super(context, R.layout.attack_row, powers);
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View mView = mInflater.inflate(R.layout.attack_row, parent, false);

        final Power attack = getItem(position);

        ((TextView) mView.findViewById(R.id.name)).setText(attack.getName());
        ((TextView) mView.findViewById(R.id.keywords)).setText(attack.getKeywords());
        ((TextView) mView.findViewById(R.id.frequency)).setText(attack.getFrequencyString());
        ((TextView) mView.findViewById(R.id.extra)).setText(attack.getRangeString() + " " + attack.getDistance());
        final AttackAdapter current = this;
        ((ImageView) mView.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SnackbarManager.show(
                        Snackbar.with(getContext()).text("¿Quieres borrarlo?").actionLabel("Sí").actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                current.remove(attack);
                            }
                        })
                );
                //TODO: convert text to resource
            }
        });

        return mView;
    }
}
