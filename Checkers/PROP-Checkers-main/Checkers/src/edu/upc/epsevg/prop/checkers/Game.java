package edu.upc.epsevg.prop.checkers;

import edu.upc.epsevg.prop.checkers.players.HumanPlayer;
import edu.upc.epsevg.prop.checkers.players.RandomPlayer;
import edu.upc.epsevg.prop.checkers.IPlayer;
import static edu.upc.epsevg.prop.checkers.PlayerType.*;
import edu.upc.epsevg.prop.checkers.players.OnePiecePlayer;
//Jugadors a fer
import edu.upc.epsevg.prop.checkers.players.TactiCheckers.PlayerID;
import edu.upc.epsevg.prop.checkers.players.TactiCheckers.PlayerMiniMax;



import javax.swing.SwingUtilities;

/**
 * Checkers: el joc de taula.
 * @author bernat
 */
public class Game {
        /**
     * @param args
     */
    public static void main(String[] args) { 
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
               // //Jugador donat ONEPIECEPLAYER
              
                // IPlayer player1 = new HumanPlayer("Human");
                // IPlayer player1 = new OnePiecePlayer(1); 
               
                IPlayer player1 = new RandomPlayer("Kamikaze 1");
                // IPlayer player2 = new RandomPlayer("Kamikaze 2");
                
                IPlayer player2= new PlayerMiniMax("Jugador MiniMax");
                //IPlayer player2= new PlayerID("Jugador ID");
             
                System.out.println("Inici");
              new Board(player1 , player2, 1, false);
                
     
                
            }
        });
    }
}
