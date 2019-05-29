package com.example.android.lighthouse;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;//bt
import android.bluetooth.BluetoothDevice;//bt
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.content.BroadcastReceiver;//bt
import android.content.Context;//bt
import android.content.Intent;//bt
import android.content.IntentFilter;//bt

import java.util.Random;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    Random rand = new Random();
    int aux;
    boolean using_ble=false;
    boolean has_ble=true;
    private static final int REQUEST_ENABLE_BT = 1;//bt

    BluetoothAdapter bluetoothAdapter;  //bt
    BluetoothManager bluetoothManager; //ble


    Beacon beacons;
    int scan_ttl=0;
    class_marcador marcador;
    stan eventos[];
    stan anuncios[];


    final float metros_entre_beacon1_y_beacon2=3.5f; //distancia entre beacon1 y beacon2 para calcular la relación en pixeles de 1metro,  más adelante obtener de la base de datos (la app gestor lo introduce al meter el 2ºbeacon) //f de float

//layout_main
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
//layout1
    ListView eventos_list;  //más adelante se obtendrán de la base de datos
    String[] arrayEventos_n = {"Actuación de LADYBABY","Recoge tu manga de regalo","Entrega de premios R.E.T.O."} ;
    String[] arrayEventos_c = {"Actuación de LADYBABY a las 17:30 en el escenario principal","Recoge tu manga de regalo en el stan de información","Entrega de premios R.E.T.O. en el stan BOSSY a las 18:30"} ;
    Integer[] imageId_e = {R.drawable.anuncio_ladybaby,R.drawable.anuncio_noodles,R.drawable.anuncio_japanweek2};
    Integer[] beacon_reference = {1,2,3} ;  //beacon de referencia en distancia
    Integer[] Eventos_metros_x = {3,4,5} ;  //metros desde beacon correspondiente en eje x
    Integer[] Eventos_metros_y = {5,2,8} ;  //metros desde beacon correspondiente en eje y
    class_listview eventos_adapter;
    //TextView listDevicesFound;
    //Button btnScanDevice;
    //TextView textView4;
//layout2
    ListView ofertas_list;  //más adelante se obtendrán de la base de datos
    //un array de ofertas y otro que se irá reyenando y vaciando según estés o no cerca del beacon
    //String[] arrayOfertasVisibles;// = {"20% descuento en noodles presentando este mensaje","Dos camisetas por 15€ de tus videojuegos favoritos en los puestos de Foreverdai","Maquillaje kawaii en la entrada norte del recinto por sólo 5€","Pásate ahora por nuestro stan y recibirás un regalo (hasta fin de existencias)"} ;
    String[] arrayAnuncios_n = {"Noodleshop","Foreverdai","Maquillaje kawaii","F&C comics"} ;
    String[] arrayAnuncios_c = {"20% descuento en noodles presentando este mensaje","Dos camisetas por 15€ de tus videojuegos favoritos en los puestos de Foreverdai","Maquillaje kawaii en la entrada norte del recinto por sólo 5€","Pásate ahora por nuestro stan y recibirás un regalo (hasta fin de existencias)"} ;
    Integer[] Anuncios_metros_x = {0,0,0,0} ;  //valores no reales, tienen que ser con respecto a algun beacon (reimplementado en el listener del layout)
    Integer[] Anuncios_metros_y = {0,0,0,0} ;
    Integer[] imageId_a = {R.drawable.anuncio_noodles,R.drawable.anuncio_foreverdai,R.drawable.anuncio_japanweek2,R.drawable.anuncio_japanweek};
    class_listview ofertas_adapter;
