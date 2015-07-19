package com.kauron.dungeonmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.File;
import java.util.ArrayList;

public class Welcome extends ActionBarActivity {

    public static final String PREFERENCES = "basics";
    public static final char SEPARATOR = '¬';

    private SharedPreferences p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Player.setStrings(getResources());
        Power .setStrings(getResources());

        p = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_add_player ) {
            addPlayer();
        }

        return super.onOptionsItemSelected(item);
    }

    void addPlayer(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final Activity activity = this;
        alert.setItems(
                new String[]{
                        getString(R.string.add_new_player),
                        getString(R.string.add_existing_player),
                        getString(R.string.add_import)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            startActivity(new Intent(activity, PlayerEditor.class).putExtra("first_time", true));
                        } else if (which == 1) {
                            startActivity(new Intent(activity, PlayerEditor.class).putExtra("first_time", false));
                        } else if (which == 2) {
                            AlertDialog.Builder importDialog = new AlertDialog.Builder(activity);
                            final EditText input = new EditText(activity);
                            input.setHint(getString(R.string.paste_here));
                            importDialog.setView(input);
                            importDialog.setTitle(R.string.add_import);
                            importDialog.setPositiveButton(R.string.import_confirmation, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Gson gson = new Gson();
                                    String imp = input.getText().toString();
                                    Player player = gson
                                            .fromJson(imp.substring(0, imp.indexOf(SEPARATOR)), Player.class);
                                    p.edit().putString(
                                                    "player" + p.getInt("players", 0),
                                                    player.getName())
                                            .apply();
                                    player.saveToPreferences(
                                            getSharedPreferences(player.getName(), MODE_PRIVATE)
                                    );
                                    String errors = "";
                                    while (imp.length() != 1) {
                                        int powers = p.getInt("powers", 0);
                                        imp = imp.substring(imp.indexOf(SEPARATOR) + 1);
                                        Power power = gson
                                                .fromJson(imp.substring(0, imp.indexOf(SEPARATOR)), Power.class);
                                        boolean match = false;
                                        for (int i = 0; i < powers; i++)
                                            if (power.getName().equals(p.getString("power" + i, "")))
                                                match = true;
                                        if (!match) {
                                            p.edit().putString("power" + powers, power.getName()).apply();
                                            power.saveToPreferences(getSharedPreferences(power.getName(), MODE_PRIVATE));
                                        } else {
                                            errors += String.format(getString(R.string.power_already_exists), power.getName());
                                        }
                                    }
                                    if (errors.isEmpty()) errors = getString(R.string.import_completed);
                                    SnackbarManager.show(Snackbar.with(activity).text(errors));
                                    load();
                                }
                            });
                            importDialog.show();
                        }
                    }
                }

        );
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        int n = p.getInt("players",0);
        ListView playerList = (ListView) findViewById(R.id.playersList);
        final PlayerAdapter adapter = (PlayerAdapter) playerList.getAdapter();
        int elements = 0;
        if ( adapter != null )
            elements = adapter.getCount();
        if ( elements < n  && adapter != null ) {
            playerList.setVisibility(View.VISIBLE);
            findViewById(R.id.help_text).setVisibility(View.VISIBLE);
            findViewById(R.id.no_players_text).setVisibility(View.GONE);
            for ( int i = elements; i < n; i++ ) {
                SharedPreferences sav = getSharedPreferences(p.getString("player" + i, ""), MODE_PRIVATE);
                if (sav.contains(Player.NAME))
                    adapter.add( new Player( sav ) );
            }
            adapter.notifyDataSetChanged();
        } else if ( n != 0 ) {
            playerList.setVisibility(View.VISIBLE);
            findViewById(R.id.help_text).setVisibility(View.VISIBLE);
            findViewById(R.id.no_players_text).setVisibility(View.GONE);
            ArrayList<Player> players = new ArrayList<>();
            for ( int i = 0; i < n; i++ ) {
                SharedPreferences sav = getSharedPreferences(p.getString("player" + i, ""), MODE_PRIVATE);
                if (sav.contains(Player.NAME))
                    players.add( new Player( sav ) );
            }

            playerList.setAdapter(new PlayerAdapter(this, players));

            playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(
                            getApplicationContext(), ShowPlayer.class
                    ).putExtra("player", position));
                }
            });
            final ActionBarActivity activity = this;
            playerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    alert.setItems(
                            new String[]{
                                    getString(R.string.delete),
                                    getString(R.string.edit),
                                    getString(R.string.export),
                                    "Test new PlayerDisplay"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        //delete the item
                                        SnackbarManager.show(
                                                Snackbar.with(getApplicationContext())
                                                        .text(R.string.sure)
                                                        .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                                        .actionLabel(R.string.delete)
                                                        .actionColor(getResources().getColor(R.color.yellow))
                                                        .actionListener(new ActionClickListener() {
                                                            @Override
                                                            public void onActionClicked(Snackbar snackbar) {
                                                                String name = p.getString("player" + position, "");
                                                                if (name != null && !name.isEmpty()) {
                                                                    getSharedPreferences(name, MODE_PRIVATE).edit().clear().apply();
                                                                    Log.d("Tag", activity.getApplicationContext().getFilesDir().getParent()
                                                                            + File.separator + "shared_prefs" + File.separator + name + ".xml");
                                                                    try {
                                                                        if (!new File(activity.getApplicationContext().getFilesDir().getParent()
                                                                                + File.separator + "shared_prefs" + File.separator + name + ".xml").delete())
                                                                            throw new Exception();
                                                                    } catch (Exception e) {
                                                                        Toast.makeText(getApplicationContext(), "Error deleting player files", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    int max = p.getInt("players", 0);
                                                                    SharedPreferences.Editor ed = p.edit();
                                                                    for (int i = position; i < max - 1; i++)
                                                                        ed.putString("player" + i, p.getString("player" + (i + 1), "max"));
                                                                    ed.putInt("players", max - 1).apply();
                                                                    load();
                                                                    ed.remove("player" + (max - 1)).apply();
                                                                }
                                                            }
                                                        }),
                                                activity
                                        );
                                    } else if (which == 1) {
                                        //TODO: edit the player
                                        /**TEMP*/
                                        Toast.makeText(
                                                activity, "Editor not implemented yet", Toast.LENGTH_LONG)
                                                .show();
                                    } else if (which == 2) {
                                        //TODO: export as files
                                        String name = p.getString("player" + position, "");
//                                        File file = new File(getCacheDir() + File.separator + name + ".dm");
//                                        ObjectOutputStream oos = null;
                                        SharedPreferences current = getSharedPreferences(name, MODE_PRIVATE);
                                        int n = current.getInt("powers", 0);
                                        Gson gson = new Gson();
                                        String export = gson.toJson(new Player(current));
                                        for (int i = 0; i < n; i++) {
                                            SharedPreferences sav = getSharedPreferences(current.getString("power" + i, ""), MODE_PRIVATE);
                                            export += SEPARATOR + gson.toJson(new Power(sav));
                                        }
                                        export += SEPARATOR;
                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, export);
                                        shareIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(shareIntent, "Compartir con..."));

//                                        try {
//                                            oos = new ObjectOutputStream(
//                                                    new BufferedOutputStream(
//                                                            new FileOutputStream(string)
//                                                    )
//                                            );
//
//                                            SharedPreferences current = getSharedPreferences(name, MODE_PRIVATE);
//                                            oos.writeObject(new Player(current));
//                                            int n = current.getInt("powers", 0);
//                                            for (int i = 0; i < n; i++) {
//                                                SharedPreferences sav = getSharedPreferences(current.getString("power" + i, ""), MODE_PRIVATE);
//                                                oos.writeObject(new Power(sav));
//                                            }
//                                          TODO: fix URI share action (needs a FileProvider)
//                                            Intent shareIntent = new Intent();
//                                            shareIntent.setAction(Intent.ACTION_SEND);
//                                            shareIntent.putExtra(Intent.EXTRA_TEXT, string);
//                                            shareIntent.setType("text/*");
//                                            startActivity(Intent.createChooser(shareIntent, "Compartir con..."));
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        } finally {
//                                            if (oos != null) try {
//                                                oos.close();
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
                                    } else {
                                        /**TEMP*/
                                        startActivity(new Intent(
                                                getApplicationContext(), Display.class
                                        ).putExtra("player", position));
                                    }
                            }
                }

                );
                alert.show();
                return true;
            }
        });
        } else {
            playerList.setVisibility(View.GONE);
            findViewById(R.id.help_text).setVisibility(View.GONE);
            findViewById(R.id.no_players_text).setVisibility(View.VISIBLE);
        }
    }
}
