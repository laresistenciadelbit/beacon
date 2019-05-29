package com.example.android.lighthouse;

public class stan {
    public String nombre,contenido;
    public int imagen;
    public float x,y;
    public int beacon_ref;     //toma un beacon como referencia, a su posición se sumará el x e y de este marcador

    stan(String n, int i, String c, float coord_x, float coord_y)
    {
        nombre=n;
        imagen=i;
        contenido=c;
        x=coord_x;
        y=coord_y;
        beacon_ref=0;
    }

    stan(String n, int i, String c, float coord_x, float coord_y, int br)
    {
        nombre=n;
        imagen=i;
        contenido=c;
        x=coord_x;
        y=coord_y;
        beacon_ref=br;
    }
/*
    stan()
    {
        nombre="";
        imagen=0;
        contenido="";
        x=0;
        y=0;
    }

    public void inicializa(String n, int i, String c, float coord_x, float coord_y)
    {
        nombre=n;
        imagen=i;
        contenido=c;
        x=coord_x;
        y=coord_y;
    }
*/
}
