package com.kauron.dungeonmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;


public class PlayerEditor extends ActionBarActivity {

    private EditText name, level;
    private EditText str, con, dex, wis, intel, cha;
    private Spinner classSpinner, raceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_player_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        name = (EditText) findViewById(R.id.namePlayerEdit);
        name.requestFocus();
        level = (EditText) findViewById(R.id.xpEdit);
        str = (EditText) findViewById(R.id.STR);
        con = (EditText) findViewById(R.id.CON);
        dex = (EditText) findViewById(R.id.DEX);
        wis = (EditText) findViewById(R.id.WIS);
        intel = (EditText) findViewById(R.id.INT);
        cha = (EditText) findViewById(R.id.CHA);

        classSpinner = (Spinner) findViewById(R.id.classSpinner);
        classSpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Player.CLASS_STRINGS
                )
        );

        raceSpinner = (Spinner) findViewById(R.id.raceSpinner);
        raceSpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Player.RACE_STRINGS
                )
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player_editor, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(
                getApplicationContext(),
                R.string.player_not_saved,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if(finished()) {
                finish();
            } else {
                SnackbarManager.show(
                        Snackbar.with(getApplicationContext()).text(R.string.fill_all_info), this
                );
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean finished() {
        String nameString = name.getText().toString().trim();
        int classInt = classSpinner.getSelectedItemPosition();
        int raceInt = raceSpinner.getSelectedItemPosition();

        int pxInt = -1;
        if (!level.getText().toString().isEmpty())
            pxInt = Integer.parseInt(level.getText().toString());

        int fue = 0, con = 0, des = 0, intel = 0, sab = 0, car = 0;
        if (!this.cha.getText().toString().isEmpty())
            car = Integer.parseInt(this.cha.getText().toString());
        if (!this.str.getText().toString().isEmpty())
            fue = Integer.parseInt(this.str.getText().toString());
        if (!this.con.getText().toString().isEmpty())
            con = Integer.parseInt(this.con.getText().toString());
        if (!this.dex.getText().toString().isEmpty())
            des = Integer.parseInt(this.dex.getText().toString());
        if (!this.intel.getText().toString().isEmpty())
            intel = Integer.parseInt(this.intel.getText().toString());
        if (!this.wis.getText().toString().isEmpty())
            sab = Integer.parseInt(this.wis.getText().toString());

        if (
                !nameString.isEmpty() &&
                        classInt != Player.NULL &&
                        raceInt != Player.NULL &&
                        pxInt != -1 &&
                        car != 0 &&
                        fue != 0 &&
                        con != 0 &&
                        des != 0 &&
                        intel != 0 &&
                        sab != 0
                ) {
            SharedPreferences p = getSharedPreferences(Welcome.PREFERENCES, MODE_PRIVATE);
            int i = p.getInt("players", 0);
            String saveName = nameString;
            for (int j = 0; j < i; j++) {
                if (p.getString("player" + j, "").equals(saveName))
                    saveName += "2";
            }
            p.edit().putString("player" + i, saveName).putInt("players", i + 1).apply();
            SharedPreferences.Editor ed = getSharedPreferences(saveName, MODE_PRIVATE).edit();

            //first save it all
            ed.putString("playerName", nameString);
            ed.putInt("classInt", classInt);
            ed.putInt("raceInt", raceInt);
            ed.putInt("px", pxInt);

            ed.putInt("fue", fue);
            ed.putInt("car", car);
            ed.putInt("int", intel);
            ed.putInt("sab", sab);
            ed.putInt("con", con);
            ed.putInt("des", des);
            //TEMP
            ed.putBoolean("new", true);
            ed.apply();
            return true;
        } else {
            return false;
        }
    }
}
