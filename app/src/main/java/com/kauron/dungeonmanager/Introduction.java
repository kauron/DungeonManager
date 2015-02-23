package com.kauron.dungeonmanager;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class Introduction extends ActionBarActivity {

    EditText name, level;
    EditText fue, con, des, sab, intel, car;
    Spinner classSpinner, raceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(com.kauron.dungeonmanager.R.layout.activity_introduction);
        name = (EditText) findViewById(com.kauron.dungeonmanager.R.id.editNameIntro);
        level = (EditText) findViewById(com.kauron.dungeonmanager.R.id.editLevelIntro);

        fue = (EditText) findViewById(com.kauron.dungeonmanager.R.id.FUE);
        con = (EditText) findViewById(com.kauron.dungeonmanager.R.id.CON);
        des = (EditText) findViewById(com.kauron.dungeonmanager.R.id.DES);
        sab = (EditText) findViewById(com.kauron.dungeonmanager.R.id.SAB);
        intel = (EditText) findViewById(com.kauron.dungeonmanager.R.id.INT);
        car = (EditText) findViewById(com.kauron.dungeonmanager.R.id.CAR);

        classSpinner = (Spinner) findViewById(com.kauron.dungeonmanager.R.id.classSpinner);
        classSpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Player.classStrings
                )
        );

        raceSpinner = (Spinner) findViewById(com.kauron.dungeonmanager.R.id.raceSpinner);
        raceSpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        Player.raceStrings
                )
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.kauron.dungeonmanager.R.menu.menu_introduction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.kauron.dungeonmanager.R.id.action_save) {
            if(finished()) {
                this.finish();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        com.kauron.dungeonmanager.R.string.missing_info_error,
                        Toast.LENGTH_LONG
                ).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: fix this
    private boolean finished() {
        SharedPreferences p = getSharedPreferences("basics", MODE_PRIVATE);
        SharedPreferences.Editor ed = p.edit();
        String nameString = name.getText().toString();
        String classString = Player.classStrings[classSpinner.getSelectedItemPosition()];
        String raceString = Player.raceStrings[raceSpinner.getSelectedItemPosition()];

        int levelInt = 0;
        if (!level.getText().toString().isEmpty())
            levelInt = Integer.parseInt(level.getText().toString());

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

        if(getIntent().getExtras().getBoolean("first_time")) {
            if (
                    !nameString.isEmpty() &&
                    !classString.equals(Player.classStrings[0]) &&
                    !raceString.equals(Player.raceStrings[0]) &&
                    levelInt != 0 &&
                    car != 0 &&
                    fue != 0 &&
                    con != 0 &&
                    des != 0 &&
                    intel != 0 &&
                    sab != 0
            ) {
                //first save it all
                ed.putString("playerName", nameString);
                ed.putString("className", classString);
                ed.putString("raceName", raceString);
                ed.putInt("level", levelInt);

                ed.putInt("fue", fue);
                ed.putInt("car", car);
                ed.putInt("int", intel);
                ed.putInt("sab", sab);
                ed.putInt("con", con);
                ed.putInt("des", des);

                ed.putBoolean("saved", true);
            } else {
                return false;
            }
        } else {
            if (!nameString.isEmpty()) ed.putString("playerName", nameString);
            if (!classString.isEmpty()) ed.putString("className", classString);
            if (!raceString.isEmpty()) ed.putString("raceName", raceString);
            if (levelInt != 0)  ed.putInt("level", levelInt);

            if (fue != 0)       ed.putInt("fue", fue);
            if (car != 0)       ed.putInt("car", car);
            if (intel != 0)     ed.putInt("int", intel);
            if (sab != 0)       ed.putInt("sab", sab);
            if (con != 0)       ed.putInt("con", con);
            if (des != 0)       ed.putInt("des", des);
        }
        ed.apply();
        return true;
    }
}
