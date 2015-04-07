package com.kauron.dungeonmanager;

class Power {
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
     * any other is represented by d2, d4, d6, d8, d10, d12, d20, d100
     */
    public static final int[] DIE = {0, 2, 4, 6, 8, 10, 12, 20, 100};

    private boolean used;
    private int freq, action, distance, range, objectives;
    private String name, description;
    private String[] keywords; //fire, spell...
    private int atk, def; //constants from Player to denote atk and defense
    private int[] damage; //the max sizes of the different dies


    Power(String name, String desc, int freq, int action, int distance, String[] keywords,
                 int atk, int def, int[] damage){
        used = false;
        this.name = name; this.description = desc;
        this.freq = freq; this.action = action;
        this.distance = distance;
        this.keywords = keywords;

    }

    String getKeywords() {
        if ( keywords != null && keywords.length != 0 ) {
            String r = keywords[0];
            for (String k : keywords) r += ", " + k;
            return r;
        } else {
            return "";
        }
    }

    String getFrequencyString() {return FREQ[freq];}
    int getFreq() {return freq;}

    String getRangeString() {return DISTANCES[range];}
    int getRange() {return range;}

    int getDistance() {return distance;}

    String getName(){return name;}
    String getDescription() {return description;}

    boolean isUsed(){return used;}

    boolean use(){
        if (!used) {
            if (freq >= ENCUENTRO) used = true;
            return true;
        } else {return false;}
    }

    int rollAttack() {return atk + (int)(Math.random()*20) + 1;}
    int rollDamage() {
        int roll = 0;
        for(int i : damage) {
            roll += (int)(Math.random()*i + 1);
        }
        return roll;
    }

    void recover(int type){
        if(this.freq <= type) used = false;
    }
}