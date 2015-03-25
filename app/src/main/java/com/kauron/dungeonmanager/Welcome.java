package com.kauron.dungeonmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;


public class Welcome extends ActionBarActivity {

    private Button load;
    private SharedPreferences p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        p = getSharedPreferences("basics", MODE_PRIVATE);
        load = (Button) findViewById(R.id.loadCharacter);
        if (p.getBoolean("saved", false)) {
            load.setEnabled(true);
            load.setText(String.format(getString(R.string.load_text), p.getString("playerName", "")));
        } else {
            load.setEnabled(false);
            load.setText(R.string.load_character);
        }
    }

    public void onNewClick(View view) {
        startActivity(new Intent(this, Introduction.class).putExtra("first_time", true));
    }

    public void onLoadClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
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
        if (id == R.id.action_settings) {
            return true;
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
