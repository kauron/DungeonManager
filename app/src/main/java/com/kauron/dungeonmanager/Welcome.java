package com.kauron.dungeonmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Welcome extends ActionBarActivity {

    public static final String PREFERENCES = "basics";

    private SharedPreferences p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
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
            startActivity(new Intent(this, Introduction.class).putExtra("first_time", true));
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
        ListView playerList = (ListView) findViewById(R.id.listView);
        PlayerAdapter adapter = (PlayerAdapter) playerList.getAdapter();
        int elements = 0;
        if ( adapter != null )
            elements = adapter.getCount();
        if ( elements < n  && adapter != null ) {
            playerList.setVisibility(View.VISIBLE);
            findViewById(R.id.no_players_text).setVisibility(View.GONE);
            for ( int i = elements; i < n; i++ ) {
                SharedPreferences sav = getSharedPreferences("player" + i, MODE_PRIVATE);
                adapter.add(
                        new Player(
                                sav.getString(Player.NAME, "player" + i),
                                sav.getInt(Player.CLASS, 0),
                                sav.getInt(Player.RACE, 0),
                                sav.getInt(Player.PX, 0),
                                new int[] {
                                        sav.getInt("fue", 10),
                                        sav.getInt("con", 10),
                                        sav.getInt("des", 10),
                                        sav.getInt("int", 10),
                                        sav.getInt("sab", 10),
                                        sav.getInt("car", 10)
                                },
                                new int[18],
                                new Power[4]
                ));
            }
        } else if ( n != 0 ) {
            playerList.setVisibility(View.VISIBLE);
            findViewById(R.id.no_players_text).setVisibility(View.GONE);
            Player[] players = new Player[n];
            for ( int i = 0; i < n; i++ ) {
                //TODO: fill the information for the player creation
                SharedPreferences sav = getSharedPreferences("player" + i, MODE_PRIVATE);
                players[i] = new Player(
                        sav.getString(Player.NAME, "player" + i),
                        sav.getInt(Player.CLASS, 0),
                        sav.getInt(Player.RACE, 0),
                        sav.getInt(Player.PX, 0),
                        new int[] {
                                sav.getInt("fue", 10),
                                sav.getInt("con", 10),
                                sav.getInt("des", 10),
                                sav.getInt("int", 10),
                                sav.getInt("sab", 10),
                                sav.getInt("car", 10)
                        },
                        new int[18],
                        new Power[4]
                );
            }

            playerList.setAdapter(new PlayerAdapter(this, players));

            playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(
                            getApplicationContext(), MainActivity.class
                    ).putExtra("player", position));
                }
            });
        } else {
            playerList.setVisibility(View.GONE);
            findViewById(R.id.no_players_text).setVisibility(View.VISIBLE);
        }
    }
}
