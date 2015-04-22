package com.kauron.dungeonmanager;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class PowerEditor extends ActionBarActivity {

    public static final String NAME="name", FREQ="freq", KEYWORDS="keywords", RANGE="range", DISTANCE="distance";

    private EditText [] edits = new EditText[5];
    private Spinner [] spinners = new Spinner[5];

    private String[] strings = new String[5];
    private int[] ints = new int[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_editor);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));



        //EditText
        edits[0] = (EditText) findViewById(R.id.nameEdit);
        edits[0].requestFocus();
        edits[1] = (EditText) findViewById(R.id.keywordsEdit);
        edits[2] = (EditText) findViewById(R.id.impactEdit);
        edits[3] = (EditText) findViewById(R.id.distanceNumEdit);
        edits[4] = (EditText) findViewById(R.id.objectiveEdit);

        //Spinners
        spinners[0] = (Spinner) findViewById(R.id.freqSpinner);
        spinners[0].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.freq_array)
                )
        );
        spinners[1] = (Spinner) findViewById(R.id.rangeSpinner);
        spinners[1].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.ranges_array)
                )
        );
        spinners[2] = (Spinner) findViewById(R.id.atkSpinner);
        spinners[2].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.atk_array)
                )
        );
        spinners[3] = (Spinner) findViewById(R.id.defSpinner);
        spinners[3].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.def_array)
                )
        );
        spinners[4] = (Spinner) findViewById(R.id.actionTypeSpinner);
        spinners[4].setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.actions_array)
                )
        );

    }


    public void saveClick(View view) {
        //TODO: change strings per resources
        boolean readyToSave = true;

        for ( int i = 0; i < edits.length; i++ )
            if ( edits[i].getText().toString().trim().length() == 0 ) {
                edits[i].setError("This field is required");
                readyToSave = false;
            } else {
                strings[i] = edits[i].getText().toString().trim();
            }


        for ( int i = 0; i < spinners.length; i++)
            if ( spinners[i].getSelectedItemPosition() == 0 ) {
                spinners[i].setBackgroundColor(getResources().getColor(R.color.red));
                readyToSave = false;
            } else {
                ints[i] = spinners[i].getSelectedItemPosition();
                spinners[i].setBackgroundColor(getResources().getColor(R.color.green));
            }

        if ( readyToSave ) {
            String player = getIntent().getStringExtra("player");
            SharedPreferences p = getSharedPreferences(player, MODE_PRIVATE);

            int powers = p.getInt("powers", 0);

            for (int i = 0; i < powers; i++) {
                if ( p.getString("power" + i, "") == strings[0] ) {
                    edits[0].setError("This power has already been defined. Use another name.");
                    return;
                }
            }

            p.edit().putString("power" + powers, strings[0])
                    .putInt("powers", powers + 1)
                    .apply();

            SharedPreferences.Editor ed = getSharedPreferences(strings[0], MODE_PRIVATE).edit();

            for (int i = 0; i < strings.length; i++)
                ed.putString("s" + i, strings[i]);
            for (int i = 0; i < ints.length; i++)
                ed.putInt("i" + i, ints[i]);
            ed.apply();

            finish();
        }
    }
}
