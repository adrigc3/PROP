/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.checkers.players.TactiCheckers;


import edu.upc.epsevg.prop.checkers.CellType;
import edu.upc.epsevg.prop.checkers.GameStatus;
import edu.upc.epsevg.prop.checkers.IAuto;
import edu.upc.epsevg.prop.checkers.IPlayer;
import edu.upc.epsevg.prop.checkers.MoveNode;
import edu.upc.epsevg.prop.checkers.PlayerMove;
import edu.upc.epsevg.prop.checkers.PlayerType;
import edu.upc.epsevg.prop.checkers.SearchType;
import static edu.upc.epsevg.prop.checkers.SearchType.MINIMAX;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
* Jugador del joc de les DAMES implementa l'algorisme MINIMAX amb
 * les optimitzacions de poda d'alpha i beta
 *
 * @author mariona i adri
 */
public class PlayerMiniMax implements IPlayer,IAuto {
    
    
    private String name;
    private PlayerType jugador;   
    
    private ClasseHeuristica heuristica;
    
    /**
     *  La profunditat maxima pels estats de joc ja inspeccionats
     */
    public int depth=6;

    /**
     * Contador dels nodes inspeccionats
     */
    public int num_nodes=0;
    
    private int profunditat_actual=0;

    /**
     * Tipus de cerca utilitzada per aquest jugador MINIMAX
     */
    public SearchType cerca=MINIMAX; 
      
    
    //ALGORISME MINIMAX AMB PODA ALPHA BETA  
    private boolean nivell_max=false; //saber nivell per max o no
    private double alpha=Double.NEGATIVE_INFINITY;
    private double beta= Double.POSITIVE_INFINITY;
        
    /**
     * Constructora per inicialitzar el Jugador MINIMAX
     * @param name nom del jugador
     */
    public PlayerMiniMax(String name){
        this.name= name; 
        this.jugador= PlayerType.opposite(PlayerType.PLAYER1);
        this.heuristica= new ClasseHeuristica(jugador);
    }
    
    /**
     * Retorna el nom del jugador minimax
     * @return Nom del jugador minimax
     */
    @Override
    public String getName(){
        return ("MiniMaxPlayer ("+name+")");
    }
    
    /**
     *Possible limit de temps a implementar
     */
    @Override
    public void timeout(){ 
        //res
    }
    

     /**
     *Llista possibles moviments en PUNTS
     * @param m Llista de s.getMoves() esta en MOVENODES
     * @return Llista de llistes de possibles moviments PER PUNTS
     */ 
    public List<List<Point>> ll_moviments(List<MoveNode> m) {
        List<List<Point>> r=new ArrayList<>();
        int i=0,s;
        MoveNode fill;
        for(MoveNode pos_m:m){
            s=0;
            while(s<pos_m.getChildren().size()){
               List<Point>  cami_fills=new ArrayList<>();
             
               fill= pos_m.getChildren().get(s);
               cami_fills.add(pos_m.getPoint());
               
               while(!fill.getChildren().isEmpty()){
                   cami_fills.add(fill.getPoint());
                   fill=fill.getChildren().get(0);
               }
               cami_fills.add(fill.getPoint());
               
               r.add(cami_fills);
               ++s;           
            }
        ++i; 
        }
      return r;     
    }
    
