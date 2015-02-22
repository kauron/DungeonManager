package com.kauron.dungeonmanager;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity
                        implements HealthDialogFragment.HealthDialogListener{

    public static final int CURRENT_PG = 1, NULL = 0;

    public Player player;
    private boolean undo;
    private int undoObject, undoPreviousValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ddns.kauron.dungeonmanager.R.layout.activity_main);
        undo = false;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.ddns.kauron.dungeonmanager.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(com.ddns.kauron.dungeonmanager.R.id.action_undo).setVisible(undo);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.ddns.kauron.dungeonmanager.R.id.action_cure) {
            showHealthDialog();
            return true;
        } else if (id == com.ddns.kauron.dungeonmanager.R.id.action_edit_basics) {
            //TODO: try this startChildActivity()
            SharedPreferences p = getSharedPreferences("basics", MODE_PRIVATE);
            Intent intent = new Intent(this, Introduction.class);
            startActivity(intent.putExtra(
                    "first_time",
                    !p.getBoolean("saved", false)
            ));
            restoreData();
            return true;
        } else if (id == com.ddns.kauron.dungeonmanager.R.id.action_undo) {
            undo();
            return true;
        } else if (id == com.ddns.kauron.dungeonmanager.R.id.action_reset) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(com.ddns.kauron.dungeonmanager.R.string.reset_confirmation_title));
            alert.setMessage(getString(com.ddns.kauron.dungeonmanager.R.string.reset_confirmation));
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(
                            getApplicationContext(),
                            com.ddns.kauron.dungeonmanager.R.string.message_reset,
                            Toast.LENGTH_LONG
                    ).show();
                    getSharedPreferences("basics", MODE_PRIVATE).edit().clear().apply();
                    restoreData();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
