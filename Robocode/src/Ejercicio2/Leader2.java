/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Ejercicio2;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.HitRobotEvent;
import robocode.util.Utils;
/**
 *
 * @author VLLAN
 */
public class Leader2 extends TeamRobot{
    private Map<String, Point> enemiesPositions = new HashMap<String, Point>(); //Guardamos las posiciones de los enemigos
    private Map<String, Double> enemiesBearings = new HashMap<String, Double>(); //Guardamos los Bearings enemigos
    private Map<String, Double> enemiesEnergies = new HashMap<String, Double>(); //Guardamos la Energia de los enemigos
    
    private double leaderX;
    private double leaderY;
    
    int aliados = 4, enemigos;
    String enemigoTargeteado;
    int movimientoDireccion = 1;
    private String enemigoMasCercano;
    private double distanciaEnemigoMasCercano = Double.MAX_VALUE;
    private boolean liderEntreEnemigoDroide = false;
    
    private boolean isBetween(double value, double bound1, double bound2) {
        return (value >= Math.min(bound1, bound2)) && (value <= Math.max(bound1, bound2));
    }

        
    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    private String buscarEnemigoMasCercano() {
        enemigoMasCercano = null;
        double distanciaMasCercana = Double.MAX_VALUE;

        for (Map.Entry<String, Point> entry : enemiesPositions.entrySet()) {
            String nombreEnemigo = entry.getKey();
            Point posicionEnemigo = entry.getValue();

            // Calculamos la distancia entre el líder y el enemigo
            double distancia = calcularDistancia(leaderX, leaderY, posicionEnemigo.getX(), posicionEnemigo.getY());

            if (distancia < distanciaMasCercana) {
                distanciaMasCercana = distancia;
                enemigoMasCercano = nombreEnemigo;
            }
        }

        return enemigoMasCercano;
    }
    
