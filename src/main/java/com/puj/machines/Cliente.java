package com.puj.machines;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

public class Cliente 
{
    public static void main( String[] args ) throws Exception
    {        
            try (ZContext context = new ZContext()) {
                Socket client = context.createSocket(SocketType.REQ);
                ZHelper.setId(client); //  Set a printable identity
    
                client.connect("tcp://localhost:5557");
    
                //  Send request, get reply
                int opcion;
                Integer id, cantidad;
                Scanner sc = new Scanner(System.in);
                String reply;

                do {
                    System.out.println("----------Tienda Virtual----------");
                    System.out.println("Seleccione una opcion");
                    System.out.println("1. Listar Productos");
                    System.out.println("2. Comprar Producto");
                    System.out.println("3.Salir");
                    System.out.print("Opcion:");
                    opcion = sc.nextInt();
                    if(opcion!=3){
                        switch (opcion) {
                            case 1:
                                client.send("consultar");
                                System.out.println("Esperando respuesta ....");
                                reply = client.recvStr();
                                System.out.println("Client: " + reply);
                                break;
                            case 2:
                                System.out.println("Ingresa el id del producto: ");
                                id = sc.nextInt();
                                
    
                                System.out.println("Ingresa la cantidad: ");
                                cantidad = sc.nextInt();
                                
                                client.send("adquirir;"+id+";"+cantidad);
                                System.out.println("Esperando respuesta ....");
                                reply = client.recvStr();
                                System.out.println("Client: " + reply);
                                break;
                            default:
                                break;
                        }
                    }
                    //Thread.sleep(1000);
                } while (opcion != 3);
                System.out.println("Saliendo...");
                sc.close();
            }
    }
}

