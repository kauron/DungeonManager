package com.kauron.dungeonmanager;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;


public class PlayerEditor extends ActionBarActivity {

    private EditText name, xp;
    private EditText[] editAtk = new EditText[7];
    private int[] def;
    private int position;
    private Player player;
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
        editAtk[Player.STR] = (EditText) findViewById(R.id.STR);
        editAtk[Player.CON] = (EditText) findViewById(R.id.CON);
        editAtk[Player.DEX] = (EditText) findViewById(R.id.DEX);
        editAtk[Player.WIS] = (EditText) findViewById(R.id.WIS);
        editAtk[Player.INT] = (EditText) findViewById(R.id.INT);
        editAtk[Player.CHA] = (EditText) findViewById(R.id.CHA);

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
        position = getIntent().getIntExtra("player", -1);
        if ( position != -1 ) {
            player = new Player(
                    getSharedPreferences(
                            getSharedPreferences(Welcome.PREFERENCES, MODE_PRIVATE)
                                    .getString("player" + position, ""),
                            MODE_PRIVATE)
            );
            name.setText(player.getName());
            xp.setText(String.valueOf(player.getXp()));
            int[] attack = player.getAtk();
            for (int i = Player.STR; i < Player.CHA + 1; i++)
                editAtk[i].setText(String.valueOf(attack[i]));
            classSpinner.setSelection(player.getClassInt());
            raceSpinner.setSelection(player.getRaceInt());
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
            if (!editAtk[i].getText().toString().isEmpty())
                atkInts[i] = Integer.parseInt(editAtk[i].getText().toString());
        boolean validAtk = true;
        for (int i = Player.STR; i <= Player.CHA; i++)
            if (atkInts[i] == 0) {
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
            boolean isNew = true;
            if (player == null) {
                for (int j = 0; j < i; j++) {
                    if (nameString.equals(p.getString("player" + j, ""))) {
                        if (player == null) {
                            Toast.makeText(
                                    this,
                                    "There is another player with that name, edit that player or change the name",
                                    Toast.LENGTH_LONG
                            ).show();
                            return false;
                        }
                        isNew = false;
                        break;
                    }
                }
            } else {
                isNew = false;
                if (nameString != player.getName()) {
                    getSharedPreferences(player.getName(), MODE_PRIVATE).edit().clear().apply();
                    p.edit().putString("player" + position, nameString).apply();
                }
            }

            if (isNew) p.edit().putString("player" + i, nameString).putInt("players", i + 1).apply();
            SharedPreferences.Editor ed = getSharedPreferences(nameString, MODE_PRIVATE).edit();

            //first save it all
            ed.putString("playerName", nameString);
            ed.putInt("classInt", classInt);
            ed.putInt("raceInt", raceInt);
            ed.putInt("px", pxInt);

            ed.putInt("fue", atkInts[Player.STR]);
            ed.putInt("car", atkInts[Player.CHA]);
            ed.putInt("int", atkInts[Player.INT]);
            ed.putInt("sab", atkInts[Player.WIS]);
            ed.putInt("con", atkInts[Player.CON]);
            ed.putInt("des", atkInts[Player.DEX]);

            if (def != null) {
                ed.putInt("ac", def[Player.AC]);
                ed.putInt("fort", def[Player.FORT]);
                ed.putInt("ref", def[Player.REF]);
                ed.putInt("will", def[Player.WILL]);
            }

            ed.apply();
            return true;
        } else {
            return false;
        }
    }

    public void onDefenseClick(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.defense_editor);
        if (player != null) {
            ((EditText) dialog.findViewById(R.id.ac)).setText(
                    String.valueOf(player.getDef()[Player.AC]));
            ((EditText) dialog.findViewById(R.id.fort)).setText(
                    String.valueOf(player.getDef()[Player.FORT]));
            ((EditText) dialog.findViewById(R.id.ref)).setText(
                    String.valueOf(player.getDef()[Player.REF]));
            ((EditText) dialog.findViewById(R.id.will)).setText(
                    String.valueOf(player.getDef()[Player.WILL]));
        }
        dialog.findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                def = new int[7];
                EditText ac = (EditText) dialog.findViewById(R.id.ac);
                if (!ac.getText().toString().isEmpty())
                    def[Player.AC] = Integer.parseInt(ac.getText().toString());
                EditText fort = (EditText) dialog.findViewById(R.id.fort);
                if (!ac.getText().toString().isEmpty())
                    def[Player.FORT] = Integer.parseInt(fort.getText().toString());
                EditText ref = (EditText) dialog.findViewById(R.id.ref);
                if (!ac.getText().toString().isEmpty())
                    def[Player.REF] = Integer.parseInt(ref.getText().toString());
                EditText will = (EditText) dialog.findViewById(R.id.will);
                if (!ac.getText().toString().isEmpty())
                    def[Player.WILL] = Integer.parseInt(will.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
