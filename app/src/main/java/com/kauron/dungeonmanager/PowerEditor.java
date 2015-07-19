package com.kauron.dungeonmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;


public class PowerEditor extends ActionBarActivity {

    private EditText [] edits = new EditText[6];
    private Spinner [] spinners = new Spinner[5];

    private String[] strings = new String[6];
    private int[] ints = new int[5];

    private String originalName;
    private int power;
    private SharedPreferences p;

    private boolean changed;

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.save_changes);
        alert.setMessage(R.string.progress_lost);

        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                save();
            }
        });

        alert.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PowerEditor.super.onBackPressed();
            }
        });

        alert.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_editor);
        //identify toolbar, set it as ActionBar and add the back button to the last activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        changed = false;

        power = getIntent().getIntExtra("power", -1);

        p = getSharedPreferences(getIntent().getStringExtra("player"), MODE_PRIVATE);

        //EditText
        edits[0] = (EditText) findViewById(R.id.nameEdit);
        edits[0].requestFocus();
        edits[1] = (EditText) findViewById(R.id.keywordsEdit);
        edits[2] = (EditText) findViewById(R.id.impactEdit);
        edits[3] = (EditText) findViewById(R.id.distanceNumEdit);
        edits[4] = (EditText) findViewById(R.id.objectiveEdit);
        edits[5] = (EditText) findViewById(R.id.otherEdit);

        //Spinners
        spinners[0] = (Spinner) findViewById(R.id.freqSpinner);
        spinners[0].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Power.FREQ
                )
        );
        spinners[1] = (Spinner) findViewById(R.id.rangeSpinner);
        spinners[1].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Power.RANGES
                )
        );
        spinners[2] = (Spinner) findViewById(R.id.atkSpinner);
        spinners[2].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Power.ATK
                )
        );
        spinners[3] = (Spinner) findViewById(R.id.defSpinner);
        spinners[3].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Power.DEF
                )
        );
        spinners[4] = (Spinner) findViewById(R.id.actionSpinner);
        spinners[4].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Power.ACTIONS
                )
        );

        if (power != -1) {
            SharedPreferences powerPrefs = getSharedPreferences(p.getString("power" + power, ""), MODE_PRIVATE);
            for ( int i = 0; i < spinners.length; i++ )
                spinners[i].setSelection(powerPrefs.getInt("i" + i, 0));
            for ( int i = 0; i < edits.length; i++ )
                edits[i].setText(powerPrefs.getString("s" + i, ""));
            originalName = edits[0].getText().toString();
        }

        //TODO: detect changes in edits and spinners
//        for (int i = 0; i < edits.length; i++) {
//            edits[i].;
//        }

    }

    public void save() {
        for (int i = 0; i < strings.length; i++)
            strings[i] = edits[i].getText().toString();
        for (int i = 0; i < spinners.length; i++)
            ints[i] = spinners[i].getSelectedItemPosition();

        if (strings[0].length() == 0) {
            edits[0].setError(getString(R.string.required));
            return;
        }

        int powers = p.getInt("powers", 0);

        String saveName;
        if ( originalName == null ) {
            saveName = strings[0];
            for (int i = 0; i < powers; i++) {
                if (saveName.equals(p.getString("power" + power, ""))) {
                    SnackbarManager.show(Snackbar
                            .with(this)
                            .text(R.string.power_same_name)
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
                    return;
                }
            }
            p.edit().putString("power" + powers, saveName)
                    .putInt("powers", powers + 1)
                    .apply();
        } else {
            saveName = originalName;
        }

        SharedPreferences.Editor ed = getSharedPreferences( saveName, MODE_PRIVATE).edit();

        for (int i = 0; i < strings.length; i++)
            if (strings[i].isEmpty())
                ed.remove("s" + i);
            else ed.putString("s" + i, strings[i]);
        for (int i = 0; i < ints.length; i++)
            ed.putInt("i" + i, ints[i]);
        ed.apply();

        finish();
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_power_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
