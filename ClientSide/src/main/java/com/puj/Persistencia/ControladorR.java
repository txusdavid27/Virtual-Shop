package com.puj.Persistencia;

import com.puj.Entity.Usuario;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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

    private String hexa(byte[] hash){
        StringBuilder hBuilder = new StringBuilder(hash.length);
        for(int i=0;i<hash.length; i++){
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1){
                hBuilder.append(0);
            }
            hBuilder.append(hex);
        }
        return hBuilder.toString();
    }
    public String hash(String pass){
        String hashPass="";
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(pass.getBytes(StandardCharsets.UTF_8));
            hashPass = this.hexa(hash);
        }catch(Exception e){
            e.printStackTrace();
        }
        return hashPass;
    }
}