//layout3
    TextView beaconsfoundlist;
    TextView dot_position_box;
    //Button      mover;/////
    ImageView dot;
    ImageView marker_dot;
    ImageView beacon1;
    ImageView beacon2;
    ImageView beacon3;
    ImageView beacon4;
    ImageView map;

    ImageView leyenda_dot;
    TextView  texto_leyenda_dot;
    ImageView leyenda_destino;
    TextView  texto_leyenda_destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        beacons=new Beacon();
        marcador=new class_marcador();

        eventos=new stan[arrayEventos_n.length];
        for(int i=0;i<eventos.length;i++)
            eventos[i]=new stan(arrayEventos_n[i],imageId_e[i],arrayEventos_c[i],Eventos_metros_x[i],Eventos_metros_y[i]);

        anuncios=new stan[arrayAnuncios_n.length];
        for(int i=0;i<anuncios.length;i++)
            anuncios[i]=new stan(arrayAnuncios_n[i], imageId_a[i], arrayAnuncios_c[i], Anuncios_metros_x[i], Anuncios_metros_y[i]);



        // Load the UI from res/layout/activity_main.xml

        setContentView(R.layout.layout_main);
        final ActionBar actionBar = getActionBar();
        //actionBar.hide(); <-- para esconder la barra del menu
        actionBar.setTitle("");// <-- para esconder el título de la aplicación del menu
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            has_ble=false; //aunque esto lo forzaríamos según queramos que sea la app para ble o no, así dejamos que otros usuarios lo arranquen sin ble
//////////////////////////implementar has_ble <- meter en if(has_ble) todas las cosas que usen bluetooth

        if(using_ble)   //carga el adaptador
        {
            bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        else
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));//hook de escaneo
        registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));//hook de fin de escaneo

        //si estamos usando ble en la app y el movil tiene ble, pide que lo active, si no estamos usando ble pide que active el bt normal.
        if ( ( (using_ble && has_ble) || !using_ble ) && ( bluetoothAdapter == null || !bluetoothAdapter.isEnabled() ) ) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        scanBT();


        // When swiping between different sections, select the corresponding tab. We can also use
        // ActionBar.Tab#select() to do this if we have a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            //<START>para que muestre la primera pagina al cargar la app
            Boolean first = true;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (first && positionOffset == 0 && positionOffsetPixels == 0){
                    onPageSelected(0);
                    first = false;
                }
            }//<END>para que muestre la primera pagina al cargar la app


            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);

                switch(position+1)
                {
                    case 1: //layout1
                        draw_layout1();
                    break;

                    case 2: //layout2
                        draw_layout2();
                    break;

                    case 3: //layout3
                        beaconsfoundlist= (TextView)findViewById(R.id.beaconsfound);
                        dot_position_box= (TextView)findViewById(R.id.dot_position);
                        if(!beacons.debug) {
                            beaconsfoundlist.setVisibility(View.INVISIBLE);   //si no está en modo debug ocultamos la caja de texto
                            dot_position_box.setVisibility(View.INVISIBLE);
                        }
                        //mover = (Button) findViewById(R.id.mover);/////
                        dot = (ImageView) findViewById(R.id.dot);
                        marker_dot = (ImageView) findViewById(R.id.marker_dot);
                        beacon1=(ImageView) findViewById(R.id.beacon1);
                        beacon2=(ImageView) findViewById(R.id.beacon2);
                        beacon3=(ImageView) findViewById(R.id.beacon3);
                        beacon4=(ImageView) findViewById(R.id.beacon4);
                        map=(ImageView) findViewById(R.id.map);

                        leyenda_dot = (ImageView) findViewById(R.id.leyenda_dot);
                        texto_leyenda_dot = (TextView)findViewById(R.id.texto_leyenda_dot);
                        leyenda_destino = (ImageView) findViewById(R.id.leyenda_destino);
                        texto_leyenda_destino = (TextView)findViewById(R.id.texto_leyenda_destino);


                        final Animation animation = new AlphaAnimation(1, 0);   //animacion del punto parpadeando
                            animation.setDuration(1000);
                            animation.setInterpolator(new LinearInterpolator());
                            animation.setRepeatCount(Animation.INFINITE);
                            animation.setRepeatMode(Animation.REVERSE);
                            dot.startAnimation(animation);


                        ViewTreeObserver greenObserver = dot.getViewTreeObserver();//listener   //creamos un predrawListener para que pueda tomar los valores de las posiciones de los beacons del layout, sino aún no los ha dibujado y no saca su posición (se ejecuta una sola vez)
                        greenObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {//listener
                            @Override//listener
                            public boolean onPreDraw()//listener
                            {
                                dot.getViewTreeObserver().removeOnPreDrawListener(this);//listener

                                // de momento lo define y obtiene de la app, despues lo obtendrá directamente de la db
                                float beacon_pos[][] = {{(int) beacon1.getX(), (int) beacon1.getY()}, {(int) beacon2.getX(), (int) beacon2.getY()}, {(int) beacon3.getX(), (int) beacon3.getY()}, {(int) beacon4.getX(), (int) beacon4.getY()}};//+
                                beacons.putArgsBeacon(map.getWidth(), map.getHeight(), beacon_pos, metros_entre_beacon1_y_beacon2); //iniciamos argumentos de clase Beacon para el mapa //+

                                //arreglamos posición de eventos
                                for(int i=0;i<eventos.length;i++)   //aleatoria con respecto al beacon3 de manera relativa
                                {
                                    eventos[i].x=beacon3.getX() +  ( eventos[i].x * beacons.get_px_in_m() ) ;     //ARREGLAR PARA OBTENER DE LA DB como indico debajo\
                                    eventos[i].y=beacon3.getY() +  ( eventos[i].y * beacons.get_px_in_m() ) ;
                                    //eventos[i].x=eventos[i].beacon_ref.getX() +  ( eventos[i].x * beacons.get_px_in_m() ) ;   //tienes el beacon de referencia, cómo sacas su puto X e Y ????!?!?!? -> obtiene datos de array/db en metros desde 0,0 del mapa y los transforma a pixeles

                                }

                                //dibuja marcador (si está activado):
                                if(marcador.activado())
                                    marcador_pone_layout(animation); //dibuja o borra el marcador del layout
                                else
                                    marcador_borra_layout();


                                return false;//listener
                            }//listener
                        });//listener

                        marker_dot.setOnClickListener(new View.OnClickListener() {  //listener: al pulsar el marcador, lo desactiva
                            public void onClick(View v) {
                                marcador.desactivar();  //lo desactiva en el array
                                marcador_borra_layout(); //lo desactiva en el layout
                            }
                        });

                        task_paint_dot_layout3.post(runnableCode3);

                    break;
                }

            }
        });

        // BEGIN_INCLUDE (add_tabs) // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
        {// Create a tab with text corresponding to the page title defined by the adapter. Also specify this Activity object, which implements the TabListener interface, as the callback (listener) for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            //.setText(mSectionsPagerAdapter.getPageTitle(i)) //<-original
                            //.setIcon(R.mipmap.bar)    //<- 1 icono
                            .setIcon(mSectionsPagerAdapter.getIcon(i))
                            .setTabListener(this));
        }


    }


    void draw_layout1()     //draw layout_1:
    {
        eventos_list=(ListView)findViewById(R.id.eventos_LV);
        eventos_adapter= new class_listview(MainActivity.this, arrayEventos_c, imageId_e);  //adapter listview
        eventos_list.setAdapter(eventos_adapter);
        eventos_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, "Mensaje: " + arrayEventos_c[+position], Toast.LENGTH_SHORT).show();
                marcador.set(eventos[position].nombre, eventos[position].x, eventos[position].y, eventos[position].beacon_ref);
                mViewPager.setCurrentItem(2);
            }
        });
        task_write_anuncios_layout1.post(runnableCode1); //cada 2 segundos actualiza los anuncios según los beacons que tengamos alrededor
    }

    Handler task_write_anuncios_layout1 = new Handler();
    private Runnable runnableCode1 = new Runnable() {
        @Override
        public void run() {
            //beacons.orderBySignal();
            //listDevicesFound.setText("");   //limpiamos la caja de texto
            //for(int i=0;i<beacons.beacons_saved;i++)    //rellenamos el array de señales encontradas en el TextView
            //    listDevicesFound.append(beacons.valid_address[beacons.layout_id[i]]+" "+beacons.distArr[i]+" "+beacons.signalArr[i]+"\n");


            task_write_anuncios_layout1.postDelayed(runnableCode1, 3500);
        }
    };


    void draw_layout2()     //draw layout_2:
    {
        ofertas_list=(ListView)findViewById(R.id.ofertas_LV);
        ofertas_adapter = new class_listview(MainActivity.this, arrayAnuncios_c, imageId_a);  //adapter listview
        ofertas_list.setAdapter(ofertas_adapter);
        ofertas_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, "Mensaje: " + arrayAnuncios_c[+position], Toast.LENGTH_SHORT).show();
            }
        });
        task_write_beacon_list_layout2.post(runnableCode2);  //funcion que se ejecuta cada medio segundo y escribe el array en el layout2
    }

    Handler task_write_beacon_list_layout2 = new Handler();
    private Runnable runnableCode2 = new Runnable() {
        @Override
        public void run() {

            //ofertas_adapter.notifyDataSetChanged();            ofertas_list.invalidateViews();            ofertas_list.refreshDrawableState();//refresh layout with uptated listview
            //ofertas_adapter.add(arrayOfertas[0]);     //no implementado aún
            task_write_beacon_list_layout2.postDelayed(runnableCode2, 1500);
        }
    };


    Handler task_paint_dot_layout3 = new Handler();
    private Runnable runnableCode3 = new Runnable() {
        @Override
        public void run() {

            beacons.orderBySignal();    //primero ordenamos los beacons por señal

            if(beacons.debug) { //si esta en modo debug enseñamos los beacons encontrados con su señal y distancia

                beaconsfoundlist.setText("");   //limpiamos la caja de texto
                for (int i = 0; i < beacons.beacons_saved; i++)    //rellenamos el array de señales encontradas en el TextView
                    beaconsfoundlist.append("[]beacon"+beacons.layout_id[i] + "   " + beacons.distArr[i] + " m   " + beacons.signalArr[i] + "dB\n");

                dot_position_box.setText("    ( "+dot.getX()+" , "+dot.getY()+" )");//tambien enseñamos la posicion de la bola

            }
            if(beacons.beacons_saved>0) {   //solo calcula y mueve si ha encontrado beacons
                beacons.calculeDot();
                dot.setX((float)beacons.getDot_x());
                dot.setY((float)beacons.getDot_y());
            }

            task_paint_dot_layout3.postDelayed(runnableCode3, 500);
        }
    };


    void marcador_pone_layout(Animation animation)
    {
        leyenda_destino.setVisibility(View.VISIBLE);
        texto_leyenda_destino.setVisibility(View.VISIBLE);
        marker_dot.setVisibility(View.VISIBLE);

        texto_leyenda_destino.setText(marcador.get_nombre());
        marker_dot.setX(marcador.get_x());
        marker_dot.setY(marcador.get_y());
        marker_dot.startAnimation(animation);   //si esta activo aunque lo desactivemos puede que lo siga mostrando!!! <---- wtf
    }

    void marcador_borra_layout()
    {
        leyenda_destino.setVisibility(View.INVISIBLE);
        texto_leyenda_destino.setVisibility(View.INVISIBLE);
        marker_dot.setVisibility(View.INVISIBLE);
        marker_dot.clearAnimation();
    }



    private void scanBT(){

            if(scan_ttl>1)  //cuando ha hecho dos escaneos elimina en este tercero los beacons que ya no den señal
                    beacons.remove_dissapeared();   //elimina los beacons que tengan un numero > 2 en el array de tllArr;

            //btArrayAdapter.clear();
            bluetoothAdapter.startDiscovery();

            scan_ttl++;         //aumenta el ttl de escaneo
            beacons.raise_ttl();//aumenta el ttl de cada direccion almacenada
    }

    //scan de bluetooth continuo:
    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //String rssi = (intent.getExtras()).get(BluetoothDevice.EXTRA_RSSI).toString();
                int rssi = (int)intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);//dBs
                if(rssi > 0){   //arreglamos para dispositivos que tomen valores positivos en vez de negativos
                    rssi = rssi - 256;//rssi = rssi - 128;
                }
                //btArrayAdapter.add( device.getName() + "\n" + device.getAddress() + "\n" + rssi );
                //btArrayAdapter.notifyDataSetChanged();
                if( device.getAddress()!=null && rssi<0 )   //si no ha detectado una señal erronea (campo de address no vacío y señal menor que 0 (ya uqe la inicializamos a 0 y los valores válidos son negativos) )
                    beacons.putBeacon(device.getAddress(), rssi);//mete el dispositivo en el array de la clase Beacon (verifica si ya estaba, si estaba actualiza su señal) (cada scan esperamos 3 segundos y mostramos el array en un punto del mapa (para bt no le))

                ///////////////listDevicesFound.append("wtf");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                scanBT(); //volvemos a escanear continuamente
            }
        }
    };



    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, tell the ViewPager to switch to the corresponding page.
        mViewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

}

/*public void moverbolaroja(View v)
    {
        int x=map.getWidth();
        int y=map.getHeight();
        int start_x = (int) map.getX();
        int start_y = (int) map.getY();

        int rand1 = (int) (x * Math.random()) + 1;
        int rand2 = (int) (y * Math.random()) + 1;
        //dot.setX(start_x + x - dot.getWidth());dot.setY(start_y + y - dot.getHeight()); //<-pone la bola en la esquina de la imagen (max distancia)
        dot.setX(start_x + rand1 - dot.getWidth()  );
        dot.setY(start_y + rand2 - dot.getHeight() );
    }*/

/*sobre usar margin en vez de setx: http://stackoverflow.com/a/16782819
RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); //WRAP_CONTENT param can be FILL_PARENT
params.leftMargin = 206; //XCOORD
params.topMargin = 206; //YCOORD
childView.setLayoutParams(params);*/

//mas métodos para mover el punto o animarlo: http://stackoverflow.com/a/16556710


/*handler que se ejecuta cada 1segundo (meter en oncreate?)

Handler h = new Handler();
int delay = 1000; //milliseconds

h.postDelayed(new Runnable(){
    public void run(){
        //do something
        h.postDelayed(this, delay);
    }
}, delay);
*/
