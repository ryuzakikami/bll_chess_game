package main.java.bll_chess;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class ChessApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1) Erstelle das JFrame
            JFrame frame = new JFrame("Hall Sensor Chess");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            // 2) Erstelle das GamePanel
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 3) Starte den Spiel‐Thread
            gamePanel.startGame();
        

            // 4) Erstelle den ArduinoConnector
            ArduinoConnector connector = new ArduinoConnector(gamePanel);

            // 5) Füge WindowListener hinzu, der beim Schließen nach Bestätigung stopGame() und close() aufruft
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int result = JOptionPane.showConfirmDialog(
                            frame,
                            "Möchten Sie die Session wirklich beenden?",
                            "Beenden bestätigen",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        // 1) Stoppe den Spiel‐Thread
                        gamePanel.stopGame();
                        // 2) Schließe den Arduino‐Connector
                        connector.close();
                        // 3) Fenster schließen und Anwendung beenden
                        frame.dispose();
                        System.exit(0);
                    }
                  
                }
            });
        }
        );
        
    }
}
