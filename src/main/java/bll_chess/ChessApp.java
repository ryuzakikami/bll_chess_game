package main.java.bll_chess;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ChessApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hall Sensor Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Erstelle das GamePanel, das intern das Chessboard und den ArduinoConnector initialisiert
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);



            // Der ArduinoConnector wird bereits im GamePanel-Konstruktor initialisiert
            // Also entfernen wir die doppelte Initialisierung hier:
            // ArduinoConnector sensConnector = new ArduinoConnector("COM6", gamePanel);
            
            gamePanel.startGame();
        });
    }
}