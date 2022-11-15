package com.puj.Persistencia;

import java.sql.Statement;
import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.puj.Entity.Usuario;

public class Conexion implements InterfazBD{
    Connection c;

    public void Conexion(){

    }

    public void connectionDatabase(){
        String host="localhost";
        String user= "root";
        String password="12345";
        int port=3306;
        String database="proyecto";
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", host,port,database,user,password);
            this.c = DriverManager.getConnection(url);   
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void registrarUsario(Usuario user){
        try{
            String query = "insert into Usuario values(?,?)";
            PreparedStatement preparedStatement = c.prepareStatement(query);
            preparedStatement.setString(1, user.getNombre());
            preparedStatement.setString(2, user.getContraseña());
            preparedStatement.executeUpdate();
        }catch(SQLException e){
            e.getMessage();
            //e.printStackTrace();
        }
    }

    public boolean validarUsuario(Usuario usuario){
        try{
            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from usuario");
            while(resultSet.next()){
                if(usuario.getNombre().equals(resultSet.getString("name"))){
                    if(usuario.getContraseña().equals(resultSet.getString("password"))){
                        return true;
                    }else{
                        System.out.println("Contraseña incorrecta");
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}

