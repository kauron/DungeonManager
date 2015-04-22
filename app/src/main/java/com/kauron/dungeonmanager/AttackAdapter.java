package com.kauron.dungeonmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
            final AttackAdapter current = this;
            mView.findViewById(R.id.delete);
//            mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SnackbarManager.show(
//                            Snackbar.with(getContext()).text("¿Quieres borrarlo?").actionLabel("Sí").actionListener(new ActionClickListener() {
//                                @Override
//                                public void onActionClicked(Snackbar snackbar) {
//                                    //delete the item
//                                    String name = p.getString("power" + position, "");
//                                    if (name != null && !name.isEmpty()) {
//                                        getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply();
//                                        Log.d("Tag", activity.getApplicationContext().getFilesDir().getParent()
//                                                + File.separator + "shared_prefs" + File.separator + name + ".xml");
//                                        try {
//                                            if (!new File(activity.getApplicationContext().getFilesDir().getParent()
//                                                    + File.separator + "shared_prefs" + File.separator + name + ".xml").delete())
//                                                throw new Exception();
//                                        } catch (Exception e) {
//                                            Toast.makeText(getContext(), "Error deleting player files", Toast.LENGTH_SHORT).show();
//                                        }
//                                        int max = p.getInt("powers", 0);
//                                        SharedPreferences.Editor ed = p.edit();
//                                        for (int i = position; i < max - 1; i++)
//                                            ed.putString("power" + i, p.getString("power" + (i + 1), "max"));
//                                        ed.putInt("powers", max - 1).apply();
//                                        load();
//                                        ed.remove("power" + (max - 1)).apply();
//                                    }
//                                }
//
//                                ));
//                                //TODO: convert text to resource
//                            }
//                }
//
//                );
//            };
        }
        return mView;
    }
}
