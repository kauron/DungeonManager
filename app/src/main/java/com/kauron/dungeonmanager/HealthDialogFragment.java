package com.kauron.dungeonmanager;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class HealthDialogFragment extends DialogFragment {
    //TODO: convert to method and dialog, without class

    static HealthDialogFragment newInstance(int curativeEfforts) {
        HealthDialogFragment f = new HealthDialogFragment();
        Bundle args = new Bundle();
        args.putInt("curativeEfforts", curativeEfforts);
        f.setArguments(args);

        return f;
    }

    public interface HealthDialogListener {
        public void heal(DialogFragment dialog, boolean uses);
    }


    HealthDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (HealthDialogListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement HealthDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(com.ddns.kauron.dungeonmanager.R.string.new_energies_message)
                .setTitle(com.ddns.kauron.dungeonmanager.R.string.new_energies)
                .setPositiveButton(com.ddns.kauron.dungeonmanager.R.string.me, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.heal(HealthDialogFragment.this, true);
                    }
                })
                .setNegativeButton(com.ddns.kauron.dungeonmanager.R.string.other, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.heal(HealthDialogFragment.this, false);
                    }
                })
                .setNeutralButton(com.ddns.kauron.dungeonmanager.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
