package com.kauron.dungeonmanager;

import android.content.SharedPreferences;
import android.content.res.Resources;

import java.io.Serializable;

class Player implements Serializable {

    public static final String NAME = "playerName", CLASS = "classInt", RACE = "raceInt", XP = "px";

    /**
     * Names for the classes
     */
    public static String[] CLASS_STRINGS;

//            "Clase", "Ardiente", "Brujo", "Buscador", "Clérigo", "Explorador",
//            "Guerrero", "Mago", "Mente de Batalla", "Monje", "Paladín", "Pícaro", "Psiónico",
//            "Sacerdote Rúnico", "Señor de la guerra"

    public static final int NULL = 0;

    /**
     * Values for level - xp computation
     */
    public static final int[] LEVEL_XP = new int[]{
            0, 1000, 2250, 3750, 5500, 7500, 10000, 13000, 16500, 20500, 26000,
            32000, 39000, 47000, 57000, 69000, 83000, 99000, 119000, 143000, 175000,
            210000, 255000, 310000, 375000, 450000, 550000, 675000, 825000, 1000000
    };

    /**
     * Values for each class' characteristics
     */
    public static final int[][] CLASS_STATS = new int[][] {
            //hp on level up
            {0, 5, 5, 5, 5, 5, 6, 4, 6, 5, 6, 5, 4, 5, 5},
            //defenses bonus: fort, ref, will
            {0, 1, 0, 0, 0, 1, 2, 0, 0, 1, 1, 0, 0, 0, 1},
            {0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 2, 0, 0, 0},
            {0, 1, 1, 1, 2, 0, 0, 2, 2, 1, 1, 0, 2, 2, 1},
            //initial hp bonus
            {0,12,12,12,12,12,15,10,15,12,15,12,12,12,12},
            //daily curative efforts
            {0, 7, 6, 7, 7, 6, 9, 6, 9, 7,10, 6, 6, 7, 7}
    };

    /**
     * Identifiers for each class characteristic
     */
    public static final int HP_ON_LEVEL_UP = 0, DEF_FORT = 1, DEF_REF = 2, DEF_WILL = 3,
        INITIAL_HP = 4, DAILY_SURGES = 5;

    /**
     * Names for the races
     */
    public static String[] RACE_STRINGS;

//            "Raza", "Dracónido", "Eladrín", "Elfo", "Enano", "Gitzherai", "Humano", "Mediano",
//            "Mente del Fragmento", "Minotauro", "Salvaje", "Semielfo", "Tiflin"

    /**
     * Values for attack
     */
    public static final int STR = 1, CON = 2, DEX = 3, INT = 4, WIS = 5, CHA = 6;

    /**
     * Values for defenses
     */
    public static final int AC = 1, FORT = 2, REF = 3, WILL = 4;

    //TODO: develop abilities like attacks, with a popup
    //could be introduced by the player in a introduction screen, with ticks for train and bonuses
    /**
     * Names for the abilities
     */
    public static String[] ABILITY_STRING = new String[] {
            "Habilidades", "Acrobacias", "Aguante", "Arcanos", "Atletismo", "Diplomacia", "Dungeons", "Engañar",
            "Historia", "Hurto", "Intimidar", "Naturaleza", "Percepción", "Perspicacia", "Recursos",
            "Religión", "Sanar", "Sigilo"
    }; //TODO: move the names to a string-array in the res/values folder

    public static final int[] ABILITY_BOOST = new int[] {
            -1, DEX, CON, INT, STR, CHA, WIS, CHA, INT, CHA, DEX, WIS, WIS, WIS, CHA, INT, WIS, DEX
    };

    /**
     * Values for the current living state
     */
    public static final int OK = 1, BLOODIED = 2, DYING = 3, DEAD = 4, SAME = 5,
            USE_CURATIVE_EFFORT = -1, CURED = 1, NOT_CURED = 0, MAXED = -1;

    private int hp, maxHp, xp;
    private int state, lastState;
    private int surges, maxSurges;
    private int classInt, raceInt;
    private String name;
    private int level;
    private int[] atk, def;
    //TODO: implement fully operational powers (die rolling)

    /**
     * Builds a whole player from its saved stats
     * @param p SharedPreferences object containing data for a player.
     */
    Player (SharedPreferences p) {
        this.name = p.getString(NAME, "Player");
        this.xp = p.getInt(XP, 0);
        setLevel();
        this.raceInt = p.getInt(RACE, 0);
        this.classInt = p.getInt(CLASS, 0);
        this.def = new int[5];
        setAtk(new int[] {
                0,
                p.getInt("fue", 10),
                p.getInt("con", 10),
                p.getInt("des", 10),
                p.getInt("int", 10),
                p.getInt("sab", 10),
                p.getInt("car", 10)}
        );
        setState();
        this.hp = p.getInt( "pg" , maxHp);
        this.surges = p.getInt( "curativeEfforts" , maxSurges);
    }

