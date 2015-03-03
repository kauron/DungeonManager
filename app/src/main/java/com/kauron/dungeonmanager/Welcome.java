package com.kauron.dungeonmanager;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class Welcome extends ActionBarActivity {

    private Button load, newChar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        newChar = (Button) findViewById(R.id.newCharacter);
        load = (Button) findViewById(R.id.loadCharacter);
        load.setEnabled(
                getSharedPreferences("basics", MODE_PRIVATE).getBoolean("basics", false)
        );
    }

    //TODO: putBoolean in the intent correctly
    public void onNewClick(View view) {
        startActivity(new Intent(this, Introduction.class).putExtra("first_time", true));
    }

    //TODO: get correctly the state of the saved game
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
}
