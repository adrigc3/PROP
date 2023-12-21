/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Ejercicio1;

/**
 *
 * @author VLLAN
 */


/*Información de los mensajes y sus significados:

Cod = 0 --> informacion de nuestra posicion
Cod = 1 --> informacion del enemigo + cercano a nuestra posicion
*/
public class Mensaje implements java.io.Serializable{
    
    private int codigo;
    
    //Posiciones de la comunicación
    private double x;
    private double y;
    
    //Definimos la variable que guarda el nombre del enemigo
    private String Name;
    
    public Mensaje(int cod, double X, double Y, String nombre){
        this.codigo = cod; //guardamos la acción a realizar
        this.x = X; //Guardamos las variables de la posición pasada por mensaje
        this.y = Y;
        this.Name = nombre;
    }
    
    public double getOX(){
        return x; //Devolvemos la posición X
    }
    
    public double getOY(){
        return y; //Devolvemos la posición Y
    }
    public int getCodi(){
        return codigo;
    }
    
    public String getEnemyName(){
        return Name;
    }
}
