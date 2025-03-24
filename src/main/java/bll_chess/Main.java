package main.java.bll_chess;
import javax.swing.JFrame;

import main.java.bll_chess.piece.Sound;
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
        Thread gameThread = new Thread(panel);
        gameThread.start();

        try {
       
            panel.processMove(6, 3, 4, 3);
            Thread.sleep(1000);   
            panel.processMove(1, 4, 3, 4);
            Thread.sleep(1000);
            panel.processMove(7, 3, 5, 3);
            Thread.sleep(1000);
            panel.processMove(3, 4, 4, 3);
            Thread.sleep(1000);
            panel.processMove(7, 2, 6, 3);
            Thread.sleep(1000);
            panel.processMove(0, 4, 1, 4);
            Thread.sleep(1000);
            panel.processMove(7, 1, 5, 2);
            Thread.sleep(1000);
            panel.processMove(4, 3, 5, 2);
            Thread.sleep(1000);
            panel.processMove(7, 4, 7, 2);
            Thread.sleep(1000);
           
            } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}