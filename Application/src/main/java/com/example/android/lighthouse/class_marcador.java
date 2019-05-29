package com.example.android.lighthouse;

public class class_marcador {

    private boolean activo;
    private String nombre;
    private float x,y;
    int beacon_ref;     //toma un beacon como referencia, a su posición se sumará el x e y de este marcador

    class_marcador()
    {
        x=0;
        y=0;
        beacon_ref=1;
        activo=false;
    }
    public void set(String n, float mx, float my, int br)
    {
        nombre=n;
        x=mx;
        y=my;
        beacon_ref=br;
        activo=true;
    }

    public void desactivar(){activo=false;}

    public float get_x(){return x;}
    public float get_y(){return y;}
    public String get_nombre(){return nombre;}
    public boolean activado(){return activo;}
    public int get_beacon_ref(){return beacon_ref;}

}
