package com.ddns.kauron.dungeonmanager;

public class Player {
    /**
     * Values for attack
     */
    public static final int FUE = 1, CON = 2, DES = 3, INT = 4, SAB = 5, CAR = 6;

    /**
     * Values for defenses
     */
    public static final int CA = 1, FORT = 2, REF = 3, VOL = 4;

    /**
     * Values for abilities
     */
    public static final int ACROBACIAS = 1, AGUANTE = 2, ARCANOS = 3, ATLETISMO = 4, DIPLOMACIA = 5,
        DUNGEONS = 6, ENGAÑAR = 7, HISTORIA  = 8, HURTO = 9, INTIMIDAR = 10, NATURALEZA = 11,
        PERCEPCIÓN = 12, PERSPICACIA = 13, RECURSOS = 14, RELIGIÓN = 15, SANAR = 16, SIGILO = 17;

    /**
     * Names for the abilities
     */
    public static final String[] abilityString = new String[] {
            "Acrobacias", "Aguante", "Arcanos", "Atletismo", "Diplomacia", "Dungeons", "Engañar",
            "Historia", "Hurto", "Intimidar", "Naturaleza", "Percepción", "Perspicacia", "Recursos",
            "Religión", "Sanar", "Sigilo"
    };

    /**
     * Values for the current living state
     */
    public static final int OK = 1, MALHERIDO = 2, DEBILITADO = 3, MUERTO = 4,
            USE_CURATIVE_EFFORT = -1, CURED = 1, NOT_CURED = 0, MAXED = -1;

    private int pg, maxPg;
    private int state;
    private int curativeEfforts, maxCurativeEfforts;
    private int[] atk, def, abilities;
    private Power[] powers;
    private String name, className, raceName;
    private int level;


    public Player(String name, String className, String raceName, int level, int maxPg, int pg,
                  int maxCurativeEfforts, int curativeEfforts, int[] atk, int[] def, int[] abilities, Power[] powers){
        this.maxPg = maxPg;
        this.pg = pg;
        setState();
        this.name = name;
        this.className = className;
        this.raceName = raceName;
        this.level = level;
        this.atk = atk;
        this.def = def;
        this.abilities = abilities;
        this.powers = powers;
        this.maxCurativeEfforts = maxCurativeEfforts;
        this.curativeEfforts = curativeEfforts;
    }


    public int getMaxCurativeEfforts() {return maxCurativeEfforts;}

    public int getCurativeEfforts() {return curativeEfforts;}
    public void setCurativeEffort(int curativeEfforts) {this.curativeEfforts = curativeEfforts;}

    public int getLevel() {return level;}

    public int getMaxPg() {return maxPg;}
    public void setMaxPg(int pg) {this.pg = pg;}

    public int getPg() {return pg;}
    public void setPg(int pg) {this.pg = pg;}
    public void losePg(int damage) {
        pg -= damage;
        setState();
    }
    public int recoverPg(int recovered, boolean uses) {
        if(recovered == USE_CURATIVE_EFFORT){
            if(uses && curativeEfforts <= 0) return NOT_CURED;
            else {
                if(uses) curativeEfforts--;
                pg += maxPg / 4;
            }
        } else {
            pg += recovered;
        }
        setState();

        if (pg > maxPg) {pg = maxPg; return MAXED;}

        return CURED;
    }

    public int getState() {return state;}
    private void setState() {
        if (pg < maxPg / -2) state = MUERTO;
        else if (pg < 0) state = DEBILITADO;
        else if(pg < maxPg / 2) state = MALHERIDO;
        else state = OK;
    }

    public String getName() {return name;}
    public String getClassName() {return className;}
    public String getRaceName() {return raceName;}


    public void rest(boolean length) {
        if(length) {
            curativeEfforts = maxCurativeEfforts;
            for(Power p : powers){
                if(p != null) p.recover();
            }
        }
    }
}