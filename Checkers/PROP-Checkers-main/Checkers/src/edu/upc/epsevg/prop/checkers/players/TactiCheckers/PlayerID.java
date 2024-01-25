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
import static edu.upc.epsevg.prop.checkers.SearchType.MINIMAX_IDS;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.HashMap;

/**
 * Jugador del joc de les DAMES implementa l'algorisme ITERATIVE DEEPENING amb
 * les optimitzacions de poda d'alpha i beta
 *
 * @author mariona i adri
 */
public class PlayerID implements IPlayer, IAuto {

    private String name;
    private PlayerType jugador;

    private ClasseHeuristica heuristica;

    
    /**
     * La profunditat en els estats de joc ja inspeccionats
     */
    public int depth;
    
    /**
     * Contador dels nodes inspeccionats
     */
    public int num_nodes = 0;
    
    /**
     * El timeout desitjat per l'algorisme ID
     */
    public long timeout=200;
    
    
    private int max_depth = 5;
    private int profunditat_actual = 0;

    /**
     * Tipus de cerca utilitzada per aquest jugador MINIMAX_IDS
     */
    public SearchType cerca = MINIMAX_IDS;

    //ALGORISME ID AMB PODA ALPHA BETA  
    private boolean nivell_max = false; //saber nivell per max o no
    private double alpha = Double.NEGATIVE_INFINITY;
    private double beta = Double.POSITIVE_INFINITY;

    //MAPA DE HASH per tots els estats del joc ja revisats
   
    /**
     * Mapa de hash dels estats del joc revisats
     */
    public HashMap<String, Integer> mapaestats;

    /**
     * Constructora per inicialitzar el Jugador ID
     * @param name Nom del jugador entrat
     */
    public PlayerID(String name) {
        this.mapaestats = new HashMap<>();
        this.name = name;
        this.jugador = PlayerType.opposite(PlayerType.PLAYER1);
        this.heuristica = new ClasseHeuristica(jugador);
    }

    /**
     * Retorna el nom del jugador id
     * @return Nom del jugador id 
     */
    @Override
    public String getName() {
        return ("ID_Player (" + name + ")");
    }

    /**
     * Possible limit de temps a implementar
     */
    @Override
    public void timeout() {
        //res
    }

    /**
     * Llista possibles moviments en PUNTS
     * @param m Llista de s.getMoves() esta en MOVENODES
     * @return Llista de llistes de possibles moviments PER PUNTS
     */
    public List<List<Point>> ll_moviments(List<MoveNode> m) {
        List<List<Point>> r = new ArrayList<>();
        int i = 0, s;
        MoveNode fill;
        for (MoveNode pos_m : m) {
            s = 0;
            while (s < pos_m.getChildren().size()) {
                List<Point> cami_fills = new ArrayList<>();

                fill = pos_m.getChildren().get(s);
                cami_fills.add(pos_m.getPoint());

                while (!fill.getChildren().isEmpty()) {
                    cami_fills.add(fill.getPoint());
                    fill = fill.getChildren().get(0);
                }
                cami_fills.add(fill.getPoint());

                r.add(cami_fills);
                ++s;
            }
            ++i;
        }
        return r;
    }

    //**FUNCIONS pel MAPA HASH **//
    
