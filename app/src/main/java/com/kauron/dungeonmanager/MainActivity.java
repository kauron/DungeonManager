package com.kauron.dungeonmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity{

    public static final int CURRENT_PG = 1, NULL = 0;

    public Player player;
    private boolean undo;
    private int undoObject, undoPreviousValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        undo = false;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.action_undo).setVisible(undo);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cure) {
            if(player.getMaxPg() <= player.getPg()){
                Toast.makeText(
                        getApplicationContext(),
                        R.string.maxed_curative,
                        Toast.LENGTH_LONG
                ).show();
            } else {
                healDialog();
            }
            return true;
        } else if (id == R.id.action_edit_basics) {
            //TODO: try this startChildActivity()
            SharedPreferences p = getSharedPreferences("basics", MODE_PRIVATE);
            Intent intent = new Intent(this, Introduction.class);
            startActivity(intent.putExtra(
                    "first_time",
                    !p.getBoolean("saved", false)
            ));
            restoreData();
            return true;
        } else if (id == R.id.action_undo) {
            undo();
            return true;
        } else if (id == R.id.action_reset) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.reset_confirmation_title);
            alert.setMessage(R.string.reset_confirmation);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.message_reset,
                            Toast.LENGTH_LONG
                    ).show();
                    getSharedPreferences("basics", MODE_PRIVATE).edit().clear().apply();
                    restoreData();
                }
            });
            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
            return true;
        } else if (id == R.id.action_time_encounter_end) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.px_awarded_title);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint(R.string.px_awarded_hint);
            alert.setCancelable(false);
            alert.setView(input);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (player.addPx(Integer.parseInt(input.getText().toString()))) {
                            //levelUp
                            //TODO: update defenses
                            //TODO: add attack points when necessary
                            //TODO: update currentPg button
                            player.setMaxPgOnLevelUp();
                            ((TextView) findViewById(R.id.lvl)).setText(
                                    String.valueOf(player.getLevel())
                            );
                        }
                    } catch(Exception e) {
                        Toast.makeText(
                                getApplicationContext(),
                                R.string.message_no_px,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            });
            alert.show();
            return true;
        } else if (id == R.id.action_download) {
            //TODO: create self-updater
            Toast.makeText(
                    getApplicationContext(),
                    "This function is not ready yet",
                    Toast.LENGTH_LONG
            ).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreData();
        healthStatusCheck();
        updateCurativeString();
    }

    public void heal(boolean usesEffort) {
        int hasCured = player.recoverPg(Player.USE_CURATIVE_EFFORT, usesEffort);
        if (hasCured == Player.NOT_CURED) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.no_curative_efforts_error,
                    Toast.LENGTH_LONG
            ).show();
        } else {
            if(hasCured == Player.MAXED){
                Toast.makeText(
                        getApplicationContext(),
                        R.string.maxed_curative,
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

    public void healDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.new_energies_message)
                .setTitle(R.string.new_energies)
                .setPositiveButton(R.string.me, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        heal(true);
                    }
                })
                .setNegativeButton(R.string.other, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        heal(false);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alert.show();
    }

    public void damage(final View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.suffer_damage);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.suffer_damage_hint);

        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Button pg = (Button) findViewById(R.id.pgCurrent);
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

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void healthStatusCheck() {
        int status = player.getState();
        int lastState = player.getLastState();
        Button pg = (Button) findViewById(R.id.pgCurrent);
        pg.setText(String.valueOf(player.getPg()));
        if (status == Player.MUERTO) {
            pg.setTextColor(Color.BLACK);
            pg.setBackgroundColor(Color.RED);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.reset_confirmation_title));
            alert.setMessage(getString(R.string.reset_confirmation));
            alert.setPositiveButton(R.string.action_undo, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    undo();
                }
            });

            alert.setNegativeButton(R.string.die, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.message_death,
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
                        R.string.state_changed_debilitado,
                        Toast.LENGTH_LONG
                ).show();
            }
        } else if (status == Player.MALHERIDO) {
            pg.setBackgroundColor(android.R.drawable.btn_default);
            pg.setTextColor(Color.YELLOW);
            if(lastState != Player.SAME) {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.state_changed_malherido,
                        Toast.LENGTH_LONG
                ).show();
            }
        } else {
            if(player.getPg() >= player.getMaxPg())
                pg.setTextColor(Color.GREEN);
            else
                pg.setTextColor(getResources().getColor(
                        R.color.abc_primary_text_material_dark
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
                    p.getString("playerName", getString(R.string.adventurer_name)),
                    p.getInt("classInt", Player.NULL),
                    p.getInt("raceInt", Player.NULL),
                    p.getInt("px", 0),
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
            player.setName(p.getString("playerName", getString(R.string.adventurer_name)));
            player.setClassInt(p.getInt("classInt", Player.NULL));
            player.setRaceInt(p.getInt("raceInt", Player.NULL));
            player.setPx(p.getInt("px", 0));
            player.setAtk(new int[]{
                    p.getInt("fue", 10),
                    p.getInt("con", 10),
                    p.getInt("des", 10),
                    p.getInt("int", 10),
                    p.getInt("sab", 10),
                    p.getInt("car", 10),
            });

        }
        if(player.getLevel() != 1 && player.getMaxPg() == 0) {
            pgDialog();
        }
        player.setCurativeEffort(p.getInt("curativeEfforts", player.getCurativeEfforts()));
        player.setPg(p.getInt("pg", player.getPg()));
        healthStatusCheck();
        updateCurativeString();
        //set restored values to the respective fields
        ((TextView) findViewById(R.id.nameText)).setText(player.getName());
        ((TextView) findViewById(R.id.raceText)).setText(player.getRaceName());
        ((TextView) findViewById(R.id.classText)).setText(player.getClassName());
        ((TextView) findViewById(R.id.lvl)).setText(String.valueOf(player.getLevel()));

        ((Button) findViewById(R.id.pgCurrent)).setText(String.valueOf(player.getPg()));

        //attacks
        ((TextView) findViewById(R.id.FUE)).setText(
                getString(R.string.FUE) + ":" + player.getFue()
        );
        ((TextView) findViewById(R.id.CON)).setText(
                getString(R.string.CON) + ":" + player.getCon()
        );
        ((TextView) findViewById(R.id.DES)).setText(
                getString(R.string.DES) + ":" + player.getDes()
        );
        ((TextView) findViewById(R.id.INT)).setText(
                getString(R.string.INT) + ":" + player.getInt()
        );
        ((TextView) findViewById(R.id.SAB)).setText(
                getString(R.string.SAB) + ":" + player.getSab()
        );
        ((TextView) findViewById(R.id.CAR)).setText(
                getString(R.string.CAR) + ":" + player.getCar()
        );

        //defenses
        ((TextView) findViewById(R.id.CA)).setText(
                getString(R.string.CA) + ": " + player.getCa()
        );
        ((TextView) findViewById(R.id.FORT)).setText(
                getString(R.string.FORT) + ":" + player.getFort()
        );
        ((TextView) findViewById(R.id.REF)).setText(
                getString(R.string.REF) + ":" + player.getRef()
        );
        ((TextView) findViewById(R.id.VOL)).setText(
                getString(R.string.VOL) + ": " + player.getVol()
        );


    }

    private void updateCurativeString() {
        ((TextView) findViewById(R.id.curativeEffortsText)).setText(
                getString(
                        R.string.curative_display_text,
                        player.getCurativeEfforts(),
                        player.getMaxCurativeEfforts()
                )
        );
    }

    public void selectPlayer(View view) {
        //TODO: implement players and switch between them
    }

    private void undo() {
        String message = "";
        if(undoObject == CURRENT_PG){
            ((Button) findViewById(R.id.pgCurrent)).setText(String.valueOf(undoPreviousValue));
            player.setPg(undoPreviousValue);
            undoObject = NULL;
            message = getString(R.string.action_undo_current_pg);
        }
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
        undo = false;
        invalidateOptionsMenu();
    }

    private void pgDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final AlertDialog d;
        input.setHint(R.string.dialog_resolve_max_pg_hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.FLAG_EDITOR_ACTION) {
                    if (input.getText().toString().isEmpty()){
                        Toast.makeText(
                                getApplicationContext(),
                                R.string.empty_field,
                                Toast.LENGTH_LONG
                        ).show();
                        pgDialog();
                    } else {
                        player.setMaxPg(Integer.parseInt(input.getText().toString()));
                    }
                }
                return false;
            }
        });
        dialog
                .setView(input)
                .setCancelable(false)
                .setTitle(R.string.dialog_resolve_max_pg_title)
                .setMessage(R.string.dialog_resolve_max_pg_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().isEmpty()) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    R.string.empty_field,
                                    Toast.LENGTH_LONG
                            ).show();
                            pgDialog();
                        } else {
                            player.setMaxPg(Integer.parseInt(input.getText().toString()));
                        }
                    }
                });
        dialog.show();
    }

    //TODO: show on screen the max pg's

    //TODO: show the current px and progress bar
}