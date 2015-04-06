package com.kauron.dungeonmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;


public class Welcome extends ActionBarActivity {

    public static final String PREFERENCES = "basics";

    private Button load;
    private SharedPreferences p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        p = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        load = (Button) findViewById(R.id.loadCharacter);
        if (p.getBoolean("saved", false)) {
            load.setEnabled(true);
            load.setText(String.format(getString(R.string.load_text), p.getString("playerName", "")));
        } else {
            load.setEnabled(false);
            load.setText(R.string.load_character);
        }

        int n = p.getInt("players",0);
        if ( n != 0 ) {
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
            ListView playerList = (ListView) findViewById(R.id.listView);
            ListAdapter adapter = new PlayerAdapter(this, players);
            playerList.setAdapter(adapter);

            playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(
                            getApplicationContext(), MainActivity.class
                    ).putExtra("player", position));
                }
            });
        } else {
            findViewById(R.id.listView).setVisibility(View.GONE);
        }
    }

    public void onNewClick(View view) {startActivity(new Intent(this, Introduction.class).putExtra("first_time", true));}
    public void onLoadClick(View view) {startActivity(new Intent(this, MainActivity.class).putExtra("player", -1));}

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
        if (id == R.id.action_settings) {
            SnackbarManager.show(
                    Snackbar.with(getApplicationContext()).text("This doesn't work yet")
            );
            return true;
        } else if ( id == R.id.action_add_player ) {
            startActivity(new Intent(this, Introduction.class).putExtra("first_time", true));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (p.getBoolean("saved", false)) {
            load.setEnabled(true);
            load.setText(String.format(getString(R.string.load_text), p.getString("playerName", "")));
        } else {
            load.setEnabled(false);
            load.setText(R.string.load_character);
        }
    }
}
