package com.ddns.kauron.dungeonmanager;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class HealthDialogFragment extends DialogFragment {

    static HealthDialogFragment newInstance(int curativeEfforts) {
        HealthDialogFragment f = new HealthDialogFragment();
        Bundle args = new Bundle();
        args.putInt("curativeEfforts", curativeEfforts);
        f.setArguments(args);

        return f;
    }

    public interface HealthDialogListener {
        public void curativeEffort(DialogFragment dialog, boolean uses);
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
        String message = getString(R.string.new_energies1) +
                " " + getArguments().getInt("curativeEfforts") + " " +
                getString(R.string.new_energies2);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(R.string.new_energies_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.curativeEffort(HealthDialogFragment.this, true);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.curativeEffort(HealthDialogFragment.this, false);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
