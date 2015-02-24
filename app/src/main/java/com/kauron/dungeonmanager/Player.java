package com.kauron.dungeonmanager;

public class Player {

    /**
     * Names for the classes
     */
    public static final String[] CLASS_STRINGS = {
            "Clase", "Ardiente", "Brujo", "Buscador", "Clérigo", "Explorador",
            "Guerrero", "Mago", "Mente de Batalla", "Monje", "Paladín", "Pícaro", "Psiónico",
            "Sacerdote Rúnico", "Señor de la guerra"
    };

    /**
     * Values for classes
     */
    public static final int NULL = 0, ARDIENTE = 1, BRUJO = 2, BUSCADOR = 3, CLÉRIGO = 4,
            EXPLORADOR = 5, GUERRERO = 6, MAGO = 7, MENTE_DE_BATALLA = 8, MONJE = 9, PALADÍN = 10,
            PÍCARO = 11, PSIÓNICO = 12, SACERDOTE_RÚNICO = 13, SEÑOR_DE_LA_GUERRA = 14;

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
            "Raza", "Dracónido", "Eladrín", "Elfo", "Enano", "Gitzherai", "Humanos", "Medianos",
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

    //TODO: develop abilities
    /**
     * Values for abilities
     */
    public static final int ACROBACIAS = 1, AGUANTE = 2, ARCANOS = 3, ATLETISMO = 4, DIPLOMACIA = 5,
        DUNGEONS = 6, ENGAÑAR = 7, HISTORIA  = 8, HURTO = 9, INTIMIDAR = 10, NATURALEZA = 11,
        PERCEPCIÓN = 12, PERSPICACIA = 13, RECURSOS = 14, RELIGIÓN = 15, SANAR = 16, SIGILO = 17;

    /**
     * Names for the abilities
     */
    public static final String[] ABILITY_STRING = new String[] {
            "Habilidades", "Acrobacias", "Aguante", "Arcanos", "Atletismo", "Diplomacia", "Dungeons", "Engañar",
            "Historia", "Hurto", "Intimidar", "Naturaleza", "Percepción", "Perspicacia", "Recursos",
            "Religión", "Sanar", "Sigilo"
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

    private int[] atk, def, abilities;
    private Power[] powers;

    public Player(
            String name, int classInt, int raceInt,
            int px, int[] atk, int[] abilities,
            Power[] powers
    ){
        this.name = name;
        this.px = px;
        setLevel();
        this.raceInt = raceInt;
        this.classInt = classInt;
        this.def = new int[4];
        setAtk(atk);
        setState();
        pg = maxPg;
        curativeEfforts = maxCurativeEfforts;

        this.abilities = abilities;
        this.powers = powers;
    }


    public int getPx() {return px;}
    public void setPx (int px) {this.px = px; setLevel();}
    public boolean addPx(int px) {
        int lastLevel = level;
        setPx(this.px + px);
        return lastLevel < level;
    }

    public int getMaxCurativeEfforts() {return maxCurativeEfforts;}
    public void setMaxCurativeEfforts(int maxCurativeEfforts) {this.maxCurativeEfforts = maxCurativeEfforts;}

    public int getCurativeEfforts() {return curativeEfforts;}
    public void setCurativeEffort(int curativeEfforts) {this.curativeEfforts = curativeEfforts;}

    public int getLevel() {return level;}
    public void setLevel() {
        for (int i = 0; i < LEVEL_PX.length; i++){
            if(px < LEVEL_PX[i]) {
                level = i; return;
            }
        }
        level = LEVEL_PX.length;
        //TODO: substitute level by px and autoconvert
    }


    public int getMaxPg() {return maxPg;}
    public void setMaxPg(int maxPg) {
        if(this.maxPg == 0)
            this.pg = maxPg;
        this.maxPg = maxPg;
    }
    public void setMaxPgOnLevelUp() {maxPg += CLASS_STATS[PG_ON_LEVEL_UP][classInt];}

    public int getPg() {return pg;}
    public void setPg(int pg) {this.pg = pg; setState();}
    public void losePg(int damage) {
        pg -= damage;
        setState();
    }
    public int recoverPg(int recovered, boolean uses) {
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

    public int getLastState() {return lastState == state ? SAME : lastState;}
    public int getState() {return state;}
    private void setState() {
        lastState = state;
        if (pg <= maxPg / -2) state = MUERTO;
        else if (pg <= 0) state = DEBILITADO;
        else if(pg <= maxPg / 2) state = MALHERIDO;
        else state = OK;
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public int getClassInt() {return classInt;}
    public void setClassInt(int classInt) {
        this.classInt = classInt;
        if (atk != null) setClass();
    }
    public String getClassName() {return CLASS_STRINGS[classInt];}

    public String getRaceName() {return RACE_STRINGS[raceInt];}
    public void setRaceInt(int raceInt) {this.raceInt= raceInt;}
    public int getRaceInt() {return raceInt;}

    //TODO: implement time in the app
    public void rest(boolean length) {
        if(length) {
            curativeEfforts = maxCurativeEfforts;
            for(Power p : powers){
                if(p != null) p.recover();
            }
        }
    }

    public void setAtk(int[] atk) {this.atk = atk; if(classInt != NULL) setClass();}

    public int getFue() {return atk[FUE];}
    public int getCon() {return atk[CON];}
    public int getSab() {return atk[SAB];}
    public int getCar() {return atk[CAR];}
    public int getDes() {return atk[DES];}
    public int getInt() {return atk[INT];}
    public int getCa() {return def[CA];}
    public int getFort() {return def[FORT];}
    public int getRef() {return def[REF];}
    public int getVol() {return def[VOL];}

    public void setClass() {
        if(level == 1) maxPg = atk[CON] + CLASS_STATS[INITIAL_PG][classInt];
        maxCurativeEfforts = Player.getModifier(atk[CON]) + CLASS_STATS[DAILY_CURATIVE_EFFORTS][classInt];
        //TODO: fix ca bonuses!
        def[CA] = 10 + level / 2;
        def[FORT] = 10 + level / 2 + Player.getModifier(Math.max(atk[CON], atk[FUE])) +
                CLASS_STATS[DEF_FORT][classInt];
        def[REF] = 10 + level / 2 + Player.getModifier(Math.max(atk[DES], atk[INT])) +
                CLASS_STATS[DEF_REF][classInt];
        def[VOL] = 10 + level / 2 + Player.getModifier(Math.max(atk[CAR], atk[SAB])) +
                CLASS_STATS[DEF_VOL][classInt];
    }

    public static int getModifier(int i) {
        return i / 2 - 5;
    }

    public int getTotalModifier(int i) {
        return getModifier(i) + level / 2;
    }
}