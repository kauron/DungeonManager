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

    private EditText name, xp;
    private EditText[] atk = new EditText[7];
    private Spinner classSpinner, raceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        name = (EditText) findViewById(R.id.namePlayerEdit);
        name.requestFocus();
        xp = (EditText) findViewById(R.id.xpEdit);
        atk[Player.STR] = (EditText) findViewById(R.id.STR);
        atk[Player.CON] = (EditText) findViewById(R.id.CON);
        atk[Player.DEX] = (EditText) findViewById(R.id.DEX);
        atk[Player.WIS] = (EditText) findViewById(R.id.WIS);
        atk[Player.INT] = (EditText) findViewById(R.id.INT);
        atk[Player.CHA] = (EditText) findViewById(R.id.CHA);

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
        int position = getIntent().getExtras().getInt("player", -1);
        if ( position != -1 ) {
            Player p = new Player(getSharedPreferences("player" + position, MODE_PRIVATE));
            name.setText(p.getName());
            xp.setText(p.getXp());
            int[] attack = p.getAtk();
            for (int i = Player.STR; i < Player.CHA + 1; i++)
                atk[i].setText(attack[i]);
            classSpinner.setSelection(p.getClassInt());
            raceSpinner.setSelection(p.getRaceInt());
        }
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
        if (!xp.getText().toString().isEmpty())
            pxInt = Integer.parseInt(xp.getText().toString());
        else
            xp.setError(getString(R.string.required));

        int[] atkInts = new int[7];
        for (int i = Player.STR; i <= Player.CHA; i++)
            if (!atk[i].getText().toString().isEmpty())
                atkInts[i] = Integer.parseInt(atk[i].getText().toString());
        boolean validAtk = true;
        for (int i : atkInts)
            if (i == 0) {
                validAtk = false;
                break;
            }

        if (
                !nameString.isEmpty() &&
                        classInt != Player.NULL &&
                        raceInt != Player.NULL &&
                        pxInt != -1 &&
                        validAtk
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

            ed.putInt("str", atkInts[Player.STR]);
            ed.putInt("cha", atkInts[Player.CHA]);
            ed.putInt("int", atkInts[Player.INT]);
            ed.putInt("wis", atkInts[Player.WIS]);
            ed.putInt("con", atkInts[Player.CON]);
            ed.putInt("dex", atkInts[Player.DEX]);
            //TEMP
            ed.putBoolean("new", true);
            ed.apply();
            return true;
        } else {
            return false;
        }
    }
}
