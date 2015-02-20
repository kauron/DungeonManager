package com.ddns.kauron.dungeonmanager;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class Introduction extends ActionBarActivity {

    EditText name, className, raceName, level, maxPg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_introduction);
        name = (EditText) findViewById(R.id.editNameIntro);
        className = (EditText) findViewById(R.id.editClassIntro);
        raceName = (EditText) findViewById(R.id.editRaceIntro);
        level = (EditText) findViewById(R.id.editLevelIntro);
        maxPg = (EditText) findViewById(R.id.editMaxPgIntro);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_introduction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_finish) {
            if(finished()) {
                this.finish();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.missing_info_error,
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
        String classString = className.getText().toString();
        String raceString = raceName.getText().toString();
        int levelInt = 0, maxPgInt = 0;
        try {
            levelInt = Integer.parseInt(level.getText().toString());
            maxPgInt = Integer.parseInt(level.getText().toString());
        } catch (Exception e) {}
        if(getIntent().getExtras().getBoolean("first_time")) {
            if (!nameString.isEmpty() &&
                    !classString.isEmpty() &&
                    !raceString.isEmpty() &&
                    levelInt != 0 &&
                    maxPgInt != 0) {
                //first save it all
                ed.putString("playerName", nameString);
                ed.putString("className", classString);
                ed.putString("raceName", raceString);
                if(p.getInt("pg", Integer.MIN_VALUE) == Integer.MIN_VALUE)
                    ed.putInt("pg", maxPgInt);
                ed.putInt("level", levelInt);
                ed.putInt("maxPg", maxPgInt);
                ed.putBoolean("saved", true);
                ed.apply();
                return true;
            } else {
                return false;
            }
        } else {
            if(!nameString.isEmpty()) ed.putString("playerName", nameString);
            if(!classString.isEmpty()) ed.putString("className", classString);
            if(!raceString.isEmpty()) ed.putString("raceName", raceString);
            if(levelInt != 0) ed.putInt("level", levelInt);
            if(maxPgInt != 0) ed.putInt("maxPg", maxPgInt);
            ed.apply();
            return true;
        }
    }
}
