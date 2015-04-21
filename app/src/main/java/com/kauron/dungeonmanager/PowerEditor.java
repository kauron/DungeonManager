package com.kauron.dungeonmanager;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;


public class PowerEditor extends ActionBarActivity {

    public static final String NAME="name", FREQ="freq", KEYWORDS="keywords", RANGE="range", DISTANCE="distance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_editor);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    public void saveClick(View view) {
        //TODO: save powers
    }
}