    @Override
    public void run() {
        enemigos = getOthers() - aliados; //Obtenemos el número de robots enemigos que hay en el mapa
        System.out.println("Hay un total de = "+enemigos+" Robots enemigos");
        
	while (true) {
            setTurnRadarRight(10000);
            enemigoMasCercano = buscarEnemigoMasCercano();
            if (enemigoMasCercano != null) {
            // Calcular el ángulo hacia el enemigo más cercano
            double bearing = enemiesBearings.get(enemigoMasCercano);
            double absoluteBearing = getHeading() + bearing;

            // Apuntar hacia el enemigo más cercano
            setTurnGunRight(Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading()));

            // Disparar si el cañón está listo para disparar
            if (getGunHeat() == 0) {
                fire(2); // Ajusta la potencia del disparo según tus necesidades
            }
        }
             // Genera un número aleatorio para la dirección de movimiento (izquierda o derecha)
            int anguloGiro = (Math.random() <0.5) ? 1 : - 1;
            

            // Genera un número aleatorio para la distancia de movimiento
            int distanciaAleatoria = (int) (Math.random() * 200) + 100; // Valores aleatorios entre 100 y 300

            // Gira aleatoriamente
            turnRight(10 * anguloGiro);  // Puedes ajustar el ángulo de giro

            // Avanza una distancia aleatoria
            ahead(distanciaAleatoria);

            // Comprobar si el líder está entre el enemigo y el Droide
            liderEntreEnemigoDroide = isLeaderBetweenEnemyAndDroid();

            // Enviar un mensaje a los Droids para informarles si no deben disparar
            try {
                broadcastMessage(new Mensaje(3, 0, 0, liderEntreEnemigoDroide ? 1 : 0, 1));
            } catch (IOException ex) {
                Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
            }
        

            // Ejecuta las acciones de movimiento
            execute();
	}

        
    }
    
 
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e){
        // Calcular el bearing del enemigo
        double absoluteBearingRadians = getHeadingRadians() + e.getBearingRadians();
        System.out.println("detectado el robot "+e.getName()+" este tiene un isTeammate()= "+isTeammate(e.getName()));
        if(!isTeammate(e.getName())){
            // Calcular el bearing del enemigo
            absoluteBearingRadians = getHeadingRadians() + e.getBearingRadians();

            // Calcula la distancia al enemigo
            double distanciaEnemigo = e.getDistance();

            // Apunta al enemigo más cercano
            setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearingRadians - getGunHeadingRadians()));

            // Si el cañón está listo para disparar y el enemigo está a una distancia segura, dispara
            if (getGunHeat() == 0 && distanciaEnemigo < 400) {
                fire(3); // Ajusta la potencia del disparo según tus necesidades
            }
    
            //Calculamos la OX y OY del enemigo a partir de nuestra posción la distáncia a él
            //y el seno y coseno del Bearing
            double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearingRadians);
            double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearingRadians);
            
            //Guardamos en un HashMap
            enemiesPositions.put(e.getName(), new Point((int) enemyX, (int) enemyY));
            enemiesBearings.put(e.getName(),e.getBearing());
            
            System.out.println("Mi heading es = "+this.getHeading());
            System.out.println("El bearing de mi enemigo es = "+e.getBearing());
            
            //Enviamos el enemigo scaneado
            try {
                broadcastMessage(new Mensaje(0,enemyX, enemyY, e.getBearing(), 0));
            } catch (IOException ex) {
                Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Calculo de cambios de energia para saber si el enemigo ha disparado o no
            double energiaAnterior;
            
            //Cogemos la energia actual 
            if (enemiesEnergies.containsKey(e.getName())) {
            // Si el enemigo ya esta en el HashMap, puedes acceder al valor
                energiaAnterior = enemiesEnergies.get(e.getName());
                System.out.println("La energia del enemigo es"+energiaAnterior);
            } 
            else {//Si el enemigo no esta en el HashMap es la primera vez que lo detectamos y su carga es 100
                energiaAnterior = 100;
            }
            System.out.println("-----------------------------------------------------------------------------");
            double changeInEnergy = energiaAnterior - e.getEnergy();
            if (changeInEnergy>0 && changeInEnergy<=3) {
            //Intentamos esquivarlo
                movimientoDireccion = -movimientoDireccion;
                System.out.println("-----------------------------------------------------------------------------");
                this.setAhead((e.getDistance()/4+25)*movimientoDireccion);
                try {
                    broadcastMessage(new Mensaje(1, 0, 0, 0, 0));
                } catch (IOException ex) {
                    Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
            enemiesEnergies.put(e.getName(), e.getEnergy());
            
            comprobarVida(e.getName());
        }
        
        else{//Si es compañero de equipo entonces:
            double posX = getX() + e.getDistance() * Math.sin(absoluteBearingRadians);
            double posY = getY() + e.getDistance() * Math.cos(absoluteBearingRadians);
            
            double distancia = calcularDistancia(this.getX(),this.getY(),posX,posY);
            System.out.println("detectado el aliado "+e.getName()+" este tiene un isTeammate()= "+isTeammate(e.getName())+"Esta a una distancia de = "+distancia+" y e.getDistance = "+e.getDistance());
            System.out.println("el aliado detectado con el nombre"+e.getName()+" Esta en la posición x= "+posX+" y= "+posY);
            System.out.println("Mi posicion es x= "+this.getX()+" y= "+this.getY());
            if(distancia < 100){
                System.out.println("********************************************************************************************");
                this.setTurnRight(60);
                this.setAhead(100);
                if(this.getVelocity() == 0){//Si nos estamos chocando con alguien movemos atrás y recolocamos
                    this.setBack(100);
                    this.setTurnRight(60);
                    this.setAhead(100);
                }
            }
        }
    }
    private boolean isLeaderBetweenEnemyAndDroid() {
        // Obtenemos la posición del líder
        double leaderX = getX();
        double leaderY = getY();

        // Calculamos la posición del enemigo más cercano
        String enemyName = buscarEnemigoMasCercano();
        Point enemyPosition = enemiesPositions.get(enemyName);
        double enemyX = enemyPosition.getX();
        double enemyY = enemyPosition.getY();

        // Calculamos la posición de un Droid (ajusta esto según tu lógica)
        double droidX = 0; // Reemplaza con la posición X del Droid
        double droidY = 0; // Reemplaza con la posición Y del Droid

        // Comprobamos si el líder está entre el enemigo y el Droid
        if (isBetween(leaderX, enemyX, droidX) && isBetween(leaderY, enemyY, droidY)) {
            return true;
        }

        return false;
    }

    public void antiGravity(){
	double xForce = 0,yForce = 0,force,power;
	final double antikabe = 7500;
	xForce += antikabe/Math.pow(getBattleFieldWidth()-getX(),3);
	xForce -= antikabe/Math.pow(getX(),3);
	yForce += antikabe/Math.pow(getBattleFieldHeight()-getY(),3);
	yForce -= antikabe/Math.pow(getY(),3);
        // Calcula las fuerzas antigravedad
	double angle = getHeadingRadians()+Math.atan2(yForce, xForce)-Math.PI/2;
        // Calcula el ángulo de giro necesario para evitar obstáculos
	if(angle > Math.PI/2){
            angle -= Math.PI; 
        }
	else if(angle<-Math.PI/2){
            angle += Math.PI; 
        }
	// Calcula la distancia a avanzar para evitar obstáculos
        double distance=1000*(Math.sqrt(xForce*xForce+yForce*yForce));
        // Establece el giro del robot en la dirección calculada
	setTurnRightRadians(angle);
        // Hace que el robot avance hacia atrás para evitar obstáculos
	setAhead(-1*distance);
	}
    
    @Override
    public void onHitWall(HitWallEvent event)
    {//Si chocamos con la paret
        this.setBack(200);//Tirem cap enrere                                                             
        if(getVelocity()==0)//Si el robot esta parado entonces                                                   
        {
            this.setTurnLeft(45); //Giramos 45 grados y nos movemos hacia delante
            this.setAhead(100);
        }
    }
    
    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {//Si nos da un disparo cambiamos el rumbo y movemos
        setTurnRightRadians(Math.PI);
        setAhead(100);
    }
    
    private void comprobarVida(String name){
        double enemyEnergy = enemiesEnergies.get(name);
        double maxEnemyEnergy = 100;
        double enemyLife = maxEnemyEnergy - enemyEnergy;
        System.out.println("Vida estimada del enemigo = " + enemyLife);
        if(enemyLife > 60){
            System.out.println("Enemigo a menos de 20 puntos de vida");
            try {
            broadcastMessage(new Mensaje(2,enemiesPositions.get(name).getX(), enemiesPositions.get(name).getY(), enemiesBearings.get(name), 0));
            } catch (IOException ex) {
            Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }
    @Override
    public void onHitRobot(HitRobotEvent event) {
        // Cambia de dirección cuando choca con otro robot
        setTurnRight(90); // Gira 90 grados después de retroceder
        movimientoDireccion = -movimientoDireccion;
        setAhead(10 * movimientoDireccion); // Puedes ajustar la distancia
        
    }
}


