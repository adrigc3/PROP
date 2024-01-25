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

/**
 *
 * @author VLLAN
 */
public class Leader extends TeamRobot{
    private Map<String, Point> enemiesPositions = new HashMap<String, Point>(); //Guardamos las posiciones de los enemigos
    private Map<String, Double> enemiesBearings = new HashMap<String, Double>(); //Guardamos los Bearings enemigos
    private Map<String, Double> enemiesEnergies = new HashMap<String, Double>(); //Guardamos la Energia de los enemigos
    
    int aliados = 4, enemigos;
    String enemigoTargeteado;
    int movimientoDireccion = 1;
    
    @Override
    public void run() {
        enemigos = getOthers() - aliados; //Obtenemos el número de robots enemigos que hay en el mapa
        System.out.println("Hay un total de = "+enemigos+" Robots enemigos");
        
	while (true) {
            setTurnRadarRight(10000);
            this.setAhead(100);
            this.setBack(100);
            antiGravity();
            execute();
	}

        
    }
    
    
    
    
    
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e){ /*podemos poner que cuando el robot leader detecta que alguien ha disparado, esquivamos la bala que puede haber soltado
                                                       para ello entrar -> https://web.archive.org/web/20150907201530/http://www.ibm.com/developerworks/library/j-dodge/index.html
                                                       Cuando se recibe esto que llegue un -1 y lo multiplicamos por la direccion a la que nos movemos para ir atrás y esquivar. Una vez
                                                       mover hacia atrás cambiamos movimiento a 1.
        
                                                       Otras opciones -> https://web.archive.org/web/20160808235930/http://www.ibm.com/developerworks/library/j-tipstrats/index.html
        */
        if(!isTeammate(e.getName())){
            // Calcular el bearing del enemigo
            double absoluteBearingRadians = getHeadingRadians() + e.getBearingRadians();
            
            //Calculamos la OX y OY del enemigo a partir de nuestra posción la distáncia a él
            //y el seno y coseno del Bearing
            double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearingRadians);
            double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearingRadians);
            
            //Guardamos en un HashMap
            enemiesPositions.put(e.getName(), new Point((int) enemyX, (int) enemyY));
            enemiesBearings.put(e.getName(),e.getBearing());
            
            System.out.println("Mi heading es = "+this.getHeading());
            System.out.println("El bearing de mi enemigo es = "+e.getBearing());
            
            if(enemiesPositions.size() == enemigos){ //Si el #enemigos == size del hashMap entonces
                //System.out.println("Hola tengo un Hashmap con la cantidad correcta de enemigos");
                //System.out.println(enemiesPositions.size());
                calcularEnemigoMasCercano();
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
            
            if(e.getName() == enemigoTargeteado){ //Si el enemigo que hemos scaneado es el enemigo targeteado le comprovamos la vida para saber como atacarle
                comprobarVida();
            }
        }
    }
    
    public void calcularEnemigoMasCercano() {
        String enemigoMasCercano = null; //Iniciamos el valor como Null
        double distanciaMasCercana = Double.MAX_VALUE;
        double enemyOX,enemyOY, bearing;

        for (Map.Entry<String, Point> entry : enemiesPositions.entrySet()) {
            String nombreEnemigo = entry.getKey();
            Point posicionEnemigo = entry.getValue();
            //System.out.println("El enemigo "+nombreEnemigo+" esta en la posición = "+posicionEnemigo);

            //Obtenemos el OX y OY del enemigo
            enemyOX = posicionEnemigo.getX();
            enemyOY = posicionEnemigo.getY();
            //calculamos la distancia entre el enemigo y nosotros en cada eje

            //Calculamos el valor de distáncia
            
            double distancia = calcularDistancia(getX(), getY(), enemyOX, enemyOY);
            //System.out.println("La distancia a "+nombreEnemigo+" es de "+distancia);
            if (distancia < distanciaMasCercana) {
                distanciaMasCercana = distancia;
                enemigoMasCercano = nombreEnemigo;
            }

        }
        enemigoTargeteado = enemigoMasCercano; //Lo ponemos en caso de que sea el más cercano de 1
        
        enemyOX = enemiesPositions.get(enemigoTargeteado).getX();
        enemyOY = enemiesPositions.get(enemigoTargeteado).getY();
        bearing = enemiesBearings.get(enemigoTargeteado);
        
        this.setTurnRight(-this.getHeading());
        
        
       // System.out.println("El enemigo llamado "+enemigoTargeteado+" esta en la posición "+enemyOX+" "+enemyOY);
       //Enviamos el mensaje a los compañeros del enemigo más cercano
        try {
            broadcastMessage(new Mensaje(0,enemyOX, enemyOY, bearing, 0 ));
        } catch (IOException ex) {
            Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        
    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    
    public void antiGravity(){
	double xForce = 0,yForce = 0,force,power;
	final double antikabe = 7500;
	xForce += antikabe/Math.pow(getBattleFieldWidth()-getX(),3);
	xForce -= antikabe/Math.pow(getX(),3);
	yForce += antikabe/Math.pow(getBattleFieldHeight()-getY(),3);
	yForce -= antikabe/Math.pow(getY(),3);

	double angle = getHeadingRadians()+Math.atan2(yForce, xForce)-Math.PI/2;
	if(angle > Math.PI/2){
            angle -= Math.PI; 
        }
	else if(angle<-Math.PI/2){
            angle += Math.PI; 
        }
	
        double distance=3000*(Math.sqrt(xForce*xForce+yForce*yForce));
	setTurnRightRadians(angle);
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
        setTurnRight(90);
        setAhead(100);
    }
    
    private void comprobarVida(){
        double enemyEnergy = enemiesEnergies.get(enemigoTargeteado);
        double maxEnemyEnergy = 100;
        double enemyLife = maxEnemyEnergy - enemyEnergy;
        System.out.println("Vida estimada del enemigo = " + enemyLife);
        if(enemyLife > 60){
            System.out.println("Enemigo a menos de 20 puntos de vida");
            try {
            broadcastMessage(new Mensaje(2,enemiesPositions.get(enemigoTargeteado).getX(), enemiesPositions.get(enemigoTargeteado).getY(), enemiesBearings.get(enemigoTargeteado), 0));
            } catch (IOException ex) {
            Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }

}


