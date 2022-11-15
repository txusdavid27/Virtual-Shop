package com.puj.machines;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Interfaz extends Remote{
    String consultarRMI() throws RemoteException;
    String adquirirRMI(Integer id, Integer cantidad) throws RemoteException;
}