    /**
     * Creació d'una Clau de HASH per aquest estat del joc (s) 
     * Itera per tot el taulell i ajunta cada clau feta per cada posició del taulell
     * @param s L'estat actual del joc
     * @return Una string unica que sera la Clau pel mapa de hash per aquell
     * estat del joc (s)
     */
    public String generarestatHash(GameStatus s) {
        StringBuilder hashBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                hashBuilder.append(s.getPos(i, j).toString());
            }
        }
        return hashBuilder.toString();
    }

    /**
     * Calcula la heuristica per aquest estat del joc (s)
     * @param s L'estat actual del joc
     * @return La heuristica calculada segons la CLASSE HEURISTICA implementada
     * per aquell estat del joc (s)
     */
    public int heuristicaestat(GameStatus s) {
        return this.heuristica.heuristica(s);
    }

    /**
     * Mirar si l'estat s'ha mirat abans o no
     * Genera clau de hash i mira si esta en el mapa de hash del joc
     * - Si esta retorna el valor ja calculat d'aquell estat
     * - Si no esta calcula el valor i el guarda en el mapa de hash
     * @param s L'estat actual del joc
     * @return El valor de la heursitica d'aquest estat del joc
     */
    public int evaluarestat(GameStatus s) {
        String stateHash = generarestatHash(s);
        if (mapaestats.containsKey(stateHash)) {
            return mapaestats.get(stateHash);
        } else {
            int evaluation = heuristicaestat(s);
            mapaestats.put(stateHash, evaluation);
            return evaluation;
        }
    }

    /**
     * ALGORISME ID AMB PODA DE ALPHABETA
     * @param s L'estat actual del joc
     * @param depth Profunditat actual
     * @param alpha Valor de alpha
     * @param beta Valor de beta
     * @param nivell_max Si nivell MAX en l'algorisme ID true, sino false
     * @return El valor trobat per l'algorisme ID amb poda de alpha beta
     */
    public double alphabeta(GameStatus s, int depth, double alpha, double beta, boolean nivell_max) {
        List<List<Point>> moviments = ll_moviments(s.getMoves());

        if (moviments.isEmpty() || depth == this.depth) {
            return evaluarestat(s);
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
     * ALGORISME DEPTH-LIMITED SEARCH(DLS) AMB PODA ALPHA BETA
     * @param node L'estat actual per on la cerca comença
     * @param actual_depth La profunditat que limita l'exploracio
     * @return Una parella de:
     *      Int: El valor calculat en aquest estat del joc(node) determinat per alpha beta
     *      Boolean: Indica si hi han mes moviments a explorar en aquest estat(node)
     */
    public Pair<Integer, Boolean> DLS(GameStatus node, int actual_depth) {
        ++num_nodes;
        if (actual_depth == 0 || node.isGameOver()) { //acabat el joc
            int score = evaluarestat(node);
            return new Pair<>(score, !node.getMoves().isEmpty());
        }

        double resultat;      
        resultat = alphabeta(node, actual_depth, alpha, beta, false);

        boolean movperexplorar = !node.getMoves().isEmpty();
        return new Pair<>((int) resultat, movperexplorar);
    }

    /**
     * Funció del moviment que ha de fer el jugador
     * @param s L'estat actual del joc
     * @return El MILLOR MOVIMENT d'un jugador
     */
    @Override
    public PlayerMove move(GameStatus s) {
        num_nodes = 0; // Reset the node counter
        depth = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<Point> bestMove = null;
        mapaestats.clear();

       // long startTime = System.currentTimeMillis();
        
        long tempsinici = System.currentTimeMillis();
        long tempsfinal = tempsinici + timeout; 

        while ( System.currentTimeMillis() <= tempsfinal) { //temps maxim arribat
            boolean movperexplorar = false;
            profunditat_actual = depth;

            for (List<Point> move : ll_moviments(s.getMoves())) {
                GameStatus nodeseguent = new GameStatus(s);
                nodeseguent.movePiece(move);

                Pair<Integer, Boolean> result = DLS(nodeseguent, depth);
                int score = result.getKey();

                if (score > bestScore) {    //funcio minimax
                    bestScore = score;
                    bestMove = move;
                }
                if (result.getValue()) {
                    movperexplorar = true;
                }
            }
            if (!movperexplorar) {  //No hi ha mes nodes a explorar
                break; 
            }

         depth++;
        }
        //long endTime = System.currentTimeMillis();
        //System.out.println("Tiempo de ejecución: " + (endTime - startTime) + " ms");
        
        return new PlayerMove(bestMove, num_nodes, profunditat_actual, cerca);
    }

}
