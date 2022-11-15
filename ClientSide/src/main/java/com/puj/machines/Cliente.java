package com.puj.machines;

import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZMQ.Socket;

import com.puj.Entity.Usuario;
import com.puj.Persistencia.ControladorR;

import org.zeromq.ZContext;

public class Cliente 
{
    static ControladorR controladorR = new ControladorR();
    public static void main( String[] args ) throws Exception
    {        
        System.out.println("=============TIENDA VIRTUAL===========");
        Scanner scanner = new Scanner(System.in);      
        Boolean ingreso=false;
        String usuario, contraseña;
        Usuario user= new Usuario();
        do{
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar Sesion");
            System.out.print("Opcion: ");
            String opcion = scanner.nextLine();
            if(opcion.equals("1")){
                System.out.print("Ingrese su nombre: ");
                usuario = scanner.nextLine();
                System.out.print("Ingrese contraseña: ");
                contraseña = scanner.nextLine();
                contraseña = controladorR.hash(contraseña);
                user = new Usuario(usuario,contraseña);
                controladorR.registrarUsario(user);
                iniciarSistema();
                ingreso=true;
            }else if(opcion.equals("2")){
                System.out.print("Ingrese su nombre: ");
                usuario = scanner.nextLine();
                System.out.print("Ingrese contraseña: ");
                contraseña = scanner.nextLine();
                contraseña = controladorR.hash(contraseña);
                user = new Usuario(usuario,contraseña);
                if(controladorR.verificarUsuario(user)){
                    System.out.println("Ingreso correcto");
                    iniciarSistema();
                    ingreso=true;
                }
            }else{
                System.out.println("Elega una opcion valida");
            }
            
        }while(!ingreso);
    }

    private static void iniciarSistema()throws Exception{
        try (ZContext context = new ZContext()) {
            Socket client = context.createSocket(SocketType.REQ);
            ZHelper.setId(client); //  Set a printable identity

            client.connect("tcp://25.6.84.72:5557");

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
                        long inicio =System.currentTimeMillis(); 
                            client.send("consultar");
                            System.out.println("Esperando respuesta ....");
                            reply = client.recvStr();
                        long fin = System.currentTimeMillis();
                        long total=fin-inicio;
                        System.out.println("Tiempo de respuesta: "+total); 
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

