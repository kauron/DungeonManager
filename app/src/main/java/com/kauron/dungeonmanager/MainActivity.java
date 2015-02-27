package com.kauron.dungeonmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
        ((ProgressBar) findViewById(R.id.xpBar))
                .getProgressDrawable()
                        .setColorFilter(Color.parseColor("#62BACE"), PorterDuff.Mode.SRC_IN);
        ((ProgressBar) findViewById(R.id.curativeEffortsBar))
                .getProgressDrawable()
                        .setColorFilter(Color.parseColor("#FFD700"), PorterDuff.Mode.SRC_IN);
        //TODO: use the negative PG bar, not curativeEfforts one
//        findViewById(R.id.negCurativeEffortsBar).setRotation(180);
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
                    player = null;
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
                        boolean levelUp = player.addPx(Integer.parseInt(input.getText().toString()));
                        if (levelUp) {
                            //levelUp
                            //TODO: update defenses
                            //TODO: add attack points when necessary
                            //TODO: update currentPg button
                            //TODO: improve leveling up
                            player.setMaxPgOnLevelUp();
                            ((TextView) findViewById(R.id.lvl)).setText(
                                    String.valueOf(player.getLevel())
                            );
                            healthStatusCheck();
                            updateCurativeString();
                        }
                        getSharedPreferences("basics", MODE_PRIVATE)
                                .edit().putInt("px", player.getPx()).apply();
                        incrementProgressBar(
                                (ProgressBar) findViewById(R.id.xpBar),
                                (TextView) findViewById(R.id.currentXp),
                                1, levelUp, Player.LEVEL_PX[player.getLevel()] -
                                            Player.LEVEL_PX[player.getLevel() - 1],
                                true, player.getPx() - Player.LEVEL_PX[player.getLevel() - 1]
                        );
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
            input.requestFocus();
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
            SharedPreferences.Editor e = getSharedPreferences("basics", MODE_PRIVATE).edit();
            e.putInt("pg", player.getPg());
            if(usesEffort) {
                e.putInt("curativeEfforts", player.getCurativeEfforts());
                incrementProgressBar(
                        (ProgressBar) findViewById(R.id.curativeEffortsBar),
                        (TextView) findViewById(R.id.currentCurativeEfforts),
                        100, false, 0,
                        true, player.getCurativeEfforts()
                );
            }
            e.apply();
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
                    incrementProgressBar(
                            (ProgressBar) findViewById(R.id.pgBar),
                            (TextView) findViewById(R.id.currentPg),
                            100, false, 0,
                            true, player.getPg()
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
//        ProgressBar pgBar = (ProgressBar) findViewById(R.id.pgBar);
//        incrementProgressBar(
//                pgBar,
//                (TextView) findViewById(R.id.currentPg),
//                Math.abs(player.getPg()),
//                100,
//                false,
//                CURRENT_PG
//        );
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
//            pgBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
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
//            pgBar.getProgressDrawable().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
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
//            pgBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
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
            while(!p.getBoolean("saved", false));
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
            ((ProgressBar) findViewById(R.id.xpBar))
                    .setMax(
                            Player.LEVEL_PX[player.getLevel()] -
                            Player.LEVEL_PX[player.getLevel() - 1]
                    );
        } else {
            player.setName(p.getString("playerName", getString(R.string.adventurer_name)));
            player.setClassInt(p.getInt("classInt", Player.NULL));
            player.setRaceInt(p.getInt("raceInt", Player.NULL));
            player.setPx(p.getInt("px", player.getPx()));
            player.setAtk(new int[]{
                    p.getInt("fue", 10),
                    p.getInt("con", 10),
                    p.getInt("des", 10),
                    p.getInt("int", 10),
                    p.getInt("sab", 10),
                    p.getInt("car", 10),
            });

        }
        if(player.getMaxPg() == 0) {
            pgDialog();
        }
        player.setCurativeEffort(p.getInt("curativeEfforts", player.getMaxCurativeEfforts()));
        player.setPg(p.getInt("pg", player.getMaxPg()));
        incrementProgressBar(
                (ProgressBar) findViewById(R.id.pgBar),
                (TextView) findViewById(R.id.currentPg),
                100, true, player.getMaxPg(),
                true, player.getPg()
        );
        ProgressBar curativeEffortsBar = (ProgressBar) findViewById(R.id.curativeEffortsBar);
        incrementProgressBar(
                curativeEffortsBar,
                (TextView) findViewById(R.id.currentCurativeEfforts),
                100, true, player.getMaxCurativeEfforts(),
                true, player.getCurativeEfforts()
        );
        healthStatusCheck();
        updateCurativeString();
        //set restored values to the respective fields
        ((TextView) findViewById(R.id.nameText)).setText(player.getName());
        ((TextView) findViewById(R.id.raceText)).setText(player.getRaceName());
        ((TextView) findViewById(R.id.classText)).setText(player.getClassName());
        ((TextView) findViewById(R.id.lvl)).setText(String.valueOf(player.getLevel()));

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
        ((TextView) findViewById(R.id.currentCurativeEfforts)).setText(
                player.getCurativeEfforts() + " / " +
                player.getMaxCurativeEfforts()
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
        input.requestFocus();
    }

    //TODO: fix the display of maxPg and levelUp
    //TODO: use this for px and curativeEfforts
    //TODO: set up a partial barCommand to raise only between the ratios, then a manager, then another
    //TODO: if pgBar, change color accordingly with the pg
    private void incrementProgressBar(final ProgressBar progressBar, final TextView textView,
                                      final int factor,
                                      final boolean setMax, int max,
                                      final boolean setVal, int end) {
        if(setMax) progressBar.setMax(factor*max);
        if(!setVal) return;
        if(progressBar.getProgress() - end*factor == 0) return;
        final Handler handler = new Handler();
        final int finalEnd = end * factor;
        final boolean negative = finalEnd < 0;
        final int time = 2000 / Math.abs(progressBar.getProgress() - finalEnd);
        new Thread(new Runnable() {
            public void run() {
                int current = progressBar.getProgress();
                boolean bigger = current < finalEnd;
                while ((bigger && current < finalEnd) || (!bigger && current > finalEnd)) {
                    if(bigger) current++;
                    else current--;

//                    if(progressBar.getId() == R.id.pgBar) {
//                        double rate = (double)current / progressBar.getMax() * (negative ? -1:1);
//                        if (rate <= 0) {
//                            progressBar.getProgressDrawable()
//                                    .setColorFilter(
//                                            Color.RED,
//                                            PorterDuff.Mode.SRC_IN
//                                    );
//                        } else if (rate <= 0.5) {
//                            progressBar.getProgressDrawable()
//                                    .setColorFilter(
//                                            Color.YELLOW,
//                                            PorterDuff.Mode.SRC_IN
//                                    );
//                        } else {
//                            progressBar.getProgressDrawable()
//                                    .setColorFilter(
//                                            Color.GREEN,
//                                            PorterDuff.Mode.SRC_IN
//                                    );
//                        }
//                    }
                    // Update the progress bar and display the
                    //current value in the text view
                    final int finalCurrent = Math.abs(current);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(finalCurrent);
                            if(factor == 1){
                                textView.setText(
                                        (finalCurrent + Player.LEVEL_PX[player.getLevel() - 1])
                                        + " / " +
                                        (progressBar.getMax() + Player.LEVEL_PX[player.getLevel() - 1])
                                );
                            } else {
                                textView.setText((finalCurrent/factor) +" / "+(progressBar.getMax()/factor));
                            }
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        //Just to display the progress slowly
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


    //TODO: show on screen the max pg's
    //TODO: show in the bars the max and current values
    //TODO: create secondary thread to move slower the value of the progressBar
}