package com.puj.Persistencia;

import com.puj.Entity.Usuario;

public interface InterfazBD {
    
    public void connectionDatabase();
    public void registrarUsario(Usuario user);
    public boolean validarUsuario(Usuario usuario);
}