    /**
     * Veure si es l'inici de la partida o no
     * @param s L'estat actual del joc
     * @return True si i només si totes les peces estan a la seva posicio inicial
     * sino retorna False
     */
    public boolean inici_partida(GameStatus s){
        boolean isInitialState = true;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {            
                if ((row + col) % 2 != 0) {         // cel·les buides graella de dames
                    continue;
                }

                CellType cell = s.getPos(row, col);
                if (row < 3) {
                    if (s.getCurrentPlayer() == PlayerType.PLAYER1 && cell != CellType.P1) {
                        isInitialState = false;
                        break;
                    }
                    if (s.getCurrentPlayer() == PlayerType.PLAYER2 && cell != CellType.EMPTY) {
                        isInitialState = false;
                        break;
                    }
                } else if (row > 4) {
                    if (s.getCurrentPlayer() == PlayerType.PLAYER1 && cell != CellType.EMPTY) {
                        isInitialState = false;
                        break;
                    }
                    if (s.getCurrentPlayer() == PlayerType.PLAYER2 && cell != CellType.P2) {
                        isInitialState = false;
                        break;
                    }
                } else {
                    if (cell != CellType.EMPTY) {
                        isInitialState = false;
                        break;
                    }
                }
            }
            if (!isInitialState) {  //si es fals parem
                break;
            }
        }
        return isInitialState;
    }
  
    /**
     * ALGORISME MINIMAX AMB PODA DE ALPHABETA
     * @param s L'estat actual del joc
     * @param depth Profunditat actual
     * @param alpha Valor de alpha
     * @param beta Valor de beta
     * @param nivell_max Si nivell MAX en l'algorisme Minimax true, sino false
     * @return El valor trobat per l'algorisme MINIMAX amb poda de alpha beta
     */
    public double alphabeta(GameStatus s, int depth, double alpha, double beta, boolean nivell_max) {
        List<List<Point>> moviments = ll_moviments(s.getMoves());

        if (moviments.isEmpty() || depth == this.depth) {
            return this.heuristica.heuristica(s);
        }

        if (nivell_max) {
            double valorMaxim = Double.NEGATIVE_INFINITY;
            for (List<Point> move : moviments) {
                GameStatus copia = new GameStatus(s);
                copia.movePiece(move);

                valorMaxim = Math.max(valorMaxim, alphabeta(copia, depth - 1, alpha, beta, false));

                if (valorMaxim >= beta) {
                    return valorMaxim;
                }
                alpha = Math.max(alpha, valorMaxim);
            }
            return valorMaxim;
        } else {
            double valorMinim = Double.POSITIVE_INFINITY;
            for (List<Point> move : moviments) {
                GameStatus copia = new GameStatus(s);
                copia.movePiece(move);

                valorMinim = Math.min(valorMinim, alphabeta(copia, depth - 1, alpha, beta, true));

                if (valorMinim <= alpha) {
                    return valorMinim;
                }
                beta = Math.min(beta, valorMinim);
            }
            return valorMinim;
        }
    }

  
    /**
     * Funció del moviment que ha de fer el jugador 
     * @param s L'estat actual del joc
     * @return El MILLOR MOVIMENT d'un jugador
     */
    @Override
    public PlayerMove move(GameStatus s) {

        List<List<Point>> possiblesmov = ll_moviments(s.getMoves());
        List<Point> millormov = new ArrayList<>();

        double millorValor = Double.NEGATIVE_INFINITY;
        
        //long startTime = System.currentTimeMillis();

        if (inici_partida(s)) {     //Estat inicial del joc
            List<MoveNode> nodesInicials = s.getMoves();
            millormov.add(nodesInicials.get(1).getPoint());
            MoveNode nodeSeguent = nodesInicials.get(1).getChildren().get(1);
            millormov.add(nodeSeguent.getPoint());
            return new PlayerMove(millormov, num_nodes, profunditat_actual,cerca);
        }

        //Per algorisme Minimax explorar TOTS els possibles moviments
        for (int p = 0; p < this.depth; p++) {
            for (List<Point> moviment : possiblesmov) {
                GameStatus simulacio = new GameStatus(s);
                simulacio.movePiece(moviment);
                //Comença alpha beta amb nivell mini-> boolea de nivell max = false
                double valorActual = alphabeta(simulacio,this.depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
                if (valorActual > millorValor) {
                    millormov = moviment;
                    millorValor = valorActual;
                    profunditat_actual = p;
                }
            }
        }
        //long endTime = System.currentTimeMillis();
        //System.out.println("Tiempo de ejecución: " + (endTime - startTime) + " ms");

        return new PlayerMove(millormov, num_nodes, profunditat_actual, cerca);
    }

}
