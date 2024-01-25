/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Ejercicio1;


import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.io.IOException;
import static java.time.Clock.system;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredatorRobot extends TeamRobot {
    private Map<String, Point> teammatePositions = new HashMap<String, Point>(); //Guardamos las posiciones de los aliados
    private Map<String, Point> enemiesPositions = new HashMap<String, Point>(); //Guardamos las posiciones de los enemigos
    private Map<String, Double> enemiesBearings = new HashMap<String, Double>(); //Guardamos los Bearings enemigos
    private Map<String, Double> enemiesEnergies = new HashMap<String, Double>(); //Guardamos la Energia de los enemigos
    boolean aliado2 = false;
    boolean aliado3 = false;
    boolean aliado4 = false;
    boolean aliado5 = false;
    boolean aliado1muerto = false;
    boolean aliado2muerto = false;
    boolean aliado3muerto = false;
    boolean aliado4muerto = false;
    int aliadosVivos = 4;
    //private String targetEnemyName;
    int fase = 0;
    double distMax = 0;
    String enemigoTargeteado = "";
    int count = 0;
    int moveDirection = 1;
    int lado = 1; //Indica hacia que lado estamos giradndo

    @Override
    public void run() {
        setColors(Color.red, Color.black, Color.green); // Colores personalizados
        
        while (true) {
            //System.out.println(getX() + "+" + getY());
            if (getTime() % 20 == 0) {
		moveDirection *= -1;}
            
            switch (fase) {
                case 0: //Fase de Handshake
                    System.out.println("Fase 0000000000000000000");
                    //System.out.println(this.getName());
                    setTurnRadarLeftRadians(Double.POSITIVE_INFINITY);
                    handshakePhase();
              
                break;
                case 1: //Fase de Aproximación
                    System.out.println("Fase Aproximacion");
                    //System.out.println("El # de aliados vivos es:"+aliadosVivos);
                    aproximacionPhase();

                break;
                case 2: //Fase de Orbita
                    System.out.println("Fase Orbita");
                    orbitaPhase();
                
                break;
                case 3: //Fase Kamikaze
                    
                    this.setTurnGunRight(normalizeBearing(getHeading() - getGunHeading() + enemiesBearings.get(enemigoTargeteado))); //Giramos el cañon en direccion al aliado
                    this.setTurnRight(enemiesBearings.get(enemigoTargeteado));
                    setAdjustGunForRobotTurn(false); //Nos aseguramos que todo el cuerpo se mueve junto, asi como vamos en dirección al enemigo, le disparamos a él
                    setAdjustRadarForGunTurn(false);
                    this.setAhead(10);
                    
            }
            //System.out.println(getX() + "+" + getY());

            execute();
        }
    }

    private void handshakePhase(){
        //System.out.println("Hello");

            scan();
            //System.out.println("Hello");
            broadcastTeammatePosition();
            execute();
    }
    private void aproximacionPhase(){
        
        setAdjustGunForRobotTurn(false); //Nos aseguramos que todo el cuerpo se mueve junto, asi como vamos en dirección al enemigo, le disparamos a él
        setAdjustRadarForGunTurn(false);


        //Obtenemos los datos necesarios
        Point enemigoPos = enemiesPositions.get(enemigoTargeteado);
        double posEnemigoX = enemigoPos.getX();
        double posEnemigoY = enemigoPos.getY();
        
        
        // Calculamos el ángulo hacia el enemigo
        //double anguloHaciaEnemigo = calcularAnguloHaciaEnemigo(posEnemigoX, posEnemigoY);
        //double angleToPoint = getHeading() - getRadarHeading() + enemiesBearings.get(enemigoTargeteado);
        //double angleToMove = getHeading() + enemiesBearings.get(enemigoTargeteado);
       
        //System.out.println("Fase Aproximacion --> El enemigo es: " + enemigoTargeteado+ " y sus coordenadas son: OX= "+posEnemigoX+" OY "+posEnemigoY);
        //System.out.println(enemiesBearings.get(enemigoTargeteado));
        
        //Giramos
        if(enemiesBearings.get(enemigoTargeteado) < 180){ //Si el angulo de Bearing del enemigo es menor que 180 mejor girar a la derecha
            this.setTurnRight(enemiesBearings.get(enemigoTargeteado));
        }
        else{ //Si el angulo de Bearing del enemigo es mayor que180 mejor girar a la izquierda
            this.setTurnLeft(enemiesBearings.get(enemigoTargeteado));
        }
        
        //Movemos
        double dist = calcularDistancia(this.getX(), this.getY(), posEnemigoX,posEnemigoY); //calculamos a la distancia que esta el enemigo
        double distMax = getBattleFieldWidth()*0.1;
        //System.out.println("Distancia max= "+distMax);
        if(distMax < dist){/*Solo nos movemos hasta chocar al enemigo cuando estamos o en la fase 4 o tenemos una distancia con
                                            él mayor que la distáncia máxima*/
                  this.setAhead(10);
        }
        else{
            fase ++;
        }
        
        //Disparamos
        //poner el bearing threshold para disparar (Apuntes)
        this.fire(2);

        execute();
    }
    
    private void orbitaPhase(){
	//Si estamos parados cambiamos de dirección de movimiento
        //Ponemos que se puedan mover el arma por si sola
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while (fase == 2) {
            
            //Conseguimos el tiempo que pasa (2 segundos -> aprox 4 ticks)
            //Mientras estemos en fase de orbita se repite el giro
            long currentTime = getTime();
        
            // Calculamos el valor de la variable en función del tiempo
            if (((getTime()) % 20) == 0) //Cambia el comportamiento cada 20 ticks
            {
            lado *= -1;}
            
            //Cogemos el angulo del enemigo le sumamos 90 para tener el perpendicular y girar a su alrededor
            //Al resultado anterior le sumamos 180 para girar en el sentido de las agujas del reloj de manera inicial
            //Cada 2 segundo se cambia el lado de 1 a -1, y de -1 a 1
            setTurnRight((normalizeBearing(enemiesBearings.get(enemigoTargeteado) + 90 + 180)));
            //Limitamos la velocidad
            setMaxVelocity(5);
            
            //Nos movemos hacia delante
            ahead(10 * lado); //Si lado es 1 nos movemos en sentido de las agujas del reloj, si es -1 en sentido contrario
            
            //Calculamos el movimiento del arma del tanque
            double turn = getHeading() - getGunHeading() + enemiesBearings.get(enemigoTargeteado);
            //Normalizamos el angulo de giro del arma
            setTurnGunRight(normalizeBearing(turn));
            //Solo disparamos si el arma está fría y el angulo de giro restante es muy cercano al objetivo
            if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10){
               this.setFire(3);
            }
            
            comprobarVida();//Comprovamos la vida del enemigo y si es menor de 40% de su vida inicial pasamos a la fase de ramming
        }

        
    }
    
    private void broadcastTeammatePosition(){
        teammatePositions.put(getName(), new Point((int) getX(), (int) getY())); //Ponemos nuestras coordenadas en un hashmap
        //broadcastMessage(new Mensaje("hanshake", getX(), getY())); //Enviamos las coordenadas a los aliados
        try {
        broadcastMessage(new Mensaje(0, getX(), getY(),""));
        } catch (IOException ex) {
            ex.printStackTrace(); // Opcional: imprime la traza de la excepción
}
        }   

    //Método que comprueba cual es el enemigo mas 
    public String calcularEnemigoMasCercano() {
        String enemigoMasCercano = null; //Iniciamos el valor como Null
        double distanciaMasAlejada = Double.MAX_VALUE;

        for (Map.Entry<String, Point> entry : enemiesPositions.entrySet()) {
            String nombreEnemigo = entry.getKey();
            Point posicionEnemigo = entry.getValue();
            //System.out.println(posicionEnemigo);
            
            //calculamos la distancia entre el enemigo y nosotros en cada eje
            //double distancia_OX = Math.abs(posicionEnemigo.getX()-getX());
            //double distancia_OY = Math.abs(posicionEnemigo.getY()-getY());
            //Calculamos el valor de distáncia
            //double distancia = Math.sqrt(Math.pow(distancia_OX, 2) + Math.pow(distancia_OY, 2));
            double distancia = calcularDistancia(getX(), getY(), posicionEnemigo.getX(), posicionEnemigo.getY());
            //System.out.println("La distancia a "+nombreEnemigo+" es de "+distancia);
            if (distancia < distanciaMasAlejada) {
                distanciaMasAlejada = distancia;
                enemigoMasCercano = nombreEnemigo;
                distMax = distancia; //Cambiamos el valor de distMax par que cuando se calcule el enemigo mas alejado
                                     //Se tenga un valor como referencia, ya que sino el aliado mas alejado nunca comparara
                                     //Con su enemigo pasado
            }

        }
        enemigoTargeteado = enemigoMasCercano; //Lo ponemos en caso de que sea el más cercano de 1
        return enemigoMasCercano;
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        if(fase != 0){
            return;
        }
        
        else{
            Mensaje men = (Mensaje) e.getMessage();
        
        
            int posOX = (int) Math.round(men.getOX());
            int posOY = (int) Math.round(men.getOY());
            int codigo = men.getCodi();
            String enemigo = men.getEnemyName();
        
        //System.out.println("El nombre es " + e.getSender() + "Posicion en X " +posOX + " Posicion en OY = "+ posOY);
        //System.out.println(teammatePositions.size());
        
            switch(codigo){
        
                case 0: //Handshake
                //Guardamos las coordenadas del mensaje recibido en el mapa de hash
                    teammatePositions.put(e.getSender(), new Point(posOX, posOY));
                    Point teammatePositios = teammatePositions.get(e.getSender());
                    //System.out.println(teammatePositios.getX());
                    //System.out.println(teammatePositios.getY());
                
             
                //Comprovamos que se actualizan las posiciones
                /*    for (Map.Entry<String, Point> entry : teammatePositions.entrySet()) {
                    String teammateName = entry.getKey();
                    Point teammatePosition = entry.getValue();
    
                    System.out.println("Nombre: " + teammateName);
                    System.out.println("Coordenadas: X=" + teammatePosition.getX() + ", Y=" + teammatePosition.getY());
                }*/
                    break;
            
                case 1: //Pasamos enemigo más cercano
                    //System.out.println("Caso 1");
                    //Obtenemos la posicion a partir de los valores guardados en el MAp de los aliados
                    //Estos los necesitamso para calcular los valores absolutos de las distáncias
                    //Y asi saber cual es la más grande e ir a por ese
                    if(("Ejercicio1.PredatorRobot* (1)".equals(this.getName())) || 
                            (aliado1muerto && "Ejercicio1.PredatorRobot* (2)".equals(this.getName())) ||
                            (aliado1muerto && aliado2muerto && "Ejercicio1.PredatorRobot* (3)".equals(this.getName())) ||
                            (aliado1muerto && aliado2muerto && aliado3muerto && "Ejercicio1.PredatorRobot* (4)".equals(this.getName())) ||
                            (aliado1muerto && aliado2muerto && aliado3muerto && aliado4muerto && "Ejercicio1.PredatorRobot* (5)".equals(this.getName()))){ //Añadir variables de que Robot esta vivo si el 1 no esta pasar al dos, si 1 y 2 muertos al 3
                        Point teammatePosition = teammatePositions.get(e.getSender());
                        double posAliadoX = teammatePosition.getX();
                        double posAliadoY = teammatePosition.getY();
                
                        //Calculamos la distancia
                        double distancia = calcularDistancia(posOX, posOY, posAliadoX, posAliadoY);
                
                        //metemos en un hashmap
                        if(distancia>distMax){
                            distMax = distancia;
                            enemigoTargeteado = enemigo;
                        }
                        
                        System.out.println("El enemigo con mayor distancia es: "+enemigoTargeteado+"Con una distancia de: "+distMax);
                        
                        if("Ejercicio1.PredatorRobot* (2)".equals(e.getSender())){
                            if(!aliado2){
                                aliado2=!aliado2;
                                count ++;
                            }
                        }
                        else if("Ejercicio1.PredatorRobot* (3)".equals(e.getSender())){
                            if(!aliado3){
                                aliado3=!aliado3;
                                count ++;
                            }
                        }
                        else if("Ejercicio1.PredatorRobot* (4)".equals(e.getSender())){
                            if(!aliado4){
                                aliado4=!aliado4;
                                count ++;
                            }
                        }
                        else if("Ejercicio1.PredatorRobot* (5)".equals(e.getSender())){
                            if(!aliado5){
                                aliado5=!aliado5;
                                count ++;
                            }
                        }
                        if(count == aliadosVivos){
                            fase = 1;
                            try {
                                broadcastMessage(new Mensaje(2, 0, 0,enemigoTargeteado));
                            } catch (IOException ex) {
                                Logger.getLogger(PredatorRobot.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    //System.out.println("Count = "+count);
                    break;
                    
                case 2:
                    if("Ejercicio1.PredatorRobot* (1)".equals(this.getName())) break;
                    fase = 1;
                    enemigoTargeteado = enemigo;
                    
        }
        /*if (e.getMessage() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Point> message = (Map<String, Point>) e.getMessage();
            teammatePositions.putAll(message);
        }*/
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if(isTeammate(e.getName())){ //Si el robot detectado es un aliado volvemos
            //System.out.println("Aliado");
            return;
        }
        else {
            System.out.println("enemigo detected");
            //Calculo de las coordenadas del enemigo
            
            // Calcular el bearing del enemigo
            double absoluteBearingRadians = getHeadingRadians() + e.getBearingRadians();
            
            //Calculamos la OX y OY del enemigo a partir de nuestra posción la distáncia a él
            //y el seno y coseno del Bearing
            double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearingRadians);
            double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearingRadians);
            /*System.out.println("Mi posicion es "+getX()+" "+getY());
            System.out.println("Enemigo"+e.getName()+" avistado en "+enemyX+" "+enemyY);*/
            enemiesPositions.put(e.getName(), new Point((int) enemyX, (int) enemyY));
            enemiesBearings.put(e.getName(),e.getBearing());
            enemiesEnergies.put(e.getName(), e.getEnergy());
            
            
            if(fase == 0){ //Solo se entra si estamos en fase = 0
                calcularEnemigoMasCercano();
            //System.out.println("Calculado");
                try {//Hay que poner la X y la Y del enemigo mas cercano que es el targeteado, no estamos pasando nada ahora mismo, simplemente el elemento actual
                    broadcastMessage(new Mensaje(1, (int)enemyX, (int)enemyY,e.getName()));
                    } catch (IOException ex) {
                        ex.printStackTrace(); // Opcional: imprime la traza de la excepción
                    } 
            }
        }
    }  
    
    
    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        String robotName = e.getName(); // Nombre del robot que ha muerto
        System.out.println("RObot muertoooo" + robotName);
    
        // Comprueba si el robot que ha muerto es un enemigo
        if (!isTeammate(robotName)){
            enemiesPositions.remove(robotName);
            enemiesBearings.remove(robotName);
            enemiesEnergies.remove(robotName);
            
            if(robotName.equals(enemigoTargeteado)){
                
            //System.out.println("Has eliminado a un enemigo: " + robotName);
                fase = 0;
                count = 0;
                aliado2=false;
                aliado3=false;
                aliado4=false;
                aliado5=false;
                enemigoTargeteado = "";
            }
        }
        else if(isTeammate(robotName)){
            aliadosVivos --;
            System.out.println("ha muerto el aliado "+robotName);
            if(null != robotName)switch (robotName) {
                case "Ejercicio1.PredatorRobot* (1)":
                    aliado1muerto = true;
                    break;
                case "Ejercicio1.PredatorRobot* (2)":
                    aliado2muerto = true;
                    break;
                case "Ejercicio1.PredatorRobot* (3)":
                    aliado3muerto = true;
                    break;
                case "Ejercicio1.PredatorRobot* (4)":
                    aliado4muerto = true;
                    break;
                default:
                    break;
            }
        }
}
    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {//Si nos da un disparo cambiamos el rumbo y movemos
        setTurnRight(90);
        setAhead(100);
    }
    
    @Override
    public void onHitRobot(HitRobotEvent event)
    {
        if(isTeammate(event.getName()))     //Si disparamos a un compañero giramos 45 grados y nos movemos hacia detrás
        {
            setTurnRight(45);
            setBack(100);
        }
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
            
            /*for (Map.Entry<String, Point> entry : enemiesPositions.entrySet()) {
                String teammateName = entry.getKey();
                Point teammatePosition = entry.getValue();
    
                System.out.println("Nombre enemigo: " + teammateName);
                System.out.println("Coordenadas: X=" + enemiesPositions.getX() + ", Y=" + enemiesPositions.getY());
            }
            // Apunta y dispara al objetivo seleccionado
            /*double absBearing = getHeadingRadians() + e.getBearingRadians();
            turnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
            fire(3); // Puedes ajustar la potencia del disparo según sea necesario
        */

    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    private void comprobarVida(){
        double enemyEnergy = enemiesEnergies.get(enemigoTargeteado);
        double maxEnemyEnergy = 100;
        double enemyLife = maxEnemyEnergy - enemyEnergy;
        //System.out.println("Vida estimada del enemigo = " + enemyLife);
        if(enemyLife > 60){
            //System.out.println("Enemigo a menos de 20 puntos de vida");
            fase++;
        }
    }
    
    private double calcularAnguloHaciaEnemigo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.toDegrees(Math.atan2(dy, dx)) - getHeading();
    }
    double normalizeBearing(double angle) {
	while (angle >  180) angle -= 360;
	while (angle < -180) angle += 360;
	return angle;
}
}


