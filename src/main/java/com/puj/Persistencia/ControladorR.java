package com.puj.Persistencia;

import com.puj.Entity.Usuario;

public class ControladorR {
    InterfazBD interfazBD = new Conexion();

    public ControladorR(){
        interfazBD.connectionDatabase();
    }

    public void registrarUsario(Usuario user){
        interfazBD.registrarUsario(user);
    }

    public boolean verificarUsuario(Usuario user){
        return interfazBD.validarUsuario(user);
    }
}
