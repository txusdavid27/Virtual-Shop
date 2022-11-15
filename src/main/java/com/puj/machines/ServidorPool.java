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

public class ServidorPool {
    private static final int PUERTO = 1100;
    private static final String IP = "127.0.0.1";
    public static final String SEPARATOR = ";";
    public static final String QUOTE = "\"";
    private static byte[]    WORKER_READY = { '\001' };
    public static ArrayList<Producto> listaProductos = new ArrayList<>();

    public static void main( String[] args ) throws Exception
    {
        Registry registry = LocateRegistry.getRegistry(IP, PUERTO);
        Interfaz interfaz = (Interfaz) registry.lookup("Tienda Virtual");
        
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
                        answer=interfaz.consultarRMI();
                        System.out.println("Listado en cola...");
                    }else if(fields[0].equals("adquirir")){
                        System.out.println(fields[0]);
                        answer=interfaz.adquirirRMI( Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
                    }
                }catch(Exception e){}
                
                msg.getLast().reset(answer);
                msg.send(worker); 
                System.out.println("Respuesta Enviada.");
            }
        }
    }        
}