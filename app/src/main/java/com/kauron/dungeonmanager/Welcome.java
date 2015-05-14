package com.kauron.dungeonmanager;

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
import android.widget.ListView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.File;
import java.util.ArrayList;

public class Welcome extends ActionBarActivity {

    public static final String PREFERENCES = "basics";

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
            startActivity(new Intent(this, PlayerEditor.class).putExtra("first_time", true));
        }

        return super.onOptionsItemSelected(item);
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
                                    getString(R.string.export)},
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
                                    } else if(which==1) {
                                        //TODO: edit the player
                                        /**TEMP*/
                                        Toast.makeText(
                                                activity, "Editor not implemented yet", Toast.LENGTH_LONG)
                                                .show();
                                    } else {
                                        //TODO: export as files
                                        /**TEMP*/
                                        startActivity(new Intent(
                                                getApplicationContext(), Display.class
                                        ).putExtra("player", position));
                                        Toast.makeText(
                                                activity, "Exporting feature not implemented yet", Toast.LENGTH_LONG)
                                                .show();
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
