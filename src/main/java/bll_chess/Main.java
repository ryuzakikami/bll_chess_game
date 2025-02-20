package main.java.bll_chess;

import javax.swing.JFrame;
public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Schachbrett");
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setLocationRelativeTo(null); 
        window.setVisible(true);     
 
        try {
       
            gamePanel.processMove(1, 4, 3, 4); 
            Thread.sleep(1000);
            gamePanel.processMove(6, 3, 4, 3); 
            Thread.sleep(1000);
            gamePanel.processMove(3, 4, 4, 4);
            Thread.sleep(1000);
            gamePanel.processMove(4, 3, 3, 3);
            Thread.sleep(1000);
            gamePanel.processMove(4, 4, 5, 4);
            Thread.sleep(1000);
            gamePanel.processMove(3, 3, 2, 3);
            Thread.sleep(1000);
            gamePanel.processMove(5, 4, 6, 5);
            Thread.sleep(1000);
            gamePanel.processMove(7, 4, 6, 3);
            Thread.sleep(1000);
            gamePanel.processMove(6, 5, 7, 6);
           



        } catch (Exception e) {
            e.printStackTrace();
        }
        
      
 }
}

