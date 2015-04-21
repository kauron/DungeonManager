package com.kauron.dungeonmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

public class MainActivity extends ActionBarActivity {

    public static final int CURRENT_PG = 1, NULL = 0;

    public Player player;
    private boolean undo;
    private int undoObject, undoPreviousValue;

    private ProgressBar pgBar, negPgBar, xpBar, curativeEffortsBar;
    private Button pgCurrent;
    private TextView currentPg, currentXp, currentCurativeEfforts, lvl;
    private SharedPreferences p;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initializing activity (setting toolbar as actionbar)
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Loading player
        try{
            String name = getSharedPreferences(Welcome.PREFERENCES, MODE_PRIVATE)
                    .getString("player" + getIntent().getIntExtra("player", 0), "null");
            p = getSharedPreferences(name, MODE_PRIVATE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error loading character", Toast.LENGTH_LONG).show();
            finish();
        }

        //Performing all the findViewById commands
        xpBar = (ProgressBar) findViewById(R.id.xpBar);
        curativeEffortsBar = (ProgressBar) findViewById(R.id.curativeEffortsBar);
        pgBar = (ProgressBar) findViewById(R.id.pgBar);
        negPgBar = (ProgressBar) findViewById(R.id.negPgBar);

        pgCurrent = (Button) findViewById(R.id.pgCurrent);

        lvl = (TextView) findViewById(R.id.lvl);
        currentPg = (TextView) findViewById(R.id.currentPg);
        currentXp = (TextView) findViewById(R.id.currentXp);
        currentCurativeEfforts = (TextView) findViewById(R.id.currentCurativeEfforts);

        xpBar.getProgressDrawable()
                        .setColorFilter(getResources().getColor(R.color.px_bar), PorterDuff.Mode.SRC_IN);
        curativeEffortsBar.getProgressDrawable()
                        .setColorFilter(getResources().getColor(R.color.surges_bar), PorterDuff.Mode.SRC_IN);
        undo = false;

        restoreData();
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
        MenuItem menuUndo = menu.findItem(R.id.action_undo);
        menuUndo.setEnabled(undo);
        if (undo)
            menuUndo.getIcon().setAlpha(255);
        else
            menuUndo.getIcon().setAlpha(128);
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
                SnackbarManager.show(
                        Snackbar
                                .with(this)
                                .text(R.string.maxed_curative)
                );
            } else {
                healDialog();
            }
            return true;
        } else if (id == R.id.action_undo) {
            undo();
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
                            //TODO: improve leveling up by using a sliding guide
                            player.setMaxPgOnLevelUp();
                            lvl.setText(
                                    String.valueOf(player.getLevel())
                            );
                        }
                        p.edit().putInt("px", player.getPx()).apply();
                        if(levelUp)
                            xpBar.setMax(Player.LEVEL_PX[player.getLevel()] -
                                            Player.LEVEL_PX[player.getLevel() - 1]);
                        pxUpdate();
                        ceUpdate();
                        pgUpdate();
