package com.kauron.dungeonmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.File;
import java.util.ArrayList;

public class ShowPlayer extends ActionBarActivity {

    public static final int CURRENT_PG = 1, NULL = 0;

    public Player player;
    private boolean undo;
    private int undoObject, undoPreviousValue;

    private ProgressBar posHpBar, negHpBar, xpBar, surgesBar;
    private TextView hpText, xpText, surgesText, levelText;
    private SharedPreferences p;
    private Toolbar toolbar;


    private ListView powerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initializing activity (setting toolbar as actionbar)
        setContentView(R.layout.activity_show_player);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        xpBar     = (ProgressBar) findViewById(R.id.xpBar);
        surgesBar = (ProgressBar) findViewById(R.id.surgesBar);
        posHpBar  = (ProgressBar) findViewById(R.id.positive_hp_bar);
        negHpBar  = (ProgressBar) findViewById(R.id.negative_hp_bar);

        hpText     = (TextView) findViewById(R.id.hpText);
        xpText     = (TextView) findViewById(R.id.xpText);
        surgesText = (TextView) findViewById(R.id.surgesText);
        levelText  = (TextView) findViewById(R.id.levelText);

        xpBar.getProgressDrawable()
                        .setColorFilter(getResources().getColor(R.color.px_bar), PorterDuff.Mode.SRC_IN);
        surgesBar.getProgressDrawable()
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
        boolean maxed = player.getHp() == player.getMaxHp();
        menuHeal.setEnabled(!maxed);
        if (maxed)
            menuHeal.getIcon().setAlpha(128);
        else
            menuHeal.getIcon().setAlpha(255);
        return true;
    }

    private void addPx(EditText input) {
        try {
            if (player.addPx(Integer.parseInt(input.getText().toString()))) levelUp();
            p.edit().putInt("px", player.getXp()).apply();
            xpUpdate();
            surgeUpdate();
            hpUpdate();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "There was an error leveling up", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cure) {
            if(player.getMaxHp() <= player.getHp()){
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
            input.setImeOptions(EditorInfo.IME_ACTION_DONE);
            input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        addPx(input);
                    }
                    return false;
                }
            });
            input.setHint(R.string.px_awarded_hint);
            alert.setView(input);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addPx(input);
                }
            });
            alert.setNegativeButton(R.string.level_up, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    player.setXp(Player.LEVEL_XP[player.getLevel()]);
                    levelUp();
                    p.edit().putInt(Player.XP, player.getXp()).apply();
                    xpUpdate();
                }
            });
            alert.show();
            input.requestFocus();
            return true;
        } else if (id == R.id.action_time_long_rest) {
            PowerAdapter powerAdapter = (PowerAdapter) powerList.getAdapter();
            if (powerAdapter != null) {
                for (int i = 0; i < powerAdapter.getCount(); i++) {
                    Power power = powerAdapter.getItem(i);
                    if ( power.getFreq() != Power.AT_WILL) {
                        power.recover(Power.DAILY);
                        getSharedPreferences(p.getString("power" + i, ""), MODE_PRIVATE)
                                .edit().putBoolean("used", false);
                    }
                }
                //TODO: substitute all calls to refreshList for an update on the single view that changed
                refreshList();
            }
            player.rest(true);
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(R.string.long_rest_done)
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            );
            p.edit()
                    .putInt("pg", player.getHp())
                    .putInt("curativeEfforts", player.getSurges())
                    .apply();
            hpUpdate();
            surgeUpdate();
        } else if (id == R.id.action_time_rest) {
            PowerAdapter powerAdapter = (PowerAdapter) powerList.getAdapter();
            if (powerAdapter != null) {
                for (int i = 0; i < powerAdapter.getCount(); i++) {
                    Power power = powerAdapter.getItem(i);
                    if ( power.getFreq() == Power.ENCOUNTER) {
                        power.recover(Power.ENCOUNTER);
                        getSharedPreferences(p.getString("power" + i, ""), MODE_PRIVATE)
                                .edit().putBoolean("used", false);
                    }
                }
                refreshList();
            }
//            player.rest(false); TODO: this isn't needed without action points
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(R.string.rest_done)
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            );
            hpUpdate();
            surgeUpdate();
        }

        return super.onOptionsItemSelected(item);
    }

    private void levelUp() {
        //TODO: improve leveling up by using a sliding guide
        xpBar.setMax(Player.LEVEL_XP[player.getLevel()] -
                Player.LEVEL_XP[player.getLevel() - 1]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreData();
        hpUpdate();
        surgeUpdate();
        xpUpdate();
    }

    /**
     * Heals the player and displays an error if the healing wasn't possible
     * @param usesEffort boolean Whether if the healing consumes a surge or not
     * @return boolean ! ( usesEffort && error )
     */
    public boolean heal(boolean usesEffort) {
        int hasCured = player.recoverPg(Player.USE_CURATIVE_EFFORT, usesEffort);
        if (hasCured == Player.NOT_CURED) {
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(R.string.no_curative_efforts_error)
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            );
            return false;
        } else {
            SharedPreferences.Editor e = p.edit();
            e.putInt("pg", player.getHp());
            if(usesEffort) {
                e.putInt("curativeEfforts", player.getSurges());
                surgeUpdate();
            }
            e.apply();
            hpUpdate();
            return true;
        }
    }

    /**
     * Healing dialog that let's the player choose between using or not a surge to heal themselves.
     * If the healing action is successful, a Snackbar is displayed to let the player undo it.
     */
    public void healDialog() {
        final Activity activity = this;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.new_energies_message)
                .setTitle(R.string.new_energies)
                .setPositiveButton(R.string.me, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        undoPreviousValue = player.getHp();
                        if (heal(true)) { //a surge has been used
                            SnackbarManager.show(
                                    Snackbar.with(getApplicationContext())
                                            .text(String.format(getString(R.string.healed), player.getMaxHp() / 4))
                                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                            .actionLabel(R.string.action_undo)
                                            .actionColor(getResources().getColor(R.color.yellow))
                                            .actionListener(new ActionClickListener() {
                                                @Override
                                                public void onActionClicked(Snackbar snackbar) {
                                                    player.setHp(undoPreviousValue);
                                                    player.setCurativeEffort(player.getSurges() + 1);
                                                    p.edit().putInt("pg", undoPreviousValue)
                                                            .putInt("curativeEfforts", player.getSurges())
                                                            .apply();
                                                    hpUpdate();
                                                    surgeUpdate();
                                                    SnackbarManager.show(
                                                            Snackbar
                                                                    .with(getApplicationContext())
                                                                    .text(R.string.restored)
                                                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE),
                                                            activity
                                                    );
                                                }
                                            }),
                                    activity
                            );
                        }
                    }
                })
                .setNegativeButton(R.string.other, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        undoPreviousValue = player.getHp();
                        heal(false);
                        SnackbarManager.show(
                                Snackbar.with(getApplicationContext())
                                        .text(String.format(getString(R.string.healed), player.getMaxHp() / 4))
                                        .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                        .actionLabel(R.string.action_undo)
                                        .actionColor(getResources().getColor(R.color.yellow))
                                        .actionListener(new ActionClickListener() {
                                            @Override
                                            public void onActionClicked(Snackbar snackbar) {
                                                player.setHp(undoPreviousValue);
                                                p.edit().putInt("pg", undoPreviousValue).apply();
                                                SnackbarManager.show(
                                                        Snackbar
                                                                .with(getApplicationContext())
                                                                .text(R.string.restored)
                                                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE),
                                                        activity
                                                );
                                                hpUpdate();
                                            }
                                        }),
                                activity
                        );
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

    /**
     * Damage dialog with an Edittext to input a number (damage), which then is done to the player
     * If the input is not empty, the hit points are updated and an undo Snackbar is added
     *
     * @param view View pressed to trigger this method (onClick attribute on xml)
     */
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
                    int preValue = player.getHp();
                    int damage = Integer.parseInt(input.getText().toString());
                    player.losePg(damage);
                    undo = true;
                    undoPreviousValue = preValue;
                    undoObject = CURRENT_PG;
                    SnackbarManager.show(
                            Snackbar.with(context).text(String.format(getString(R.string.lost_hp), damage))
                                    .actionLabel(R.string.action_undo) // action button label
                                    .actionListener(new ActionClickListener() {
                                        @Override
                                        public void onActionClicked(Snackbar snackbar) {
                                            undo();
                                        }
                                    })
                                    .actionColor(getResources().getColor(R.color.yellow))
                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                    ,activity); // action button's
                    p.edit().putInt("pg", player.getHp()).apply();
                    hpUpdate();
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

    /**
     * Sets the hit points progress bars to the adequate value and color.
     * Sets the text indicating the numerical value of the hit points
     */
    private void hpUpdate() {
        int hp = player.getHp();
        negHpBar.setProgress(hp < 0 ? -hp : 0);
        posHpBar.setProgress(hp > 0 ? hp : 0);

        hpText.setText(player.getHp() + " / " + player.getMaxHp());

        int color = player.getStatusColor(getApplicationContext().getResources());
        posHpBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        negHpBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Recovers the player's data from the sharedPreferences and then updates all the layouts
     * This includes an update for the powerList layout.
     */
    private void restoreData(){
        //Loading player

        player = new Player( p );

        xpUpdate();
        xpBar.setMax(
                Player.LEVEL_XP[player.getLevel()] -
                        Player.LEVEL_XP[player.getLevel() - 1]
        );

        player.setCurativeEffort(p.getInt("curativeEfforts", player.getMaxSurges()));
        player.setHp(p.getInt("pg", player.getMaxHp()));

        posHpBar.setMax(player.getMaxHp());
        negHpBar.setMax(player.getMaxHp() / 2);
        surgesBar.setMax(player.getMaxSurges());
        hpUpdate();
        surgeUpdate();
        //set restored values to the respective fields
        toolbar.setTitle(player.getName());
        toolbar.setSubtitle(
                player.getClassName() + " " + player.getRaceName()
        );
        //Loading powers and abilities (not implemented yet)
        refreshList();
    }

    /**
     * This updates the progress and indicator text of the available surges.
     */
    private void surgeUpdate() {
        surgesBar.setProgress(player.getSurges());
        surgesText.setText(
                player.getSurges() + " / " +
                        player.getMaxSurges()
        );
    }

    /**
     * Updates the progress and indicator text of the XP
     */
    private void xpUpdate() {
        xpBar.setProgress(player.getXp() - Player.LEVEL_XP[player.getLevel() - 1]);
        levelText.setText(getString(R.string.level) + " " + player.getLevel());
        xpText.setText(
                player.getXp() + " / " +
                        Player.LEVEL_XP[player.getLevel()]
        );

    }

    /**
     * Undoes the last change done by the player. Only used in damage().
     * Healing is undone in the health dialog
     */
    private void undo() {
        String message = "";
        if(undoObject == CURRENT_PG){
            player.setHp(undoPreviousValue);
            undoObject = NULL;
            message = getString(R.string.restored);
        }
        if (!message.isEmpty()) {
            SnackbarManager.show(
                    Snackbar
                            .with(this)
                            .text(message)
                            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            );
        }
        hpUpdate();
        undo = false;
        invalidateOptionsMenu();
    }


    /**
     * Launches the PowerEditor to create a new attack inside the current player
     * @param view View Button that called this method (onClick)
     */
    public void addToList (View view) {
        startActivity(
                new Intent(
                        this,
                        PowerEditor.class
                )
                        .putExtra("player", player.getName())
                        .putExtra("power", -1)
        );
    }

    /**
     * Checks which list is currently displayed and shows on screen a list of the elements in that list
     * No abilities are displayed yet, INCOMING FEATURE
     *
     * Also has a popup for the attacks (displaying all info)
     * INCOMING FEATURE: onClicking one ability a dialog with a d20 appears and onClicking rolls,
     * then displays the sum of the dice result plus the player's ability bonus
     */
    private void refreshList() {
        //TODO: check which is active (now there is only a power list), so there is only one possibility

        int n = p.getInt("powers",0);
        int elements = 0;
        powerList = (ListView) findViewById(R.id.powerList);
        final PowerAdapter adapter = (PowerAdapter) powerList.getAdapter();

        if ( adapter != null )
            elements = adapter.getCount();
        if ( elements < n  && adapter != null ) {
            for ( int i = elements; i < n; i++ ) {
                SharedPreferences sav = getSharedPreferences(p.getString("power" + i, ""), MODE_PRIVATE);
                //TODO: solve error when closing the editor
                adapter.add( new Power ( sav ) );
            }
        } else if ( n != 0 ) {
            ArrayList<Power> powers = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                SharedPreferences sav = getSharedPreferences(p.getString("power" + i, ""), MODE_PRIVATE);
                powers.add( new Power(sav) );
            }

            powerList.setAdapter(new PowerAdapter(this, powers));
            final Activity activity = this;
            powerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    final Dialog dialog = new Dialog(ShowPlayer.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.attack_display);

                    //identify all the elements from the VIEW and then add LISTENERS
                    final Power power = (Power) parent.getItemAtPosition(position);
                    View nameText = dialog.findViewById(R.id.nameText);
                    final int color = power.getFreqColor(getApplicationContext());
                    nameText.setBackgroundColor(color);

                    ((TextView) nameText).setText(power.getName());
                    ((TextView) dialog.findViewById(R.id.typeText)).setText(power.getTypeString());
                    ((TextView) dialog.findViewById(R.id.rangeText)).setText(power.getRangeString() + " ");
                    ((TextView) dialog.findViewById(R.id.freqText)).setText(power.getFrequencyString());
                    ((TextView) dialog.findViewById(R.id.keywordsText)).setText(power.getKeywords());
                    ((TextView) dialog.findViewById(R.id.distanceText)).setText(String.valueOf(power.getDistance()));
                    ((TextView) dialog.findViewById(R.id.objectiveText)).setText(power.getObjective());
                    ((TextView) dialog.findViewById(R.id.impactText)).setText(power.getImpact());
                    ((TextView) dialog.findViewById(R.id.otherText)).setText(power.getOther());

                    String[] attack = getResources().getStringArray(R.array.attack);
                    String[] defense = getResources().getStringArray(R.array.defense);
                    ((TextView) dialog.findViewById(R.id.attackText)).setText(attack[power.getAtk()]
                            + " " + getResources().getString(R.string.vs)
                            + " " + defense[power.getDef()]);

                    final Button useButton = (Button) dialog.findViewById(R.id.useButton);

                    if (power.isUsed()) {
                        useButton.getBackground().setAlpha(128);
                        useButton.setEnabled(false);
                        useButton.setClickable(false);
                    } else {
                        useButton.setBackgroundColor(color);
                        useButton.setTextColor(getResources().getColor(R.color.white));
                        useButton.getBackground().setAlpha(255);
                        useButton.setEnabled(true);
                        useButton.setClickable(true);
                        useButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO: use power
                                power.use();
                                if (power.isUsed()) {
                                    useButton.getBackground().setAlpha(128);
                                    useButton.setTextColor(getResources().getColor(R.color.black));
                                    useButton.setEnabled(false);
                                    useButton.setClickable(false);
                                    getSharedPreferences(p.getString("power" + position, ""), MODE_PRIVATE)
                                            .edit().putBoolean("used", true).apply();
                                    refreshList();
                                    SnackbarManager.show(
                                            Snackbar.with(getApplicationContext())
                                                    .text(getString(R.string.used) + " " + power.getName())
                                                    .actionListener(new ActionClickListener() {
                                                        @Override
                                                        public void onActionClicked(Snackbar snackbar) {
                                                            power.recover(Power.DAILY);
                                                            useButton.setBackgroundColor(color);
                                                            useButton.setTextColor(getResources().getColor(R.color.white));
                                                            useButton.getBackground().setAlpha(255);
                                                            useButton.setEnabled(true);
                                                            useButton.setClickable(true);
                                                            getSharedPreferences(p.getString("power" + position, ""), MODE_PRIVATE)
                                                                    .edit().putBoolean("used", false).apply();
                                                            refreshList();
                                                        }
                                                    })
                                                    .actionLabel(getString(R.string.action_undo))
                                                    .actionColor(getResources().getColor(R.color.yellow))
                                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE),
                                            activity
                                    );
                                } else {
                                    SnackbarManager.show(
                                            Snackbar.with(getApplicationContext())
                                                    .text(getString(R.string.used) + " " + power.getName())
                                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE),
                                            activity
                                    );
                                }
                                dialog.dismiss();

                            }
                        });
                    }
                    dialog.show();
                }
            });
        }
        final Activity thisActivity = this;

        powerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(thisActivity);
                alert.setItems(
                        new String[]{getString(R.string.delete), getString(R.string.edit)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    SnackbarManager.show(
                                            Snackbar.with(getApplicationContext())
                                                    .text(R.string.sure)
                                                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                                    .actionLabel(R.string.delete)
                                                    .actionColor(getResources().getColor(R.color.yellow))
                                                    .actionListener(new ActionClickListener() {
                                                        @Override
                                                        public void onActionClicked(Snackbar snackbar) {
                                                            //delete the item
                                                            String name = p.getString("power" + position, "");
                                                            if (name != null && !name.isEmpty()) {
                                                                getSharedPreferences(name, MODE_PRIVATE).edit().clear().apply();
                                                                Log.d("Tag", thisActivity.getApplicationContext().getFilesDir().getParent()
                                                                        + File.separator + "shared_prefs" + File.separator + name + ".xml");
                                                                try {
                                                                    if (!new File(thisActivity.getApplicationContext().getFilesDir().getParent()
                                                                            + File.separator + "shared_prefs" + File.separator + name + ".xml").delete())
                                                                        throw new Exception();
                                                                } catch (Exception e) {
                                                                    Log.e("POWER:DELETION", "Error deleting attack files\n" + e.getMessage() + "\n" + e.getStackTrace().toString());
                                                                }
                                                                int max = p.getInt("powers", 0);
                                                                SharedPreferences.Editor ed = p.edit();
                                                                for (int i = position; i < max - 1; i++)
                                                                    ed.putString("power" + i, p.getString("power" + (i + 1), "max"));
                                                                ed.putInt("powers", max - 1).apply();
                                                                refreshList();
                                                                ed.remove("power" + (max - 1)).apply();
                                                            }
                                                        }
                                                    }),
                                            thisActivity
                                    );
                                } else {
                                    //edit the item
                                    startActivity(
                                            new Intent(getApplicationContext(), PowerEditor.class)
                                                    .putExtra("power", position)
                                                    .putExtra("player", player.getName())
                                    );
                                }
                            }
                        });
                alert.show();
                return true;
            }
        });
    }
}