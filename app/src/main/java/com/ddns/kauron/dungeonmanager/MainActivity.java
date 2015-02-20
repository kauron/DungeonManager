package com.ddns.kauron.dungeonmanager;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity
                        implements HealthDialogFragment.HealthDialogListener{

    public static final int CURRENT_PG = 1, NULL = 0;

    public Player player;
    private boolean undo;
    private int undoObject, undoPreviousValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restoreData();
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
            showHealthDialog();
            return true;
        } else if (id == R.id.action_edit_basics) {
            //TODO: try this startChildActivity()
            SharedPreferences p = getSharedPreferences("basics", MODE_PRIVATE);
            Intent intent = new Intent(this, Introduction.class);
            startActivity(intent.putExtra(
                    "first_time",
                    !p.getBoolean("saved", false)
            ));
            restoreData();
            return true;
        } else if (id == R.id.action_undo) {
            String message = "";
            if(undoObject == CURRENT_PG){
                ((Button) findViewById(R.id.pgCurrent)).setText(String.valueOf(undoPreviousValue));
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
            return true;
        } else if (id == R.id.action_calendar_activity) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("title", R.string.dungeons_and_dragons);
            startActivity(intent);
        } else if (id == R.id.action_save) {
            saveData();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showHealthDialog(){
        DialogFragment dialog = HealthDialogFragment.newInstance(player.getCurativeEfforts());
        dialog.show(getFragmentManager(), "HealthDialogFragment");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("UTIL", "pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("UTIL", "resume");
        restoreData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("UTIL", "stop");
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
        Log.e("UTIL", "destroy");
    }

    @Override
    public void curativeEffort(DialogFragment dialog, boolean uses) {
        int hasCured = player.recoverPg(Player.USE_CURATIVE_EFFORT, uses);
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
            healthStatusCheck();
        }
    }

    public void onCurrentPgClick(final View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.suffer_damage));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.suffer_damage_hint);


        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Button pg = (Button) findViewById(R.id.pgCurrent);
                try {
                    int preValue = Integer.parseInt(pg.getText().toString());
                    int damage = Integer.parseInt(input.getText().toString());
                    player.losePg(damage);
                    pg.setText(
                            String.valueOf(player.getPg())
                    );
                    //finished correctly, then apply values to undo's
                    healthStatusCheck();
                    undo = true;
                    undoPreviousValue = preValue;
                    undoObject = CURRENT_PG;
                    invalidateOptionsMenu();
                } catch (Exception e) {}
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void healthStatusCheck(){
        int status = player.getState();
        Button pg = (Button) findViewById(R.id.pgCurrent);
        pg.setText(String.valueOf(player.getPg()));
        if (status == Player.MUERTO) {
            pg.setText("");
        } else if (status == Player.DEBILITADO)
            pg.setTextColor(Color.RED);
        else if (status == Player.MALHERIDO)
            pg.setTextColor(Color.YELLOW);
        else pg.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
    }


    private void restoreData(){
        SharedPreferences p = getSharedPreferences("basics", MODE_PRIVATE);
        //restore state
        if(player == null) {
            player = new Player(
                    p.getString("playerName", getString(R.string.adventurer_name)),
                    p.getString("className", getString(R.string.class_name)),
                    p.getString("raceName", getString(R.string.race_name)),
                    p.getInt("level", 1),
                    p.getInt("maxPg", 15),
                    p.getInt("pg", 15),
                    p.getInt("maxCurativeEfforts", 5),
                    p.getInt("curativeEfforts", 5),
                    new int[6],
                    new int[3],
                    new int[18],
                    new Power[4]);
        } else {

        }
        //set restored values to the respective fields
        ((TextView) findViewById(R.id.nameText)).setText(player.getName());
        ((TextView) findViewById(R.id.raceText)).setText(player.getRaceName());
        ((TextView) findViewById(R.id.classText)).setText(player.getClassName());
        ((TextView) findViewById(R.id.lvl)).setText(String.valueOf(player.getLevel()));
        ((Button) findViewById(R.id.pgCurrent)).setText(String.valueOf(player.getMaxPg()));
    }

    private void saveData() {
        getSharedPreferences("basics", MODE_PRIVATE).edit()
                .putString("playerName", player.getName())
                .putString("className", player.getClassName())
                .putString("raceName", player.getRaceName())
                .putInt("level", player.getLevel())
                .putInt("maxPg", player.getMaxPg())
                .putInt("pg", player.getPg())
                .putInt("maxCurativeEfforts", player.getMaxCurativeEfforts())
                .putInt("curativeEfforts", player.getCurativeEfforts())
                .apply();
    }
}