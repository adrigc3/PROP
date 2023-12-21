/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Ejercicio2;

import Ejercicio1.*;

/**
 *
 * @author VLLAN
 */


/*Información de los mensajes y sus significados:

Cod = 0 --> informacion de nuestra posicion
Cod = 1 --> informacion del enemigo + cercano a nuestra posicion
*/
public class Mensaje implements java.io.Serializable{
    
    //Posiciones de la comunicación
    private double x;
    private double y;
    private double bearing;
    int codigo;
    int shouldNotFire;
    /*
    codigo = 0 --> pasamos las coordenadas y bearing del enemigo
    codigo = 1 --> Informamos de que algun enemigo ha disparado y hay que esquivar
    codigo = 2 --> Informamos de que el robot analizado tiene poca energia y le disparamos con todo
    */
    
    //Definimos la variable que guarda el nombre del enemigo
    
    public Mensaje(int Code, double X, double Y, double Bearing, int ShouldNotFire){
        this.x = X; //Guardamos las variables de la posición pasada por mensaje
        this.y = Y;
        this.bearing = Bearing;
        this.codigo = Code;
        this.shouldNotFire = ShouldNotFire;
    }
    
    public double getOX(){
        return x; //Devolvemos la posición X
    }
    
    public double getOY(){
        return y; //Devolvemos la posición Y
    }
    
    public double getBearing(){
        return bearing;
    }
    
    public int getCode(){
        return codigo;
    }
    public int getShouldNotFire() {
        return shouldNotFire; // Debe coincidir con el nombre del campo en la clase Mensaje
    }
}
