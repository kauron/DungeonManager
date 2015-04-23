package com.kauron.dungeonmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

public class ShowPlayer extends ActionBarActivity {

    public static final int CURRENT_PG = 1, NULL = 0;

    public Player player;
    private boolean undo;
    //TODO: fix undo (show snackbar with button in each case, without timing).
    private int undoObject, undoPreviousValue;

    private ProgressBar pgBar, negPgBar, xpBar, curativeEffortsBar;
    private TextView currentPg, currentXp, currentCurativeEfforts;
    private SharedPreferences p;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initializing activity (setting toolbar as actionbar)
        setContentView(R.layout.activity_show_player);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        getMenuInflater().inflate(R.menu.menu_show_player, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        MenuItem menuHeal = menu.findItem(R.id.action_cure);
        boolean maxed = player.getPg() == player.getMaxPg();
        menuHeal.setEnabled(!maxed);
        if (maxed)
            menuHeal.getIcon().setAlpha(128);
        else
            menuHeal.getIcon().setAlpha(255);
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
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                );
            } else {
                healDialog();
            }
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
                            //TODO: improve leveling up by using a sliding guide
                        }
                        p.edit().putInt("px", player.getPx()).apply();
                        if(levelUp)
                            xpBar.setMax(Player.LEVEL_PX[player.getLevel()] -
                                            Player.LEVEL_PX[player.getLevel() - 1]);
                        pxUpdate();
                        ceUpdate();
                        pgUpdate();
                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "There was an error leveling up", Toast.LENGTH_LONG).show();
                    }
                }
            });
            alert.show();
            input.requestFocus();
            return true;
            //TODO: the player no longer contains the powers, therefore the
