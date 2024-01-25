/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.checkers.players.TactiCheckers;

import edu.upc.epsevg.prop.checkers.CellType;
import static edu.upc.epsevg.prop.checkers.CellType.*;
import edu.upc.epsevg.prop.checkers.GameStatus;
import edu.upc.epsevg.prop.checkers.IAuto;
import edu.upc.epsevg.prop.checkers.IPlayer;
import edu.upc.epsevg.prop.checkers.MoveNode;
import edu.upc.epsevg.prop.checkers.PlayerMove;
import edu.upc.epsevg.prop.checkers.PlayerType;
import static edu.upc.epsevg.prop.checkers.PlayerType.*;
import edu.upc.epsevg.prop.checkers.SearchType;
import static edu.upc.epsevg.prop.checkers.SearchType.MINIMAX;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Classe per calcular la HEURISTICA en el joc de les DAMES
 *
 * @author mariona i adri
 */
public class ClasseHeuristica {
    
    private PlayerType jugador;   

    
    /**
     * Constructora de la Classe pel CÀLCUL DE LA HEURISTICA
     * @param jugador Jugador (Player1 o Player2) que va a moure peca
     */
    public ClasseHeuristica(PlayerType jugador) {
        this.jugador=jugador;
    }
    
    
    /**
     * CÀLCUL DE LA HEURISTICA
     * @param s L'estat actual del joc
     * @return El valor de la heuristica en l'estat actual - segons el jugador
     */
    public int heuristica(GameStatus s) {
        int boardVal = 0;
        boolean tfinal = taulerfinal(s);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                CellType peca = s.getPos(row, col);
                switch (peca) {
                    case P1:
                    case P1Q:
                        boardVal += valorPeca(peca, row, col, s, PlayerType.PLAYER1, tfinal);
                        break;
                    case P2:
                    case P2Q:
                        boardVal -= valorPeca(peca, row, col, s, PlayerType.PLAYER2, tfinal);
                        break;
                }
            }
        }
    return boardVal;
}

    /**
     * Valor de la HEURISTICA PER UNA PEÇA
     * @param peca Peça a mirar
     * @param row Fila a mirar
     * @param col Fila a mirar
     * @param s L'estat actual del joc
     * @param jugador Jugador que necessita fer el moviment
     * @param taulerfinal Si esta en l'estat final de la partida o no
     * @return El valor de la heursitica de la peça [col][row]
     */
    public int valorPeca(CellType peca, int row, int col, GameStatus s, PlayerType jugador, boolean taulerfinal) {
       int valor = 0;
        if (peca == CellType.P1Q || peca == CellType.P2Q) {
            valor = plusreina(peca, row, col, taulerfinal, s);
        } else {
            valor = 30; 
        }
        valor += plusdefensa(row, col, s); 
        valor += pluscentre(row, col); 
        valor += (jugador == s.getCurrentPlayer()) ? plusfons(row) : 0;
        valor += plusmobilitat(row, col, s); 
        return valor;
    }
 
    /**
     * Retorna una llista de MoveNode representant tots els moviments legals per a una peça específica.
     * @param row La fila de la peça.
     * @param col La columna de la peça.
     * @param gameStatus L'estat actual del joc.
     * @return Una llista de MoveNode amb tots els moviments legals per a la peça.
    */
  public List<MoveNode> llistamovespeca(int row, int col, GameStatus gameStatus) {
      List<MoveNode> movimentsLegals = new ArrayList<>();

      if (!gameStatus.validateCordinates(row, col)) {
          return movimentsLegals;
        }

      CellType peca = gameStatus.getPos(row, col);
      if (peca == CellType.EMPTY || peca.getPlayer() != gameStatus.getCurrentPlayer()) {
          return movimentsLegals;
        }

      List<MoveNode> movimentsPossibles = gameStatus.getMoves();
      for (MoveNode moviment : movimentsPossibles) {
          if (moviment.getPoint().equals(new Point(row, col))) {
              movimentsLegals.add(moviment);
          }
        } 
      return movimentsLegals;
     }
  

    /**
     *Contador de peces en lestat actual
     * @param s L'estat actual del joc
     * @return El numero de peces en lestat
     */
    public int conta_peces(GameStatus s) {
        int count = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (s.getPos(row, col) != CellType.EMPTY) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Inspecciona si el joc esta en l'estat final - Si hi han entre 5 a 6 peces per jugador
     * @param s L'estat actual del joc
     * @return Retorna true si i nomes si hi han entre 5 i 6 peces per cada jugador en el taulell
     */
    public boolean taulerfinal(GameStatus s) {
        int piecesPlayer1 = 0;
        int piecesPlayer2 = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                CellType peca = s.getPos(row, col);
                if (peca == CellType.P1 || peca == CellType.P1Q) {
                    piecesPlayer1++;
                } else if (peca == CellType.P2 || peca == CellType.P2Q) {
                    piecesPlayer2++;
                }
            }
        }
    return (piecesPlayer1 <= 6 && piecesPlayer1 >= 5) || (piecesPlayer2 <= 6 && piecesPlayer2 >= 5);
    }
    
    
    /**
     * Heuristica millora si esta defensat
     * @param row Fila a mirar
     * @param col Columna a mirar
     * @param s L'estat actual del joc
     * @return Numero de peces que pot defensar des d'aquella casella [col][row]
     */
    public int plusdefensa(int row, int col, GameStatus s) {
        PlayerType currentPlayer = s.getCurrentPlayer();
        int defencapeca = 0;

        // Verifica els límits per evitar anar fora del tauler
        if (row <= 0 || row >= 7 || col <= 0 || col >= 7) {
            return 0;
        }

        //Caselles adjacents
        int[][] offsets = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] offset : offsets) {
            int filacostat = row + offset[0];
            int colcostat = col + offset[1];

            CellType cell = s.getPos(filacostat, colcostat);
            if (cell == null) {
                continue;
            }

            //Peca del costat sigui del mateix jugador!
            if ((currentPlayer == PlayerType.PLAYER1 && (cell == CellType.P1 || cell == CellType.P1Q))
                    || (currentPlayer == PlayerType.PLAYER2 && (cell == CellType.P2 || cell == CellType.P2Q))) {
                ++defencapeca;
            }
        }
        return defencapeca;
    }
    
    /**
     *Heuristica millora si estan mes al centre
     * @param row Fila a mirar
     * @param col Columna a mirar
     * @return Numero que disminueix amb la distancia al centre
     */
    public int pluscentre(int row, int col) {
        int center = 3; // Centre del tauler en un tauler 8x8
        int distanceToCenter = Math.abs(center - row) + Math.abs(center - col);
        return Math.max(0, (7 - distanceToCenter) * 5);
    }
    

    /**
     * Heuristica millora si arriben al fons per crear reines
     * @param row Fila a mirar
     * @return Numero per fer arribar les peces al final
     */
    public int plusfons(int row) {
         int filafinalp1 = 0;
         int filafinalp2 = 7;

         if ((jugador == PlayerType.PLAYER1 && row == filafinalp1) ||
             (jugador == PlayerType.PLAYER2 && row == filafinalp2)) {
                return 15;
         }
         return 0;
    }


    /**
     * Heuristica millora si son REINES 
     * Afegeix més valor a les reines, especialment quan estan protegides
     * @param peca Peça a mirar
     * @param row Fila a mirar
     * @param col Columna a mirar
     * @param taulerfinal  Si esta en l'estat final de la partida o no
     * @param s L'estat actual del joc
     * @return El valor de la heursitica de les reines
     */
    public int plusreina(CellType peca, int row, int col,boolean taulerfinal, GameStatus s) {
        int valorBase = 150; 
        int valorProteccio = plusdefensa(row, col, s) * 10; 
        if (taulerfinal) {
            valorBase += 30; 
        }
        if(peca.isQueen()) valorBase+=100;

        return valorBase + valorProteccio;
    }
    

    /**
     * Heuristica millora si hi han mes moviments a fer despres
     * @param row Fila a mirar
     * @param col Columna a mirar
     * @param s L'estat actual del joc
     * @return El valor de la heursitica si hi han mes moviments despres
     */
    public int plusmobilitat(int row, int col, GameStatus s) {    
        List<MoveNode> moviments = llistamovespeca(row, col, s);
        int valormobilitat = 0;

        for (MoveNode moviment : moviments) {
            GameStatus simulacio = new GameStatus(s);
            PlayerType currentPlayer = s.getCurrentPlayer();

            //Si sacrifici es beneficios pel jugador
            int sacrifici = evaluarsacrifici(simulacio, row, col, currentPlayer);

            if (sacrifici > 0) {
                valormobilitat += 5; //mes punts per si hi ha mes mobilitat
            }
        }

        return valormobilitat;
    }
    

    /**
     * Heuristica millora si es fa un sacrifici 
     * @param s L'estat actual del joc
     * @param row Fila a mirar
     * @param col Columna a mirar
     * @param currentPlayer Jugador que fa la jugada
     * @return El valor de la heuristica si el sacrifici es beneficios
     */
    public int evaluarsacrifici(GameStatus s, int row, int col, PlayerType currentPlayer) {
        int valorsacrifici = 0;

        List<MoveNode> possibleMoves = llistamovespeca(row, col, s);
        for (MoveNode move : possibleMoves) {
            GameStatus simulacio = new GameStatus(s);
            
            if (mataoponent(simulacio, s)) {
                valorsacrifici = 10; 
                break;
            }
        }
    return valorsacrifici;
    }
    
    /**
     * Inspecciona si es maten peces contant i comparant numeros
     * @param nou L'estat NOU del joc
     * @param antic L'estat ANTIC del joc
     * @return Retorna true si i nomes si es mata alguna peça en lestat nou comparan-ho en
     * lestat antic
     */
    public boolean mataoponent(GameStatus nou, GameStatus antic) {
        int num_nou = conta_peces(nou);
        int num_antic = conta_peces(antic);

        return num_nou < num_antic;
    }
     
}