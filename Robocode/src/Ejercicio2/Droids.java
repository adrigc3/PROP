/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Ejercicio2;

import robocode.*;


/**
 *
 * @author VLLAN
 */
public class Droids extends TeamRobot implements Droid {
/*
    Posibilidad es la de inicialmente colocarlos mirando al angulo 0 es decir a la derecha así lo unico que hay que hacer es sumarle el bearing a cada droid
    */
    double anguloEnGrados;
    int contador = 0;
    int movimientoDireccion = 1;
    double distancia;

    private boolean liderEntreEnemigoDroide = false;
    
    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    
   @Override
    public void run() {
        // Agrega un bucle para que el Droid se mueva continuamente
        while (true) {

           // Genera un número aleatorio para la dirección de movimiento (izquierda o derecha)
            int anguloGiro = (Math.random()<0.5) ? 1 : - 1;

            // Genera un número aleatorio para la distancia de movimiento
            int distanciaAleatoria = (int) (Math.random() * 200) + 100; // Valores aleatorios entre 100 y 300

            // Gira aleatoriamente
            turnRight(10 * anguloGiro);  // Puedes ajustar el ángulo de giro

            // Avanza una distancia aleatoria
            ahead(distanciaAleatoria);

            // Ejecuta las acciones de movimiento
            execute();
        }
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        System.out.println("Holaaaaa");
        Mensaje men = (Mensaje) e.getMessage();
        int codigo = men.getCode();
        
        double posOX = men.getOX();
        double posOY = men.getOY();
        double enemyBearing = men.getBearing();
        System.out.println("La posición del enemigo es: "+posOX+" "+posOY+" y tiene un bearing = "+enemyBearing);
        //this.setTurnRight(enemyBearing);
        System.out.println("Mi heading es = "+this.getHeading());
        double angulo = Math.atan2(posOY - this.getY(), posOX - this.getX());
        // Convierte el ángulo de radianes a grados si es necesario
        anguloEnGrados = Math.toDegrees(angulo);
        anguloEnGrados -= 90;
        anguloEnGrados *= (-1); /*Debemos ponerlo para que miren de manera correcta al robot enemigo*/
        
        switch (codigo) {
            case 0://Caso normal
                {
                    //System.out.println("El angulo es de = "+anguloEnGrados);
                    contador ++;
                    //System.out.println("El angulo normalizado es de = "+anguloEnGrados);
                    //System.out.println("El angulo normalizado * (-1) es de = "+anguloEnGrados);
                    if (men.getShouldNotFire() == 0 || !liderEntreEnemigoDroide) {
                        // Realiza el disparo solo si no se ha recibido la orden de no disparar
                        // y si el líder no está entre el enemigo y el Droide
                        this.setTurnRight(anguloEnGrados - this.getHeading());
                        this.fire(2);
                }
                    //this.setAhead(50*movimientoDireccion);
                    distancia = calcularDistancia(getX(), getY(), posOX, posOY);
                    break;
                }
            case 1://Esquivamos disparo
                movimientoDireccion = -movimientoDireccion;
                this.setAhead(50*movimientoDireccion);
                //System.out.println("-----------------------------------------------------------------------------");
                break;
            case 2://Disparamos 
                if (!liderEntreEnemigoDroide) {
                    // Realiza el disparo solo si el líder no está entre el enemigo y el Droide
                    this.setTurnRight(anguloEnGrados - this.getHeading());
                    this.fire(3);
                }
                break;
                case 3:
                    // El líder envía un mensaje especial indicando si no deben disparar
                    int shouldNotFire = men.getShouldNotFire();
                    if (shouldNotFire == 1) {
                        liderEntreEnemigoDroide = true;
                    } else {
                        liderEntreEnemigoDroide = false;
                    }
                    break;
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
}

