package com.kauron.dungeonmanager;

import android.content.SharedPreferences;
import java.io.Serializable;

class Player implements Serializable {

    public static final String NAME = "playerName", CLASS = "classInt", RACE = "raceInt", PX = "px";

    /**
     * Names for the classes
     */
    public static final String[] CLASS_STRINGS = {
            "Clase", "Ardiente", "Brujo", "Buscador", "Clérigo", "Explorador",
            "Guerrero", "Mago", "Mente de Batalla", "Monje", "Paladín", "Pícaro", "Psiónico",
            "Sacerdote Rúnico", "Señor de la guerra"
    };

    public static final int NULL = 0;

//    /**
//     * Values for classes
//     */
//    public static final int ARDIENTE = 1, BRUJO = 2, BUSCADOR = 3, CLÉRIGO = 4,
//            EXPLORADOR = 5, GUERRERO = 6, MAGO = 7, MENTE_DE_BATALLA = 8, MONJE = 9, PALADÍN = 10,
//            PÍCARO = 11, PSIÓNICO = 12, SACERDOTE_RÚNICO = 13, SEÑOR_DE_LA_GUERRA = 14;

    /**
     * Values for level - px computation
     */
    public static final int[] LEVEL_PX = new int[]{
            0, 1000, 2250, 3750, 5500, 7500, 10000, 13000, 16500, 20500, 26000,
            32000, 39000, 47000, 57000, 69000, 83000, 99000, 119000, 143000, 175000,
            210000, 255000, 310000, 375000, 450000, 550000, 675000, 825000, 1000000
    };

    /**
     * Values for each class' characteristics
     */
    public static final int[][] CLASS_STATS = new int[][] {
            //pg on level up
            {0, 5, 5, 5, 5, 5, 6, 4, 6, 5, 6, 5, 4, 5, 5},
            //defenses bonus: fort, ref, vol
            {0, 1, 0, 0, 0, 1, 2, 0, 0, 1, 1, 0, 0, 0, 1},
            {0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 2, 0, 0, 0},
            {0, 1, 1, 1, 2, 0, 0, 2, 2, 1, 1, 0, 2, 2, 1},
            //initial pg bonus
            {0,12,12,12,12,12,15,10,15,12,15,12,12,12,12},
            //daily curative efforts
            {0, 7, 6, 7, 7, 6, 9, 6, 9, 7,10, 6, 6, 7, 7}
    };

    /**
     * Identifiers for each class characteristic
     */
    public static final int PG_ON_LEVEL_UP = 0, DEF_FORT = 1, DEF_REF = 2, DEF_VOL = 3,
        INITIAL_PG = 4, DAILY_CURATIVE_EFFORTS = 5;

    /**
     * Names for the races
     */
    public static final String[] RACE_STRINGS = new String[] {
            "Raza", "Dracónido", "Eladrín", "Elfo", "Enano", "Gitzherai", "Humano", "Mediano",
            "Mente del Fragmento", "Minotauro", "Salvaje", "Semielfo", "Tiflin"
    };

    /**
     * Values for attack
     */
    public static final int FUE = 0, CON = 1, DES = 2, INT = 3, SAB = 4, CAR = 5;

    /**
     * Values for defenses
     */
    public static final int CA = 0, FORT = 1, REF = 2, VOL = 3;

    //TODO: develop abilities like attacks, with a popup
    //could be introduced by the player in a introduction screen, with ticks for train and bonuses
//    /**
//     * Values for abilities
//     */
//    public static final int ACROBACIAS = 1, AGUANTE = 2, ARCANOS = 3, ATLETISMO = 4, DIPLOMACIA = 5,
//        DUNGEONS = 6, ENGAÑAR = 7, HISTORIA  = 8, HURTO = 9, INTIMIDAR = 10, NATURALEZA = 11,
//        PERCEPCIÓN = 12, PERSPICACIA = 13, RECURSOS = 14, RELIGIÓN = 15, SANAR = 16, SIGILO = 17;

    /**
     * Names for the abilities
     */
    public static final String[] ABILITY_STRING = new String[] {
            "Habilidades", "Acrobacias", "Aguante", "Arcanos", "Atletismo", "Diplomacia", "Dungeons", "Engañar",
            "Historia", "Hurto", "Intimidar", "Naturaleza", "Percepción", "Perspicacia", "Recursos",
            "Religión", "Sanar", "Sigilo"
    };

    public static final int[] ABILITY_BOOST = new int[] {
            -1, DES, CON, INT, FUE, CAR, SAB, CAR, INT, CAR, DES, SAB, SAB, SAB, CAR, INT, SAB, DES
    };

    /**
     * Values for the current living state
     */
    public static final int OK = 1, MALHERIDO = 2, DEBILITADO = 3, MUERTO = 4, SAME = 5,
            USE_CURATIVE_EFFORT = -1, CURED = 1, NOT_CURED = 0, MAXED = -1;

