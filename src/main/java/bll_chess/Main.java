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
        Thread gameThread = new Thread(panel);
        gameThread.start();

         try {
            panel.processMove(7, 0, 6, 0);
         } catch (Exception e) {
       
         }
    }
}