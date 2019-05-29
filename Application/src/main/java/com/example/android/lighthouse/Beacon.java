package com.example.android.lighthouse;

public class Beacon {

    public final boolean debug=true;

    int map_x,map_y;
    float beacon_position[][];    //int beacon_position[][]={ {x,y},{x2,y2},{x3,y3},{x4,y4} };  //posiciones en el mapa de los Beacon, mas adelante se obtendrá de una base de datos (mysql?) junto con las beacon address
    public final String valid_address[] = {"A0:E9:DB:96:87:46","00:11:67:8C:14:E6","5A:5A:5A:A6:75:28","00:00:00:00:00:01"}; //la posicion de los beacons en este vector indica que numero en el layout tiene ese beacon (a0-e9 es el beacon1(altavoz), 00:11 es el beacon2(MiniBtUsb),5a:5a es el beacon3 (MiniBt con bateria), ...)
    final int calibrated_db=-66;    //señal calibrada a 1metro del beacon (solo usada en bluetooth no LE, en le usa la variable TX recibida del propio beacon)

    final int triang_beacons=4; //maximo numero de Beacon que usaremos para triangular (tamaño de array de Beacon encontrados)
    //public String beaconArr[]; //[*]nos lo ahorramos comparando con el id en valid_address[] en vez de almacenar su valor
    public float distArr[];//traduciremos signal to distance
    public int signalArr[];//array debug para mostrarnos las señales en dB.
    public int layout_id[];    //la id equivale al numero correspondiente de posición en valid_address[],que también equivale a la posición de beacon_position[][]
    int ttlArr[];
    public int beacons_saved=0;
    float dot_x, dot_y;
    float pixeles_en_1m;   //para obtener la relación entre metros y pixeles

    Beacon(/*int map_width, int map_height, int beacon_pos[][]*/)   //los argumentos los metemos después en la ventana del mapa así podemos usar la clase mientras para obtener información de los beacons (anuncios) sin posicionarlos en el mapa
    {
        //beaconArr=new String[triang_beacons];
        distArr=new float[triang_beacons];
        if(debug)signalArr=new int[triang_beacons];
        layout_id=new int[triang_beacons];
        ttlArr=new int[triang_beacons];
        for(int i=0;i<triang_beacons;i++) ttlArr[i]=0;

        dot_x=0;
        dot_y=0;
        map_x=0;
        map_y=0;
        /*beacon_position = new int[2][];
        for (int i=0; i<beacon_pos[i].length; i++)
            beacon_position[i] = beacon_pos[i].clone();*/
    }

    void putArgsBeacon(int map_width, int map_height, float beacon_pos[][], float metros_entre_beacon1_beacon2)   //inicializa variables a los argumentos correspondientes
    {
        map_x=map_width;
        map_y=map_height;

        beacon_position = new float[beacon_pos.length][];
        for (int i=0; i<beacon_pos.length; i++)
        {
            beacon_position[i] = new float[2];            //asignamos espacio
            //beacon_position[i][0] = beacon_pos[i][0]; //lo rellenamos con el argumento (pos x)
            //beacon_position[i][1] = beacon_pos[i][1]; //lo rellenamos con el argumento (pos y)
        }
        beacon_position=beacon_pos.clone();

        pixeles_en_1m = (float)Math.sqrt( Math.pow(beacon_position[1][0]-beacon_position[0][0], 2) + Math.pow(beacon_position[1][1]-beacon_position[0][1],2) )   /   metros_entre_beacon1_beacon2; //calculamos el equivalente a un metro en pixeles (módulo de la distancia entre beacon1 y beacon2 / distancia entre beacon1 y beacon2 en metros reales
        //pixeles_en_1m /= 3; //lo dividimos entre 3 D= D= D= D=
    }

