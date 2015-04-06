package com.kauron.dungeonmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

class AttackAdapter extends ArrayAdapter<Power> {
    AttackAdapter(Context context, Power[] powers) {
        super(context, R.layout.attack_row, powers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View mView = mInflater.inflate(R.layout.attack_row, parent, false);

        Power attack = getItem(position);
        return mView;
    }
}
