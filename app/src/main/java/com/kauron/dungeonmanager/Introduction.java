package com.kauron.dungeonmanager;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;


public class Introduction extends ActionBarActivity {

    private EditText name, level;
    private EditText fue, con, des, sab, intel, car;
    private Spinner classSpinner, raceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_introduction);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        name = (EditText) findViewById(R.id.editNameIntro);
        name.requestFocus();
        level = (EditText) findViewById(R.id.editPxIntro);
        fue = (EditText) findViewById(R.id.FUE);
        con = (EditText) findViewById(R.id.CON);
        des = (EditText) findViewById(R.id.DES);
        sab = (EditText) findViewById(R.id.SAB);
        intel = (EditText) findViewById(R.id.INT);
        car = (EditText) findViewById(R.id.CAR);

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
        getMenuInflater().inflate(R.menu.menu_introduction, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(
                getApplicationContext(),
                R.string.message_no_back_button_intro,
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
                        Snackbar.with(getApplicationContext()).text(R.string.missing_info_error), this
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
        if (!this.car.getText().toString().isEmpty())
            car = Integer.parseInt(this.car.getText().toString());
        if (!this.fue.getText().toString().isEmpty())
            fue = Integer.parseInt(this.fue.getText().toString());
        if (!this.con.getText().toString().isEmpty())
            con = Integer.parseInt(this.con.getText().toString());
        if (!this.des.getText().toString().isEmpty())
            des = Integer.parseInt(this.des.getText().toString());
        if (!this.intel.getText().toString().isEmpty())
            intel = Integer.parseInt(this.intel.getText().toString());
        if (!this.sab.getText().toString().isEmpty())
            sab = Integer.parseInt(this.sab.getText().toString());

//        if(first) {
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
                    saveName += p.getString("player" + j, "").equals(saveName) ? "2" : "";
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
            } else {
                return false;
            }
//        } else {
//            if (!nameString.isEmpty()) ed.putString("playerName", nameString);
//            if (classInt != Player.NULL) ed.putInt("classInt", classInt);
//            if (raceInt != Player.NULL) ed.putInt("raceInt", raceInt);
//            if (pxInt != -1)  ed.putInt("px", pxInt);
//
//            if (fue != 0)       ed.putInt("fue", fue);
//            if (car != 0)       ed.putInt("car", car);
//            if (intel != 0)     ed.putInt("int", intel);
//            if (sab != 0)       ed.putInt("sab", sab);
//            if (con != 0)       ed.putInt("con", con);
//            if (des != 0)       ed.putInt("des", des);
//        }

        return true;
    }
}
