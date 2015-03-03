package com.kauron.dungeonmanager;

public class Power {
    public static final int DIARIO = 4, A_VOLUNTAD = 2, ENCUENTRO = 3, OPORTUNIDAD = 1;

    private boolean used;
    private int type;
    private String name;
    private int atk, def, damage;


    public Power(String name, int type){
        this.name = name;
        this.type = type;
        used = false;
    }

    public String getName(){return name;}
    public int getType(){return type;}

    public boolean isUsed(){return used;}

    public void use(){
        if(type >= ENCUENTRO && !used)
            used = true;
    }
    public void recover(){used = false;}
}