//        } else if (id == R.id.action_time_long_rest) {
//            player.rest(true);
//            SnackbarManager.show(
//                    Snackbar
//                            .with(this)
//                            .text(R.string.long_rest_done)
//                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
//            );
//            p.edit()
//                    .putInt("pg", player.getPg())
//                    .putInt("curativeEfforts", player.getCurativeEfforts())
//                    .apply();
//            pgUpdate();
//            ceUpdate();
//        } else if (id == R.id.action_time_rest) {
//            player.rest(false);
//            SnackbarManager.show(
//                    Snackbar
//                            .with(this)
//                            .text(R.string.rest_done)
//                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
//            );
//            pgUpdate();
//            ceUpdate();
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
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            );
        } else {
            if(hasCured == Player.MAXED){
                SnackbarManager.show(
                        Snackbar
                                .with(this)
                                .text(R.string.maxed_curative)
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                );
            }
            SharedPreferences.Editor e = p.edit();
            e.putInt("pg", player.getPg());
            if(usesEffort) {
                e.putInt("curativeEfforts", player.getCurativeEfforts());
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
        final ShowPlayer activity = this;

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int preValue = player.getPg();
                    int damage = Integer.parseInt(input.getText().toString());
                    player.losePg(damage);
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
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
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
        int pg = player.getPg();
        if (pg < 0) {
            if (status == Player.MUERTO)
                negPgBar.setProgress(negPgBar.getMax());
            else
                negPgBar.setProgress(-pg);
            pgBar.setProgress(0);
        } else {
            pgBar.setProgress(pg);
            negPgBar.setProgress(0);
        }

        currentPg.setText(player.getPg() + " / " + player.getMaxPg());

        if (status == Player.MUERTO) {
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        } else if (status == Player.DEBILITADO) {
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            negPgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        } else if (status == Player.MALHERIDO) {
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_IN);
            negPgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_IN);
        } else {
            pgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            negPgBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        }
    }

    private void restoreData(){
        //Loading player

        player = new Player( p );

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
        toolbar.setTitle(player.getName());
        toolbar.setSubtitle(
                player.getClassName() + " " + player.getRaceName() + " " + player.getLevel()
        );
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.white));
//
//        //attacks
//        ((TextView) findViewById(R.id.FUE)).setText(
//                getString(R.string.FUE) + ":" + player.getFue()
//        );
//        ((TextView) findViewById(R.id.CON)).setText(
//                getString(R.string.CON) + ":" + player.getCon()
//        );
//        ((TextView) findViewById(R.id.DES)).setText(
//                getString(R.string.DES) + ":" + player.getDes()
//        );
//        ((TextView) findViewById(R.id.INT)).setText(
//                getString(R.string.INT) + ":" + player.getInt()
//        );
//        ((TextView) findViewById(R.id.SAB)).setText(
//                getString(R.string.SAB) + ":" + player.getSab()
//        );
//        ((TextView) findViewById(R.id.CAR)).setText(
//                getString(R.string.CAR) + ":" + player.getCar()
//        );
//
//        //defenses
//        ((TextView) findViewById(R.id.CA)).setText(
//                getString(R.string.CA) + ":" + player.getCa()
//        );
//        ((TextView) findViewById(R.id.FORT)).setText(
//                getString(R.string.FORT) + ":" + player.getFort()
//        );
//        ((TextView) findViewById(R.id.REF)).setText(
//                getString(R.string.REF) + ":" + player.getRef()
//        );
//        ((TextView) findViewById(R.id.VOL)).setText(
//                getString(R.string.VOL) + ":" + player.getVol()
//        );
//
//
        //Loading powers and abilities (not implemented yet)
        refreshList();
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
            player.setPg(undoPreviousValue);
            undoObject = NULL;
            message = getString(R.string.action_undo_current_pg);
        }
        if (!message.isEmpty()) {
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(message)
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
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

    public void addToList (View view) { startActivity(new Intent(this, PowerEditor.class).putExtra("player", player.getName())); }

    private void refreshList() {
        //TODO: check which is active (now there is only a power list), so there is only one possibility

        int n = p.getInt("powers",0);
        int elements = 0;
        ListView attackList = (ListView) findViewById(R.id.attackList);
        AttackAdapter adapter = (AttackAdapter) attackList.getAdapter();

        if ( adapter != null )
            elements = adapter.getCount();
        if ( elements < n  && adapter != null ) {
            for ( int i = elements; i < n; i++ ) {
                SharedPreferences sav = getSharedPreferences(p.getString("power" + i, ""), MODE_PRIVATE);
                adapter.add( new Power ( sav ) );
            }
        } else if ( n != 0 ) {
            Power[] powers = new Power[n];
            for (int i = 0; i < n; i++) {
                SharedPreferences sav = getSharedPreferences(p.getString("power" + i, ""), MODE_PRIVATE);
                powers[i] = new Power(sav);
            }

            attackList.setAdapter(new AttackAdapter(this, powers));
            final Activity thisActivity = this;
            attackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final Dialog dialog = new Dialog(ShowPlayer.this);
                    dialog.setContentView(R.layout.attack_display);
                    // set the custom dialog components - text, image and button
//                    Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
//                    // if button is clicked, close the custom dialog
//                    dialogButton.setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            dialog.dismiss();
//                        }
//                    });

                    //identify all the elements from the VIEW and then add LISTENERS
                    Power power = (Power) parent.getItemAtPosition(position);
                    View nameText = dialog.findViewById(R.id.nameText);
                    switch(power.getFreq()){
                        case Power.A_VOLUNTAD:
                            nameText.setBackgroundColor(getResources().getColor(R.color.at_will));
                            break;
                        case Power.ENCUENTRO:
                            nameText.setBackgroundColor(getResources().getColor(R.color.encounter));
                            break;
                        case Power.DIARIO:
                            nameText.setBackgroundColor(getResources().getColor(R.color.daily));
                            break;
                        default:
                            nameText.setBackgroundColor(getResources().getColor(R.color.green));
                    }
                    //TODO: fix the title gap
                    ((TextView) nameText)                               .setText(power.getName());
                    ((TextView) dialog.findViewById(R.id.typeText))     .setText(power.getTypeString());
                    ((TextView) dialog.findViewById(R.id.rangeText))    .setText(power.getRangeString() + " ");
                    ((TextView) dialog.findViewById(R.id.freqText))     .setText(power.getFrequencyString());
                    ((TextView) dialog.findViewById(R.id.keywordsText)) .setText(power.getKeywords());
                    ((TextView) dialog.findViewById(R.id.distanceText)) .setText(String.valueOf(power.getDistance()));
                    ((TextView) dialog.findViewById(R.id.objectiveText)).setText(power.getObjective());
                    ((TextView) dialog.findViewById(R.id.impactText))   .setText(power.getImpact());
                    ((TextView) dialog.findViewById(R.id.otherText))    .setText(power.getOther());

                    String[] attack  = getResources().getStringArray(R.array.attack_array);
                    String[] defense = getResources().getStringArray(R.array.defense_array);
                    //TODO: add attack and defense array
                    ((TextView) dialog.findViewById(R.id.attackText))   .setText(attack[power.getAtk()]
                            + " " + getResources().getString(R.string.vs)
                            + " " + defense[power.getDef()]);

                    dialog.findViewById(R.id.useButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: use power
                            Toast.makeText(getApplicationContext(), "Use it", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialog.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: delete power
                            Toast.makeText(getApplicationContext(), "Delete it", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialog.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: edit power
                            Toast.makeText(getApplicationContext(), "Edit it", Toast.LENGTH_LONG).show();
                        }
                    });

                    dialog.show();
                }
            });
        }
    }
}