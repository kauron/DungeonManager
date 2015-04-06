package com.kauron.dungeonmanager;

class Power {
    public static final int MELEE = 1, AREA = 2, RANGED = 3;
    public static final int DIARIO = 4, A_VOLUNTAD = 2, ENCUENTRO = 3, OPORTUNIDAD = 1;

    private boolean used;
    private int frequency, range, distance;
    private String name, keywords;
    private int atk, def;
    /** An array filled with the maximum damage of each die.
     *  The position 0 is the damage that doesn't depend on dies.
     *      Example: 1d6 + 1d4 + 4 is stored as {4, 4, 6}*/
     private int[] damage;
    //TODO: modify this so that it takes an array of the same size always, each with each kind of damage


    Power(String name, int frequency, int range, int distance, String keywords, int atk, int def, int[] damage){
        this.name = name;
        this.keywords = keywords;
        this.frequency = frequency;
        this.range = range;
        this.distance = distance;
        this.atk = atk;
        this.def = def;
        this.damage = damage;
        used = false;
    }

    String getName(){return name;}
    int getFrequency() {return frequency;}
    String getFrequencyString(){
        //TODO: change lists to arrays in resources
        switch(frequency) {
            case 1: return "Oportunidad";
            case 2: return "A voluntad";
            case 3: return "Encuentro";
            case 4: return "Diario";
            default: return null;
        }
    }
    int getRange() {return range;}
    String getRangeString() {
        switch(range){
            case 1: return "Cuerpo a cuerpo";
            case 2: return "Área";
            case 3: return "A distancia";
            default: return null;
        }
    }
    int getDistance() {return distance;}
    String getKeywords() {return keywords;}
    int getAtk() {return atk;}
    int getDef() {return def;}
    int[] getDamage() {return damage;}

    boolean isUsed(){return used;}

    void use(){
        if(frequency >= ENCUENTRO && !used)
            used = true;
    }
    void recover(){used = false;}
}