package com.puj.machines;

public class Producto {
    public Integer ID;
    public String nombre;
    public Integer cantidad;

    public Producto(Integer id, String name, Integer cant){
        this.ID=id;
        this.nombre=name;
        this.cantidad=cant;
    }
}