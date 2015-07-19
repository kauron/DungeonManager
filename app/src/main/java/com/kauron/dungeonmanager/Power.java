package com.kauron.dungeonmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.io.Serializable;

class Power implements Serializable{
    /**frequencies*/
    public static final int AT_WILL = 2, ENCOUNTER = 3, DAILY = 4;
    /*{"Nada", "A voluntad", "Encuentro", "Diario"}*/
    /**actions*/
    public static final int STANDARD = 1, MOVEMENT = 2, MINOR = 3, FREE = 4;
    /*{"Nada", "Estándar", "Movimiento", "Menor", "Gratuita"}*/
    /**distances*/
    public static final int MELEE = 1, RANGED = 2, EXPLOSION = 3, AREA = 4, CLOSE = 5, AURA = 6;
    /* {"Nada", "Cuerpo a cuerpo", "A distancia", "Explosión", "Estallido"};*/

    public static String[] RANGES, ACTIONS, FREQ, ATK, DEF, ATTACK, DEFENSE;
    /**dies
     * They are represented by its max size
     * 0 corresponds to [A], the weapon
     * any other is represented by [A], d2, d4, d6, d8, d10, d12, d20, d100, extra damage
     */
    public static final int[] DIE = {0, 2, 4, 6, 8, 10, 12, 20, 100, 0};

    private boolean used;
    private int freq, action, range;
    private String name, impact, objective, distance;
    private String keywords; //fire, spell...
    private int atk, def; //constants from Player to denote atk and defense

    Power ( SharedPreferences p ) {
        this.name      = p.getString("s0", "Name");
        this.keywords  = p.getString("s1", "Keywords");
        this.impact    = p.getString("s2", "2d10");
        this.distance  = p.getString("s3", "10");
        this.objective = p.getString("s4", "One creature");

        this.used = p.getBoolean("used", false);

        this.freq   = p.getInt("i0", 0);
        this.range  = p.getInt("i1", 0);
        this.atk    = p.getInt("i2", 0);
        this.def    = p.getInt("i3", 0);
        this.action = p.getInt("i4", 0);
    }

    static void setStrings(Resources res) {
        FREQ    = res.getStringArray(R.array.frequencies);
        ACTIONS = res.getStringArray(R.array.actions);
        RANGES  = res.getStringArray(R.array.ranges);
        ATK     = res.getStringArray(R.array.atk);
        DEF     = res.getStringArray(R.array.def);
        ATTACK  = res.getStringArray(R.array.attack);
        DEFENSE = res.getStringArray(R.array.defense);
    }

    String getKeywords() {return keywords;}

    String getActionString() {return ACTIONS[action];}
    String getFrequencyString() {return FREQ[freq];}
    String getRangeString() {return RANGES[range];}

    int getAtk() {return atk;}
    int getDef() {return def;}
    int getFreq() {return freq;}
    int getAction() {return action;}
    int getRange() {return range;}

    String getDistance() {return distance;}
    String getName(){return name;}
    String getImpact() {return impact;}
    String getObjective() {return objective;}
    String getOther() {return "";}

    //TODO: add other element to include further description

    boolean isUsed(){return used;}

    boolean use(){
        if (!used) {
            if (freq >= ENCOUNTER) used = true;
            return true;
        } else {return false;}
    }

    int rollAttack() {return atk + (int)(Math.random()*20) + 1;}

    void recover(int type){
        if(this.freq <= type) used = false;
    }
    void setUsed(boolean used) {this.used = used;}

    int getFreqColor(Context context) {
        switch (freq) {
            case DAILY:
                return context.getResources().getColor(R.color.daily);
            case ENCOUNTER:
                return context.getResources().getColor(R.color.encounter);
            default:
                return context.getResources().getColor(R.color.at_will);
        }
    }
}