    private int pg, maxPg, px;
    private int state, lastState;
    private int curativeEfforts, maxCurativeEfforts;
    private int classInt, raceInt;
    private String name;
    private int level;
    //TODO: use dice dialogs
    private int[] atk, def;
    //TODO: implement fully operational powers displayed as cards

    Player (SharedPreferences p) {
        this.name = p.getString(NAME, "Player");
        this.px   = p.getInt(PX, 0);
        setLevel();
        this.raceInt = p.getInt(RACE, 0);
        this.classInt = p.getInt(CLASS, 0);
        this.def = new int[4];
        setAtk(new int[] {
                p.getInt("fue", 10),
                p.getInt("con", 10),
                p.getInt("des", 10),
                p.getInt("int", 10),
                p.getInt("sab", 10),
                p.getInt("car", 10)}
        );
        setState();
        this.pg = p.getInt( "pg" , maxPg);
        this.curativeEfforts = p.getInt( "curativeEfforts" , maxCurativeEfforts );
    }

    int getPx() {return px;}
    void setPx (int px) {this.px = px; setLevel();}
    boolean addPx(int px) {
        int lastLevel = level;
        setPx(this.px + px);
        return lastLevel < level;
    }

    int getMaxCurativeEfforts() {return maxCurativeEfforts;}
    void setMaxCurativeEfforts(int maxCurativeEfforts) {this.maxCurativeEfforts = maxCurativeEfforts;}

    int getCurativeEfforts() {return curativeEfforts;}
    void setCurativeEffort(int curativeEfforts) {this.curativeEfforts = curativeEfforts;}

    int getLevel() {return level;}
    void setLevel() {
        for (int i = 0; i < LEVEL_PX.length; i++)
            if(px < LEVEL_PX[i]) {level = i; return;}
        level = LEVEL_PX.length;
    }


    int getMaxPg() {return maxPg;}
    void setMaxPg(int maxPg) {
        if(this.maxPg == 0)
            this.pg = maxPg;
        this.maxPg = maxPg;
    }
    void setMaxPgOnLevelUp() {maxPg += CLASS_STATS[PG_ON_LEVEL_UP][classInt];}

    int getPg() {return pg;}
    void setPg(int pg) {this.pg = pg; setState();}
    void losePg(int damage) {
        pg -= damage;
        setState();
    }
    int recoverPg(int recovered, boolean uses) {
        if(recovered == USE_CURATIVE_EFFORT){
            if(uses && curativeEfforts <= 0) return NOT_CURED;
            else {
                if(uses && pg < maxPg) curativeEfforts--;
                if (pg < 0) {
                    pg = 0;
                } else {
                    pg += maxPg / 4;
                }
            }
        } else {
            if (pg < 0) {
                pg = 0;
            } else {
                pg += recovered;
            }
        }
        setState();

        if (pg > maxPg) {pg = maxPg; return MAXED;}

        return CURED;
    }

    int getLastState() {return lastState == state ? SAME : lastState;}
    int getState() {return state;}
    private void setState() {
        lastState = state;
        if (pg <= maxPg / -2) state = MUERTO;
        else if (pg <= 0) state = DEBILITADO;
        else if(pg <= maxPg / 2) state = MALHERIDO;
        else state = OK;
    }

    String getName() {return name;}
    String getClassName() {return CLASS_STRINGS[classInt];}
    String getRaceName() {return RACE_STRINGS[raceInt];}
    void setRaceInt(int raceInt) {this.raceInt = raceInt;}
    int getRaceInt() {return raceInt;}

    void setAtk(int[] atk) {this.atk = atk; if(classInt != NULL) setClass();}

    int getFue() {return atk[FUE];}
    int getCon() {return atk[CON];}
    int getSab() {return atk[SAB];}
    int getCar() {return atk[CAR];}
    int getDes() {return atk[DES];}
    int getInt() {return atk[INT];}
    int getCa() {return def[CA];}
    int getFort() {return def[FORT];}
    int getRef() {return def[REF];}
    int getVol() {return def[VOL];}

    void setClass() {
        if(level == 1) maxPg = atk[CON] + CLASS_STATS[INITIAL_PG][classInt];
        maxCurativeEfforts = Player.getModifier(atk[CON]) + CLASS_STATS[DAILY_CURATIVE_EFFORTS][classInt];
        //TODO: implement armor!
        def[CA] = 10 + level / 2 + Math.max(0, Player.getModifier(Math.max(atk[DES], atk[INT])));
        def[FORT] = 10 + level / 2 + Player.getModifier(Math.max(atk[CON], atk[FUE])) +
                CLASS_STATS[DEF_FORT][classInt];
        def[REF] = 10 + level / 2 + Player.getModifier(Math.max(atk[DES], atk[INT])) +
                CLASS_STATS[DEF_REF][classInt];
        def[VOL] = 10 + level / 2 + Player.getModifier(Math.max(atk[CAR], atk[SAB])) +
                CLASS_STATS[DEF_VOL][classInt];
    }

    static int getModifier(int i) {
        return i / 2 - 5;
    }
}