//                        incrementProgressBar(
//                                xpBar, currentXp,
//                                1, levelUp, Player.LEVEL_PX[player.getLevel()] -
//                                            Player.LEVEL_PX[player.getLevel() - 1],
//                                true, player.getPx() - Player.LEVEL_PX[player.getLevel() - 1]
//                        );
                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "There was an error leveling up", Toast.LENGTH_LONG).show();
                    }
                }
            });
            alert.show();
            input.requestFocus();
            return true;
        } else if (id == R.id.action_time_long_rest) {
            player.rest(true);
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(R.string.long_rest_done)
            );
            p.edit()
                    .putInt("pg", player.getPg())
                    .putInt("curativeEfforts", player.getCurativeEfforts())
                    .apply();
            pgUpdate();
            ceUpdate();
        } else if (id == R.id.action_time_rest) {
            player.rest(false);
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(R.string.rest_done)
            );
            pgUpdate();
            ceUpdate();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreData();
        pgUpdate();
        ceUpdate();
        pxUpdate();
    }

    public void heal(boolean usesEffort) {
        int hasCured = player.recoverPg(Player.USE_CURATIVE_EFFORT, usesEffort);
        if (hasCured == Player.NOT_CURED) {
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(R.string.no_curative_efforts_error)
            );
        } else {
            if(hasCured == Player.MAXED){
                SnackbarManager.show(
                        Snackbar
                                .with(this)
                                .text(R.string.maxed_curative)
                );
            }
            SharedPreferences.Editor e = p.edit();
            e.putInt("pg", player.getPg());
            if(usesEffort) {
                e.putInt("curativeEfforts", player.getCurativeEfforts());
//                incrementProgressBar(
//                        curativeEffortsBar, currentCurativeEfforts,
//                        100, false, 0,
//                        true, player.getCurativeEfforts()
//                );
                ceUpdate();
            }
            e.apply();
            pgUpdate();
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

        final Context context = getApplicationContext();
        final MainActivity activity = this;

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int preValue = Integer.parseInt(pgCurrent.getText().toString());
                    int damage = Integer.parseInt(input.getText().toString());
                    player.losePg(damage);
                    pgCurrent.setText(String.valueOf(player.getPg()));
//                    incrementProgressBar(
//                            pgBar, currentPg,
//                            100, false, 0,
//                            true, player.getPg()
//                    );
                    //finished correctly, then apply values to undo's
                    undo = true;
                    undoPreviousValue = preValue;
                    undoObject = CURRENT_PG;
                    SnackbarManager.show(
                            Snackbar.with(context).text("Lost " + damage + " PG's")
                                .actionLabel("Undo") // action button label
                                .actionListener(new ActionClickListener() {
                                    @Override
                                    public void onActionClicked(Snackbar snackbar) {
                                        undo();
                                    }
                                })
                                .actionColor(getResources().getColor(R.color.yellow))
                    ,activity); // action button's
                    p.edit().putInt("pg", player.getPg()).apply();
                    pgUpdate();
                    invalidateOptionsMenu();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "There was an error", Toast.LENGTH_LONG).show();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void pgUpdate() {
        int status = player.getState();
        int lastState = player.getLastState();
        int pg = player.getPg();
        if (pg < 0) {
            pgBar.setProgress(0);
            negPgBar.setProgress(-pg);
        } else if (pg > 0) {
            pgBar.setProgress(pg);
            negPgBar.setProgress(0);
        } else {
            pgBar.setProgress(0);
            negPgBar.setProgress(0);
        }
        currentPg.setText(player.getPg() + " / " + player.getMaxPg());
//        incrementProgressBar(
//                pgBar, currentPg,
//                100, false, 0,
//                true, Math.abs(player.getPg())
//        );
        pgCurrent.setText(String.valueOf(player.getPg()));
        if (status == Player.MUERTO) {
            pgCurrent.setTextColor(Color.BLACK);
            pgCurrent.setBackgroundColor(getResources().getColor(R.color.red));

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.reset_confirmation_title));
            alert.setMessage(getString(R.string.reset_confirmation));
            alert.setPositiveButton(R.string.action_undo, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    undo();
                }
            });
            final Activity activity = this;
            alert.setNegativeButton(R.string.die, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SnackbarManager.show(
                            Snackbar
                                    .with(activity)
                                    .text(R.string.message_death)
                    );
                    p.edit().clear().apply();
                    restoreData();
                }
            });

            alert.show();
        } else if (status == Player.DEBILITADO) {
            pgCurrent.setBackgroundColor(android.R.drawable.btn_default);
            pgCurrent.setTextColor(getResources().getColor(R.color.red));
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            negPgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            if(lastState != Player.SAME) {
                SnackbarManager.show(
                        Snackbar
                                .with(this)
                                .text(R.string.state_changed_debilitado)
                );
            }
        } else if (status == Player.MALHERIDO) {
            pgCurrent.setBackgroundColor(android.R.drawable.btn_default);
            pgCurrent.setTextColor(getResources().getColor(R.color.yellow));
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_IN);
            negPgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_IN);
            if(lastState != Player.SAME) {
                SnackbarManager.show(
                        Snackbar
                                .with(this)
                                .text(R.string.state_changed_malherido)
                );
            }
        } else {
            pgCurrent.setTextColor(getResources().getColor(R.color.green));
            pgCurrent.setBackgroundColor(android.R.drawable.btn_default);
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            negPgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        }
    }

    private void restoreData(){
        if (player == null) {
            player = new Player( p );
        } else {
            player.setName(p.getString(Player.NAME, player.getName()));
            player.setClassInt(p.getInt(Player.CLASS, player.getClassInt()));
            player.setRaceInt(p.getInt(Player.RACE, player.getRaceInt()));
            player.setPx(p.getInt(Player.PX, player.getPx()));
            player.setAtk(new int[]{
                    p.getInt("fue", player.getFue()),
                    p.getInt("con", player.getCon()),
                    p.getInt("des", player.getDes()),
                    p.getInt("int", player.getInt()),
                    p.getInt("sab", player.getSab()),
                    p.getInt("car", player.getCar())
            });
        }
        pxUpdate();
        xpBar.setMax(
                Player.LEVEL_PX[player.getLevel()] -
                Player.LEVEL_PX[player.getLevel() - 1]
        );

        if (player.getMaxPg() == 0) {
            pgDialog();
        }
        player.setCurativeEffort(p.getInt("curativeEfforts", player.getMaxCurativeEfforts()));
        player.setPg(p.getInt("pg", player.getMaxPg()));

        pgBar.setMax(player.getMaxPg());
        negPgBar.setMax(player.getMaxPg() / 2);
        curativeEffortsBar.setMax(player.getMaxCurativeEfforts());
        pgUpdate();
        ceUpdate();
        //set restored values to the respective fields
        ((TextView) findViewById(R.id.nameText)).setText(player.getName());
        ((TextView) findViewById(R.id.raceText)).setText(player.getRaceName());
        ((TextView) findViewById(R.id.classText)).setText(player.getClassName());
        lvl.setText(String.valueOf(player.getLevel()));

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
                getString(R.string.CA) + ":" + player.getCa()
        );
        ((TextView) findViewById(R.id.FORT)).setText(
                getString(R.string.FORT) + ":" + player.getFort()
        );
        ((TextView) findViewById(R.id.REF)).setText(
                getString(R.string.REF) + ":" + player.getRef()
        );
        ((TextView) findViewById(R.id.VOL)).setText(
                getString(R.string.VOL) + ":" + player.getVol()
        );
    }

    private void ceUpdate() {
        curativeEffortsBar.setProgress(player.getCurativeEfforts());
        currentCurativeEfforts.setText(
                player.getCurativeEfforts() + " / " +
                player.getMaxCurativeEfforts()
        );
    }

    private void pxUpdate() {
        xpBar.setProgress(player.getPx());
        currentXp.setText(
                player.getPx() + " / " +
                        Player.LEVEL_PX[player.getLevel()]
        );

    }

    private void undo() {
        String message = "";
        if(undoObject == CURRENT_PG){
            pgCurrent.setText(String.valueOf(undoPreviousValue));
            player.setPg(undoPreviousValue);
            undoObject = NULL;
            message = getString(R.string.action_undo_current_pg);
        }
        if (!message.isEmpty()) {
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(message)
            );
        }
        pgUpdate();
        undo = false;
        invalidateOptionsMenu();
    }

    private void pgDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
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
                        if (!input.getText().toString().isEmpty()) {
                            player.setMaxPg(Integer.parseInt(input.getText().toString()));
                        }
                    }
                });
        dialog.show();
        input.requestFocus();
    }
}