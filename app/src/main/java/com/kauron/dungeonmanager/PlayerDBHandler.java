package com.kauron.dungeonmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlayerDBHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "dnd.db";

    public static final String TABLE_PLAYERS = "players";
    public static final String C_ID="_id", C_NAME="name", C_CLASS="class", C_RACE="race";
    public static final String C_HP ="hp", C_HP_MAX ="hpMax", C_SURGES="surges", C_SURGES_MAX="surgesMax";
    public static final String C_XP = "px", C_ATK = "atk", C_ABILITY = "ability", C_POWER_ID="powerID";

    //TODO: complete class and implement database to save the players instead of sharedprefs
    //TODO: not using it at the moment
    public PlayerDBHandler(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        onCreate(db);
    }

    public void addPlayer(Player p) {

    }

    public void updatePlayer (Player p) {

    }

    public void deletePlayer (Player p) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
                "DELETE FROM " + TABLE_PLAYERS + " WTHERE " //TODO: complete
        );
    }
}
