/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.checkers.players.TactiCheckers;

/**
 *Classe per crear parelles de valors per Mapes de HASH
 * @author mariona i adri
 * @param <K> Clau (key) - Clau d'un valor en el mapa
 * @param <V> Valor (value) - Valor associat a una (key) en el mapa  
 */
public class Pair<K, V> {
    private K key;
    private V value;

    /**
     * Constructora per inicialitzar una Parella (Pair) amb una clau i un valor en específic
     * @param key La Clau per la Parella 
     * @param value El valor associat a la clau per la Parella
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     *Retorna la Clau d'aquesta Parella 
     * @return La Clau de la Parella
     */
    public K getKey() { return key; }
    
    /**
     *Retorna el Valor d'aquesta Parella
     * @return El Valor associat a la Clau de la Parella
     */
    public V getValue() { return value; }
}
