package main.java.bll_chess;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Schachbrett");
        GamePanel panel = new GamePanel();
        
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Starte das Spiel (der GamePanel-Thread übernimmt das kontinuierliche Neuzeichnen)
        panel.startGame();
        
        // Optional: Falls du Testzüge einspielen möchtest, kannst du diese hier hinzufügen.
        // In einem echten Spiel mit realen Figuren erfolgt der Zugablauf durch die Live-Daten vom Arduino.
    }
}