    public void putBeacon(String found_address,int signal)
    {
        boolean found_valid_addr=false;
        boolean new_beacon=true;
        int lower_signal_id=0;

        for(int i=0; i<valid_address.length && !found_valid_addr; i++)
        {
            if (valid_address[i].equals(found_address))     //verifica que esté entre las direcciones de Beacon nuestras
            {
                found_valid_addr=true;//para no recorrer mas el bucle exterior

                for (int j = 0; j < beacons_saved /*&& new_beacon*/; j++) //hemos quitado && new_beacon que permitía salir antes del bucle porque sino no almacenamos la señal más baja del array.
                {
                    if(j==0)                    //durante el bucle vamos cogiendo la id de la señal mas baja, para, en caso de encontrar una mas alta, introducirla en el array sustituyéndola. (siempre que el array esté completo)
                        lower_signal_id=0;
                    else
                        if(distArr[j]<distArr[lower_signal_id])
                            lower_signal_id=j;

                    if (layout_id[j]==(i))//si estaba ya en el array de beacons actualiza su señal y disminuye su ttl
                    {
                        distArr[j] = dBtoM_other(signal);
                        if(debug)signalArr[j] = signal;
                        ttlArr[j]--;

                        new_beacon = false;
                    }
                }

                if(new_beacon)    //tras ver que no estaba en el array, ahora se añade al array
                {
                    if(beacons_saved >= triang_beacons)   //si hay mas de 4 Beacon almacenados (numero límite) entonces sustituimos el que menos señal tenga por el nuevo encontrado
                    {
                        layout_id[lower_signal_id] = i;
                        distArr[lower_signal_id] = dBtoM_other(signal);
                        if(debug)signalArr[lower_signal_id] = signal;   //si esta en modo debug guardamos la señal a parte de la distancia
                    }
                    else
                    {
                        layout_id[beacons_saved] = i;
                        distArr[beacons_saved] = dBtoM_other(signal);
                        if(debug)signalArr[beacons_saved] = signal;       //si esta en modo debug guardamos la señal a parte de la distancia
                        beacons_saved++;
                    }
                }
            }
        }
    }

    void raise_ttl() {for(int i=0;i<beacons_saved;i++) ttlArr[i]++;}

    void orderBySignal() //no sería necesario ordenarlo, pero por precisión, lo hacemos //ordena los arrays distArr[],beaconArr[],layout_id[] en base a distArr (de menos a mas distancia)
    {
        int pos_menor;
        float aux;
        //String aux2;//auxiliar del beaconArr[]
        int aux3;   //auxiliar del layout_id[]
        int aux4=0;//auxiliar de la señal (para debug)

        for(int i=0; i<beacons_saved-1; i++)
        {
            pos_menor=i;
            aux=distArr[i];
                //aux2=beaconArr[i];            //ya no usamos un array de direcciones mac
                aux3=layout_id[i];
                if(debug)aux4=signalArr[i];
            for(int j=i+1; j<beacons_saved; j++)
                if (distArr[j]<distArr[pos_menor])
                    pos_menor=j;

            distArr[i]=distArr[pos_menor];
                //beaconArr[i]=beaconArr[pos_menor];
                layout_id[i]=layout_id[pos_menor];
                if(debug)signalArr[i]=signalArr[pos_menor];
            distArr[pos_menor]=aux;
                //beaconArr[pos_menor]=aux2;
                layout_id[pos_menor]=aux3;
                if(debug)signalArr[pos_menor]=aux4;
        }

    }

    void remove_dissapeared()
    {
        int beacons_dissapeared=0;
        for(int i=0; i<beacons_saved; i++)
        {
            if (ttlArr[i] > 1)
            {
                distArr[i] = 33333; //ponemos una distancia alta para ordenarlo y despues quitar el último elemento
                beacons_dissapeared++;
            }
        }

        orderBySignal();    //ordenamos (de este modo estos beacons que ya no detecta quedarán los últimos)

        beacons_saved -= beacons_dissapeared;    //los quitamos
    }

