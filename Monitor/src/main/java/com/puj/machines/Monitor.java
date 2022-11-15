package com.puj.machines;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.zeromq.SocketType;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintWriter;

public class Monitor {
    private static final int PUERTO = 1100;
    public static final String SEPARATOR = ";";
    public static final String QUOTE = "\"";
    private static byte[]    WORKER_READY = { '\001' };
    public static ArrayList<Producto> listaProductos = new ArrayList<>();

    public static void main( String[] args ) throws Exception
    {

        Remote remote = UnicastRemoteObject.exportObject(new Interfaz() {
            /*
             * Sobrescribir opcionalmente los metodos que escribimos en la interfaz
             */
            @Override
            public String consultarRMI() throws RemoteException {            
                return consultar();
            };
            @Override
            public String adquirirRMI(Integer id, Integer cantidad) throws RemoteException {
                // Escribir archivo
                return adquirir(id, cantidad);
            };
        }, 0);

        Registry registry = LocateRegistry.createRegistry(PUERTO);
        leerRegistros();
        System.out.println("Servidor escuchando en el puerto " + String.valueOf(PUERTO));
        registry.bind("Tienda Virtual", remote);



        //  Prepare our context and sockets
        String answer="";
        try (ZContext context = new ZContext()) {
            
            Socket worker = context.createSocket(SocketType.REQ);
            
            ZHelper.setId(worker); //  Set a printable identity

            worker.connect("tcp://localhost:5558");

            ZFrame frame = new ZFrame(WORKER_READY);
                frame.send(worker, 0);
            //  Tell backend we're ready for work

            while (true) {
                ZMsg msg = ZMsg.recvMsg(worker);
                if (msg == null)
                        break;

                //  Get request, send reply
                //String contentType = inMsg.pop().toString();

                String request = msg.getLast().toString();
                //String request="consultar";
                System.out.println("Worker: " + request);
                String [] fields = request.split(SEPARATOR);
                try{
                    if(request.equals("consultar")){
                        answer=consultar();
                        System.out.println("Listado en cola...");
                    }else if(fields[0].equals("adquirir")){
                        System.out.println(fields[0]);
                        answer=adquirir( Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
                    }
                }catch(Exception e){}
                
                msg.getLast().reset(answer);
                msg.send(worker); 
                System.out.println("Respuesta Enviada.");
            }
        }
    }

    private static String adquirir(Integer id, Integer cantidad) {
        // Escribir archivo
        leerRegistros();
        //System.out.println(id);
        //imprimir();
        int resta=0;
        for(int i=0; i<listaProductos.size();i++){
            //System.out.println( listaProductos.get(i).ID );
            if(listaProductos.get(i).ID.equals(id)){
                resta = listaProductos.get(i).cantidad - cantidad;
                if(resta>=0){
                    listaProductos.get(i).cantidad=resta;
                    System.out.println("Actualizando...");
                    try{
                        actualizarRegistro();
                    }catch(Exception e){};
                    return "Adquisición exitosa!\n";
                }else{
                    return "\nCantidad ->Producto: "+listaProductos.get(i).nombre+" No disponible."
                    +"\n Disponibles: "+listaProductos.get(i).cantidad
                    +"\n Solicitados: "+cantidad+"\n";
                }
            }
        }
        return "Producto No Registrado.\n";
    }

    private static String consultar() {
        System.out.println("Enviando Listado...");
        return leerRegistros();
    }

    public static void actualizarRegistro(){
        try{
            PrintWriter out = new PrintWriter("./producto.csv");
            out.print("ID;NOMBRE;CANTIDAD");
            for(int i=0; i<listaProductos.size();i++){
                out.print("\n"+listaProductos.get(i).ID+";"+listaProductos.get(i).nombre+";"+listaProductos.get(i).cantidad);
            }
            out.close();
        }catch(Exception e){};
    }

    public static void imprimir(){
        System.out.print("ID;NOMBRE;CANTIDAD");
        for(int i=0; i<listaProductos.size();i++){
            System.out.print("\n"+listaProductos.get(i).ID+";"+listaProductos.get(i).nombre+";"+listaProductos.get(i).cantidad);
        }
    }

    private static String leerRegistros(){
        //Leer archivo
        String listado="";
        listaProductos.clear();
        BufferedReader br = null;
         try {
 
             br =new BufferedReader(new FileReader("./producto.csv"));
             String line = br.readLine();//HEADER
             line = br.readLine();
             while (null!=line) {
                String [] fields = line.split(SEPARATOR);
                Producto nuevo= new Producto(Integer.parseInt(fields[0]), fields[1], Integer.parseInt(fields[2]));
                //System.out.println(nuevo.ID+" "+nuevo.nombre+" "+nuevo.cantidad);
                //System.out.println(Arrays.toString(fields));
                listado += Arrays.toString(fields);
                listado +="\n";
                listaProductos.add(nuevo);
                line = br.readLine();
             }
             if (br!=null){
                 br.close();
             }
          }catch (Exception e){
            System.out.println(e);
          }
          System.out.println("Listado Listo...");
          return listado;
    }

        
}