    public static void setStrings ( Resources res ) {
        CLASS_STRINGS  = res.getStringArray(R.array.classes);
        RACE_STRINGS   = res.getStringArray(R.array.races);
        ABILITY_STRING = res.getStringArray(R.array.abilities);
    }

    int getXp() {return xp;}
    void setXp(int xp) {this.xp = xp; setLevel();}
    boolean addPx(int xp) {
        int lastLevel = level;
        setXp(this.xp + xp);
        return lastLevel < level;
    }

    int getMaxSurges() {return maxSurges;}
    void setMaxSurges(int maxSurges) {this.maxSurges = maxSurges;}

    int getSurges() {return surges;}
    void setCurativeEffort(int surges) {this.surges = surges;}

    int getLevel() {return level;}
    void setLevel() {
        for (int i = 0; i < LEVEL_XP.length; i++)
            if(xp < LEVEL_XP[i]) {level = i; return;}
        level = LEVEL_XP.length;
    }


    int getMaxHp() {return maxHp;}
    void setMaxHp(int maxHp) {
        if(this.maxHp == 0)
            this.hp = maxHp;
        this.maxHp = maxHp;
    }

    int getHp() {return hp;}
    void setHp(int hp) {this.hp = hp; setState();}
    void losePg(int damage) {
        hp -= damage;
        setState();
    }
    int recoverPg(int recovered, boolean uses) {
        if(recovered == USE_CURATIVE_EFFORT){
            if(uses && surges <= 0) return NOT_CURED;
            else {
                if(uses && hp < maxHp) surges--;
                if (hp < 0) {
                    hp = 0;
                } else {
                    hp += maxHp / 4;
                }
            }
        } else {
            if (hp < 0) {
                hp = 0;
            } else {
                hp += recovered;
            }
        }
        setState();

        if (hp > maxHp) {
            hp = maxHp; return MAXED;}

        return CURED;
    }

    int getLastState() {return lastState == state ? SAME : lastState;}
    int getState() {return state;}
    private void setState() {
        lastState = state;
        if (hp <= maxHp / -2) state = DEAD;
        else if (hp <= 0) state = DYING;
        else if(hp <= maxHp / 2) state = BLOODIED;
        else state = OK;
    }

    String getName() {return name;}
    String getClassName() {return CLASS_STRINGS[classInt];}
    String getRaceName() {return RACE_STRINGS[raceInt];}

    private void setAtk(int[] atk) {this.atk = atk; if(classInt != NULL) setClass();}

    int[] getAtk() {return atk;}
    int[] getDef() {return def;}

    private void setClass() {
        maxHp = atk[CON] + CLASS_STATS[INITIAL_HP][classInt]
                + ( level - 1 ) * CLASS_STATS[HP_ON_LEVEL_UP][classInt];
        maxSurges =
                Player.getModifier(atk[CON]) + CLASS_STATS[DAILY_SURGES][classInt];
        //TODO: implement armor!
        def[AC] = 10 + level / 2 + Math.max(0, Player.getModifier(Math.max(atk[DEX], atk[INT])));
        def[FORT] = 10 + level / 2 + Player.getModifier(Math.max(atk[CON], atk[STR])) +
                CLASS_STATS[DEF_FORT][classInt];
        def[REF] = 10 + level / 2 + Player.getModifier(Math.max(atk[DEX], atk[INT])) +
                CLASS_STATS[DEF_REF][classInt];
        def[WILL] = 10 + level / 2 + Player.getModifier(Math.max(atk[CHA], atk[WIS])) +
                CLASS_STATS[DEF_WILL][classInt];
    }

    static int getModifier(int i) {
        return i / 2 - 5;
    }

    int getStatusColor(Resources res) {
        if (hp > maxHp / 2)
            return res.getColor(R.color.green);
        else if (hp > 0)
            return res.getColor(R.color.yellow);
        else if (hp > -maxHp / 2)
            return res.getColor(R.color.red);
        else
            return res.getColor(R.color.black);
    }

    void rest (boolean isLong) {
        if ( isLong ) {
            hp = maxHp;
            surges = maxSurges;
        }
        //TODO: here implement action points!
    }

    void saveToPreferences(SharedPreferences s) {
        s.edit().clear().commit();
        SharedPreferences.Editor e = s.edit();
        e.putString(NAME, name);
        e.putInt(XP, xp);
        e.putInt(RACE, raceInt);
        e.putInt(CLASS, classInt);
        e.putInt("fue", atk[STR]);
        e.putInt("con", atk[CON]);
        e.putInt("des", atk[DEX]);
        e.putInt("int", atk[INT]);
        e.putInt("sab", atk[WIS]);
        e.putInt("car", atk[CHA]);
        //TODO: defenses (add armor and other bonuses)
        e.putInt("pg", hp);
        e.putInt("curativeEfforts", surges);
        e.commit();
    }
}