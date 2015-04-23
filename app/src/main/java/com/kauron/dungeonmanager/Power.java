package com.kauron.dungeonmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.io.Serializable;

class Power implements Serializable{
    /**frequencies*/
    public static final int OPORTUNIDAD = 1, A_VOLUNTAD = 2, ENCUENTRO = 3, DIARIO = 4;
    public static final String[] FREQ = {"Nada", "Oportunidad", "A voluntad", "Encuentro", "Diario"};
    /**actions*/
    public static final int ESTANDAR = 1, MOVIMIENTO = 2, MENOR = 3, GRATUITA = 4;
    public static final String[] ACTIONS = {"Nada", "Estándar", "Movimiento", "Menor", "Gratuita"};
    /**distances*/
    public static final int CUERPO_A_CUERPO = 1, A_DISTANCIA = 2, EXPLOSION = 3, ESTALLIDO = 4;
    public static final String[] DISTANCES = {"Nada", "Cuerpo a cuerpo", "A distancia", "Explosión", "Estallido"};
    /**dies
     * They are represented by its max size
     * 0 corresponds to [A], the weapon
     * any other is represented by [A], d2, d4, d6, d8, d10, d12, d20, d100, extra damage
     */
    public static final int[] DIE = {0, 2, 4, 6, 8, 10, 12, 20, 100, 0};

    private boolean used;
    private int freq, action, distance, range, objectives;
    private String name, impact, objective;
    private String keywords; //fire, spell...
    private int atk, def; //constants from Player to denote atk and defense

    Power ( SharedPreferences p ) {
        this.name      = p.getString("s0", "Name");
        this.keywords  = p.getString("s1", "Keywords");
        this.impact    = p.getString("s2", "2d10");
        this.distance  = Integer.parseInt(p.getString("s3", "10"));
        this.objective = p.getString("s4", "One creature");

        this.used = p.getBoolean("used", false);

        this.freq   = p.getInt("i0", 0);
        this.range  = p.getInt("i1", 0);
        this.atk    = p.getInt("i2", 0);
        this.def    = p.getInt("i3", 0);
        this.action = p.getInt("i4", 0);
    }

    String getKeywords() {return keywords;}

    String getTypeString() {return ACTIONS[action];}
    String getFrequencyString() {return FREQ[freq];}
    String getRangeString() {return DISTANCES[range];}

    int getAtk() {return atk;}
    int getDef() {return def;}
    int getFreq() {return freq;}

    int getDistance() {return distance;}

    String getName(){return name;}
    String getImpact() {return impact;}
    String getObjective() {return objective;}
    String getOther() {return "";}

    //TODO: add other element to include further description

    boolean isUsed(){return used;}

    boolean use(){
        if (!used) {
            if (freq >= ENCUENTRO) used = true;
            return true;
        } else {return false;}
    }

    int rollAttack() {return atk + (int)(Math.random()*20) + 1;}

    void recover(int type){
        if(this.freq <= type) used = false;
    }

    int getFreqColor(Context context) {
        switch (freq) {
            case DIARIO:
                return context.getResources().getColor(R.color.daily);
            case ENCUENTRO:
                return context.getResources().getColor(R.color.encounter);
            case A_VOLUNTAD:
                return context.getResources().getColor(R.color.at_will);
            default:
                return context.getResources().getColor(R.color.green); //TODO: find other color
        }
    }
}