package com.ddns.kauron.dungeonmanager;

public class Player {

    /**
     * Names for the classes
     */
    public static final String[] classStrings = {
            "Clase", "Ardiente", "Brujo", "Buscador", "Clérigo", "Explorador",
            "Guerrero", "Mago", "Mente de Batalla", "Monje", "Paladín", "Pícaro", "Psiónico",
            "Sacerdote Rúnico", "Señor de la guerra"
    };

    /**
     * Names for the races
     */
    public static final String[] raceStrings = new String[] {
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
    //TODO: convert race and class to integer values
    private int classInt, raceInt;
    private String name, className, raceName;
    private int level;

    private int[] atk, def, abilities;
    private Power[] powers;

    public Player(
            String name, String className, String raceName,
            int level, int[] atk, int[] def, int[] abilities,
            Power[] powers
    ){
        this.name = name;
        this.className = className;
        setAtk(atk);
        setState();

        this.raceName = raceName;
        this.level = level;
        this.def = def;
        this.abilities = abilities;
        this.powers = powers;
    }


    public int getMaxCurativeEfforts() {return maxCurativeEfforts;}
    public void setMaxCurativeEfforts(int maxCurativeEfforts) {this.maxCurativeEfforts = maxCurativeEfforts;}

    public int getCurativeEfforts() {return curativeEfforts;}
    public void setCurativeEffort(int curativeEfforts) {this.curativeEfforts = curativeEfforts;}

    public int getLevel() {return level;}
    public void setLevel(int level) {this.level = level;}

    public int getMaxPg() {return maxPg;}
    public void setMaxPg(int maxPg) {this.maxPg = maxPg;}

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

                } else {
                    pg += maxPg / 4;
                }
            }
        } else {
            if (pg < 0) {

            } else {
                pg += recovered;
            }
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
    public void setName(String name) {this.name = name;}

    public String getClassName() {return className;}
    public void setClassName(String className) {
        this.className = className;
        if(atk!=null) setClass();
    }

    public String getRaceName() {return raceName;}
    public void setRaceName(String raceName) {this.raceName = raceName;}

    public void rest(boolean length) {
        if(length) {
            curativeEfforts = maxCurativeEfforts;
            for(Power p : powers){
                if(p != null) p.recover();
            }
        }
    }

    public void setAtk(int[] atk) {this.atk = atk; if(className!=null) setClass();}

    public int getFue() {return atk[FUE];}
    public int getCon() {return atk[CON];}
    public int getSab() {return atk[SAB];}
    public int getCar() {return atk[CAR];}
    public int getDes() {return atk[DES];}
    public int getInt() {return atk[INT];}

    public void setClass() {
        if(className.equals(classStrings[1])){
            //Ardiente
        } else if (className.equals(classStrings[2])) {
            //Brujo
            //TODO: Kauron
            pg = maxPg = 12 + atk[CON];
            curativeEfforts = maxCurativeEfforts = 6 + Player.getModifier(atk[CON]);
            return; //TODO: temporal
        } else if (className.equals(classStrings[3])) {
            //Buscador
        } else if (className.equals(classStrings[4])) {
            //Clérigo
            //TODO: Gárafran
        } else if (className.equals(classStrings[5])) {
            //Explorador
            //TODO: Aria Saferi
        } else if (className.equals(classStrings[6])) {
            //Guerrero
        } else if (className.equals(classStrings[7])) {
            //Mago
        } else if (className.equals(classStrings[8])) {
            //Mente de Batalla
        } else if (className.equals(classStrings[9])) {
            //Monje
        } else if (className.equals(classStrings[10])) {
            //Paladín
            //TODO: Ceaelynna
        } else if (className.equals(classStrings[11])) {
            //Pícaro
        } else if (className.equals(classStrings[12])) {
            //Psiónico
        } else if (className.equals(classStrings[13])) {
            //Sacerdote rúnico
        } else {
            //Señor de la Guerra
            //TODO: Mushu
        }
        pg = maxPg = 15;
        curativeEfforts = maxCurativeEfforts = 15;
    }

    public static int getModifier(int i) {
        return i / 2 - 5;
    }

    public int getTotalModifier(int i) {
        return getModifier(i) + level / 2;
    }

    public static int getLevel (int px) {
        return 0; //TODO: substitute level by px and autoconvert
    }
}