    void calculeDot()          //usamos el vector de layout_id que está ordenado para ubicar los beacons en el mapa            //////////////////////////////***//**/*/
    {
        //PARA EL ALGORITMO: si el ttl es mayor que 0 sumarle distancia a ese beacon ya que no sabemos si no lo ha detectado por error o si realmente ya no está en el campo de visión

        switch(beacons_saved)   //hacemos un algoritmo distinto según los beacons que hayan sido encontrados
        {
            case 1: //asignamos su posición ya que es la del unico beacon encontrado
                dot_x = beacon_position[layout_id[0]][0];
                dot_y = beacon_position[layout_id[0]][1];
            break;

            case 2: //calcula distancia entre 2 beacons teniendo en cuenta su señal
                calculateTwoCircleIntersection(beacon_position[layout_id[0]][0], beacon_position[layout_id[0]][1], distArr[0]*pixeles_en_1m, beacon_position[layout_id[1]][0], beacon_position[layout_id[1]][1], distArr[1]*pixeles_en_1m);
            break;

            case 3: //calcula la intersección entre 3 puntos, si no lo consigue calcula la distancia entre 2 beacons teniendo en cuenta su señal
                if(!calculateThreeCircleIntersection(beacon_position[layout_id[0]][0],beacon_position[layout_id[0]][1],distArr[0]*pixeles_en_1m,   beacon_position[layout_id[1]][0],beacon_position[layout_id[1]][1],distArr[1]*pixeles_en_1m,   beacon_position[layout_id[2]][0],beacon_position[layout_id[2]][1],distArr[2]*pixeles_en_1m))
                    calculateTwoCircleIntersection(beacon_position[layout_id[0]][0],beacon_position[layout_id[0]][1],distArr[0]*pixeles_en_1m,   beacon_position[layout_id[1]][0],beacon_position[layout_id[1]][1],distArr[1]*pixeles_en_1m);

            break;
            case 4:
                int v[]={0,1,2};//vector auxiliar con los beacons que va a usar para el cálculo de la divergencia de los círculos   //(va a ser reescrito en el bucle j)
                boolean converge=false;

                for(int i=3; i<0 && !converge; i--)    //por cada beacon, descartando i (el último en la primera iteración) calcula la intersección (al estar ordenados de más a menos señal descarta del de menos señal al de más hasta que consiga converger)
                {
                    for(int j=0;j<3;j++)    //rellena un vector con índices 0,1,2,3 excepto i  ( primera iteración v={0,1,2} )
                    {
                        if(i!=j)
                            v[j]=j;
                    }
                    if(calculateThreeCircleIntersection( beacon_position[layout_id[v[0]]][0], beacon_position[layout_id[v[0]]][1], distArr[v[0]]*pixeles_en_1m,    beacon_position[layout_id[v[1]]][0], beacon_position[layout_id[v[1]]][1], distArr[v[1]]*pixeles_en_1m,    beacon_position[layout_id[v[2]]][0], beacon_position[layout_id[v[2]]][1], distArr[v[2]]*pixeles_en_1m) )
                        converge=true;
                }
            break;
        }
    }

    private boolean calculateThreeCircleIntersection(float x0, float y0, float r0,	        float x1, float y1, float r1,	        float x2, float y2, float r2)
    {
        float a, dx, dy, d, h, rx, ry;
        float point2_x, point2_y;
        //double EPSILON=0.1;		//double EPSILON=0.00001;

		/* dx and dy are the vertical and horizontal distances between the circle centers.*/
        dx = x1 - x0;
        dy = y1 - y0;

		/* Determine the straight-line distance between the centers. */
        d = (float)Math.sqrt((dy*dy) + (dx*dx));

		/* Check for solvability. */
        if ( d > (r0 + r1) || d < Math.abs(r0 - r1) )
        {
            return false;	/* no solution. circles do not intersect. */
        }

        // 'point 2' is the point where the line through the circle intersection points crosses the line between the circle centers.
		/* Determine the distance from point 0 to point 2. */
        a = ((r0*r0) - (r1*r1) + (d*d)) / (2.0f * d) ;

		/* Determine the coordinates of point 2. */
        point2_x = x0 + (dx * a/d);
        point2_y = y0 + (dy * a/d);

		/* Determine the distance from point 2 to either of the intersection points*/
        h = (float)Math.sqrt((r0*r0) - (a*a));

		/* Now determine the offsets of the intersection points from point 2.*/
        rx = -dy * (h/d);
        ry = dx * (h/d);

		/* Determine the absolute intersection points. */
        float intersectionPoint1_x = point2_x + rx;
        float intersectionPoint2_x = point2_x - rx;
        float intersectionPoint1_y = point2_y + ry;
        float intersectionPoint2_y = point2_y - ry;
        //System.out.println("INTERSECTION Circle1 AND Circle2:"+"(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")" + " AND (" + intersectionPoint2_x + "," + intersectionPoint2_y + ")");

        /* Lets determine if circle 3 intersects at either of the above intersection points. */
        // (x - center_x)^2 + (y - center_y)^2 < radius^2	  //ó:  	(x - x0) ^ 2 + (y - y0) ^ 2 <= R ^ 2
        if(		Math.pow( (intersectionPoint1_x - x2) , 2 ) +  Math.pow(  (intersectionPoint1_y - y2) , 2 )  <= Math.pow(r2 , 2)	)			//si el punto 1 está dentro converge en éste
        {//System.out.println("INTERSECTION Circle1 AND Circle2 AND Circle3:"+"(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")");
            dot_x = intersectionPoint1_x;
            dot_y = intersectionPoint1_y;
        }
        else
        {//si no intersecciona con un punto, obligatoriamente intersecciona con el otro (no tenemos mas posibilidades)
            //if(		Math.pow( (intersectionPoint2_x - x2) , 2 ) +  Math.pow(  (intersectionPoint2_y - y2) , 2 )  <= Math.pow(r2 , 2)	)		//si es el punto 2 el que está dentro converge en él
            //{//System.out.println("INTERSECTION Circle1 AND Circle2 AND Circle3:"+"(" + intersectionPoint2_x + "," + intersectionPoint2_y + ")");
                dot_x = intersectionPoint2_x;
                dot_y = intersectionPoint2_y;
            //}
            //else
            //    return false;	//sino, no converge en ninguno de los 2
        }

        return true;
    }