//        } else if (id == R.id.action_save) {
//            saveData();
//        } else if (id == R.id.action_load) {
//            restoreData();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showHealthDialog(){
        DialogFragment dialog = HealthDialogFragment.newInstance(player.getCurativeEfforts());
        dialog.show(getFragmentManager(), "HealthDialogFragment");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("UTIL", "pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("UTIL", "resume");
        restoreData();
        healthStatusCheck();
        updateCurativeString();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("UTIL", "stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("UTIL", "destroy");
    }

    @Override
    public void heal(DialogFragment dialog, boolean uses) {
        int hasCured = player.recoverPg(Player.USE_CURATIVE_EFFORT, uses);
        if (hasCured == Player.NOT_CURED) {
            Toast.makeText(
                    getApplicationContext(),
                    com.ddns.kauron.dungeonmanager.R.string.no_curative_efforts_error,
                    Toast.LENGTH_LONG
            ).show();
        } else {
            if(hasCured == Player.MAXED){
                Toast.makeText(
                        getApplicationContext(),
                        com.ddns.kauron.dungeonmanager.R.string.maxed_curative,
                        Toast.LENGTH_LONG
                ).show();
            }
            getSharedPreferences("basics", MODE_PRIVATE).edit()
                    .putInt("pg", player.getPg())
                    .putInt("curativeEfforts", player.getCurativeEfforts())
                    .apply();
            updateCurativeString();
            healthStatusCheck();
        }
    }

    public void damage(final View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(com.ddns.kauron.dungeonmanager.R.string.suffer_damage));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(com.ddns.kauron.dungeonmanager.R.string.suffer_damage_hint);

        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Button pg = (Button) findViewById(com.ddns.kauron.dungeonmanager.R.id.pgCurrent);
                try {
                    int preValue = Integer.parseInt(pg.getText().toString());
                    int damage = Integer.parseInt(input.getText().toString());
                    player.losePg(damage);
                    pg.setText(
                            String.valueOf(player.getPg())
                    );
                    //finished correctly, then apply values to undo's
                    undo = true;
                    undoPreviousValue = preValue;
                    undoObject = CURRENT_PG;
                    getSharedPreferences("basics", MODE_PRIVATE).edit()
                            .putInt("pg", player.getPg())
                            .apply();
                    healthStatusCheck();
                    invalidateOptionsMenu();
                } catch (Exception e) {}
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void healthStatusCheck() {
        int status = player.getState();
        int lastState = player.getLastState();
        Button pg = (Button) findViewById(com.ddns.kauron.dungeonmanager.R.id.pgCurrent);
        pg.setText(String.valueOf(player.getPg()));
        if (status == Player.MUERTO) {
            pg.setTextColor(Color.BLACK);
            pg.setBackgroundColor(Color.RED);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(com.ddns.kauron.dungeonmanager.R.string.reset_confirmation_title));
            alert.setMessage(getString(com.ddns.kauron.dungeonmanager.R.string.reset_confirmation));
            alert.setPositiveButton(com.ddns.kauron.dungeonmanager.R.string.action_undo, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    undo();
                }
            });

            alert.setNegativeButton(com.ddns.kauron.dungeonmanager.R.string.die, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(
                            getApplicationContext(),
                            com.ddns.kauron.dungeonmanager.R.string.message_death,
                            Toast.LENGTH_LONG
                    ).show();
                    getSharedPreferences("basics", MODE_PRIVATE).edit().clear().apply();
                    restoreData();
                }
            });

            alert.show();
        } else if (status == Player.DEBILITADO) {
            pg.setBackgroundColor(android.R.drawable.btn_default);
            pg.setTextColor(Color.RED);
            if(lastState != Player.SAME) {
                Toast.makeText(
                        getApplicationContext(),
                        com.ddns.kauron.dungeonmanager.R.string.state_changed_debilitado,
                        Toast.LENGTH_LONG
                ).show();
            }
        } else if (status == Player.MALHERIDO) {
            pg.setBackgroundColor(android.R.drawable.btn_default);
            pg.setTextColor(Color.YELLOW);
            if(lastState != Player.SAME) {
                Toast.makeText(
                        getApplicationContext(),
                        com.ddns.kauron.dungeonmanager.R.string.state_changed_malherido,
                        Toast.LENGTH_LONG
                ).show();
            }
        } else {
            pg.setTextColor(getResources().getColor(
                    com.ddns.kauron.dungeonmanager.R.color.abc_primary_text_material_dark
            ));
            pg.setBackgroundColor(android.R.drawable.btn_default);
        }
    }


    private void restoreData(){
        SharedPreferences p = getSharedPreferences("basics", MODE_PRIVATE);
        //restore state
        if (!p.getBoolean("saved", false)) {
            Intent intent = new Intent(this, Introduction.class);
            startActivity(intent.putExtra(
                    "first_time",
                    !p.getBoolean("saved", false)
            ));
        }
        if(player == null) {
            player = new Player(
                    p.getString("playerName", getString(com.ddns.kauron.dungeonmanager.R.string.adventurer_name)),
                    p.getString("className", getString(com.ddns.kauron.dungeonmanager.R.string.class_name)),
                    p.getString("raceName", getString(com.ddns.kauron.dungeonmanager.R.string.race_name)),
                    p.getInt("level", 1),
                    new int[] {
                            p.getInt("fue", 10),
                            p.getInt("con", 10),
                            p.getInt("des", 10),
                            p.getInt("int", 10),
                            p.getInt("sab", 10),
                            p.getInt("car", 10),
                    },
                    new int[18],
                    new Power[4]);
        } else {
            player.setName(p.getString("playerName", getString(com.ddns.kauron.dungeonmanager.R.string.adventurer_name)));
            player.setClassName(p.getString("className", getString(com.ddns.kauron.dungeonmanager.R.string.class_name)));
            player.setRaceName(p.getString("raceName", getString(com.ddns.kauron.dungeonmanager.R.string.race_name)));
            player.setLevel(p.getInt("level", 1));
            //TODO: restore pg
            player.setAtk(new int[]{
                    p.getInt("fue", 10),
                    p.getInt("con", 10),
                    p.getInt("des", 10),
                    p.getInt("int", 10),
                    p.getInt("sab", 10),
                    p.getInt("car", 10),
            });
            healthStatusCheck();
            updateCurativeString();
        }
        //set restored values to the respective fields
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.nameText)).setText(player.getName());
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.raceText)).setText(player.getRaceName());
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.classText)).setText(player.getClassName());
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.lvl)).setText(String.valueOf(player.getLevel()));

        ((Button) findViewById(com.ddns.kauron.dungeonmanager.R.id.pgCurrent)).setText(String.valueOf(player.getPg()));

        //attacks
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.FUE)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.FUE) + ":" + player.getFue()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.CON)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.CON) + ":" + player.getCon()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.DES)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.DES) + ":" + player.getDes()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.INT)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.INT) + ":" + player.getInt()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.SAB)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.SAB) + ":" + player.getSab()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.CAR)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.CAR) + ":" + player.getCar()
        );

        //defenses
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.CA)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.CA) + ": " + player.getCa()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.FORT)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.FORT) + ": " + player.getFort()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.REF)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.REF) + ": " + player.getRef()
        );
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.VOL)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.VOL) + ": " + player.getVol()
        );


    }

//    private void saveData() {
//        getSharedPreferences("basics", MODE_PRIVATE).edit()
//                .putInt("level", player.getLevel())
//                .putInt("maxPg", player.getMaxPg())
//                .putInt("pg", player.getPg())
//                .putInt("maxCurativeEfforts", player.getMaxCurativeEfforts())
//                .putInt("curativeEfforts", player.getCurativeEfforts())
//                .apply();
//    }

    private void updateCurativeString() {
        ((TextView) findViewById(com.ddns.kauron.dungeonmanager.R.id.curativeEffortsText)).setText(
                getString(com.ddns.kauron.dungeonmanager.R.string.curative_display_text1) + " " +
                        player.getCurativeEfforts() + " " +
                        getString(com.ddns.kauron.dungeonmanager.R.string.curative_display_text2) + " " +
                        player.getMaxCurativeEfforts() + " " +
                        getString(com.ddns.kauron.dungeonmanager.R.string.curative_display_text3)
        );
    }

    private void undo() {
        String message = "";
        if(undoObject == CURRENT_PG){
            ((Button) findViewById(com.ddns.kauron.dungeonmanager.R.id.pgCurrent)).setText(String.valueOf(undoPreviousValue));
            player.setPg(undoPreviousValue);
            undoObject = NULL;
            message = getString(com.ddns.kauron.dungeonmanager.R.string.action_undo_current_pg);
        }
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
        undo = false;
        invalidateOptionsMenu();
    }
}