    private void calculateTwoCircleIntersection(float x0, float y0, float r0,	        float x1, float y1, float r1)       //no usamos la distancia hasta el beacon2
    {
        //((A-B)/2)+d(A)-d(B) <-- sumamos la distancia del punto medio a la distancia hasta A - la distancia hasta B

        //algoritmo que calcula los puntos solución a partir de los radios de distancia del beacon; ec. de circunf:
        // (x-a)^2+(y-b)^2=r^2      // ó:  x^2 + y^2 - 2ax - 2by + a^2 + b^2 - r^2  = 0
        // x,y <- incógnitas (punto a descubrir),  r <- distancia de la señal,  a,b <- coordenadas del beacon con más señal
        //http://mathworld.wolfram.com/Circle-CircleIntersection.html
    //NUEVO MÉTODO:
        // point A coordinates
            //double a = 20.0;//double b = 10.0;
        // point B coordinates
            //double c = 50.0;//double d = 40.0;

        // calculate distance between the two points
        float DT = (float)Math.sqrt(Math.pow((x1 - x0), 2) + Math.pow((y1 - y0), 2)); //distancia entre beacon1 y beacon2
        float T = r0 / DT;     //proporción entre distancia entre los beacons y el punto a hallar (sabemos su distancia(radio r0))

        // finding point C coordinate
        dot_x = (1 - T) * x0 + T * x1;
        dot_y = (1 - T) * y0 + T * y1;
    }

    float getDot_x() {return dot_x;}
    float getDot_y() {return dot_y;}

    float dBtoM(int rssi) {//pasa de dB a metros -> http://stackoverflow.com/a/21617601
        return (float)  Math.floor(                Math.sqrt((Math.pow(10, (calibrated_db - rssi) / 10)))                        * 100) / 100;      //le dejamos solo 2 decimales con math.floor y *100/100
    }
    int dBtoCM(int rssi) {//pasa de dB a metros -> http://stackoverflow.com/a/21617601
        return (int) Math.sqrt( (( Math.pow(10, (calibrated_db - rssi) / 10) ) * 100) );    //multiplicamos por 100 para pasar a cm.
    }

    float dBtoM_other(int rssi)
    {
        if (rssi == 0)
            return -1.0f; // if we cannot determine accuracy, return -1.

        double ratio = rssi*1.0/calibrated_db;

        if (ratio < 1.0)
            return (float)  Math.floor(           Math.pow(ratio, 10)          *100)/100;
        else
            return (float)  Math.floor(        ( (0.89976)*Math.pow(ratio,7.7095) + 0.111 )       * 100) / 100;
    }

    float get_px_in_m() {return pixeles_en_1m;}

}

/*pasa de db a metros (txCalibratedPower es la medida calibrada en dbs del beacon a 1 metro)
function dBtoM(txCalibratedPower, rssi) {
    var ratio_db = txCalibratedPower - rssi;
    var ratio_linear = Math.pow(10, ratio_db / 10);
    var r = Math.sqrt(ratio_linear);
    return r;
} */