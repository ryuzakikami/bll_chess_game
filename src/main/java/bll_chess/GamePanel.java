package main.java.bll_chess;

import java.awt.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import main.java.bll_chess.piece.King;
import main.java.bll_chess.piece.Piece;
import main.java.bll_chess.piece.Rook;

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    public static final int WIDTH = Chessboard.MAX_COL * Chessboard.SQUARE_SIZE;
    public static final int HEIGHT = Chessboard.MAX_ROW * Chessboard.SQUARE_SIZE;

    // Felder zur Fehlererkennung und Blockierung
    private int invalidTargetRow = -1;
    private int invalidTargetCol = -1;
    private int originalRow = -1;
    private int originalCol = -1;
    // Blockiert alle Züge, solange ein illegaler Zug korrigiert werden muss.
    private boolean movesBlocked = false;
    
    private Chessboard chessboard;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        chessboard = new Chessboard();
    }

    /**
     * Wird beim Klicken auf das Spielfeld aufgerufen.
     * Bei einem blockierten Zustand (illegaler Zug) wird nur der Rückzug zugelassen.
     */
    public void processMove(int fromRow, int fromCol, int toRow, int toCol) {
  // Neue Abfrage: Sind wir gerade in einer Promotion?
  if (chessboard.isPromotionPending()) {
    System.out.println("Bitte warten Sie, bis die Umwandlung abgeschlossen ist.");
    return;
}

// ... restliche Logik wie bisher ...
Piece movingPiece = chessboard.getBoard()[fromRow][fromCol];
Piece targetPiece = chessboard.getBoard()[toRow][toCol];

if (movingPiece == null) {
    System.out.println("processMove: No piece at the source position!");
    resetInvalidState();
    repaint();
    return;
}

System.out.println("processMove: Moving piece color = " + movingPiece.getColor());

// Prüfe, ob der richtige Spieler am Zug ist.
if (!chessboard.isValidTurn(movingPiece)) {
    System.err.println("Falscher Spieler am Zug!");
    return;
}

// Grundlegende Zugprüfung (z. B. nicht auf eigene Figur ziehen)
if (movingPiece.isSameColor(targetPiece) || !movingPiece.isValidMove(toCol, toRow, chessboard.getBoard())) {
    invalidTargetRow = toRow;
    invalidTargetCol = toCol;
    originalRow = fromRow;
    originalCol = fromCol;
    movesBlocked = true;
    repaint();
    System.out.println("Invalid move. Highlighting invalid position.");
    return;
}

// Zusätzliche Prüfung: Führt der Zug dazu, dass der eigene König im Schach steht?
if (chessboard.isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
    invalidTargetRow = toRow;
    invalidTargetCol = toCol;
    originalRow = fromRow;
    originalCol = fromCol;
    movesBlocked = true;
    repaint();
    System.out.println("Illegal move: Move leaves king in check! Please reverse the move.");
    animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
    return;
}

// Falls alle Prüfungen erfolgreich sind, wird der Zug legal ausgeführt.
resetInvalidState();
animateMove(movingPiece, fromRow, fromCol, toRow, toCol, false);
}
    

    /**
     * Animiert einen Zug.
     * Bei legalen Zügen (revert == false) wird am Ende der Bewegung der Zug committet.
     * Bei illegalen Zügen (revert == true) wird der Zug _nicht_ in der Spiellogik übernommen,
     * sondern nur visuell angezeigt.
     */
    private void animateMove(Piece movingPiece, int fromRow, int fromCol, int toRow, int toCol, boolean revert) {
        int squareSize = Chessboard.SQUARE_SIZE;
        int startX = fromCol * squareSize;
        int startY = fromRow * squareSize;
        int targetX = toCol * squareSize;
        int targetY = toRow * squareSize;
        int deltaX = targetX - startX;
        int deltaY = targetY - startY;

        int frames = 30;
        int delay = 15; // Etwas längere Verzögerung für flüssige Animation
        final int[] frameCount = {0};

        // Überprüfe, ob es sich um einen Rochade-Zug handelt.
        final boolean isCastling = (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2);
        final Rook castlingRook;
        final int rookFromCol, rookToCol;
        if (isCastling) {
            rookFromCol = (toCol > fromCol) ? 7 : 0;
            rookToCol = (toCol > fromCol) ? toCol - 1 : toCol + 1;
            castlingRook = (Rook) chessboard.getBoard()[fromRow][rookFromCol];
        } else {
            rookFromCol = -1;
            rookToCol = -1;
            castlingRook = null;
        }

        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            frameCount[0]++;
            float t = frameCount[0] / (float) frames;

            int offsetX = Math.round(deltaX * t);
            int offsetY = Math.round(deltaY * t);
            movingPiece.setAnimOffset(offsetX, offsetY);

            if (isCastling && castlingRook != null) {
                int rookDeltaX = (rookToCol - rookFromCol) * squareSize;
                int rookOffsetX = Math.round(rookDeltaX * t);
                castlingRook.setAnimOffset(rookOffsetX, 0);
            }

            repaint();

            if (frameCount[0] >= frames) {
                timer.stop();
                // Bei legalen Zügen wird der Zug committet und der Spielerwechsel vollzogen.
                if (!revert) {
                    chessboard.movePiece(fromRow, fromCol, toRow, toCol, true);
                }
                // Bei illegalen Zügen (revert == true) wird _nichts_ in der Spiellogik verändert.
                // Es werden lediglich die Animations-Offsets zurückgesetzt.
                movingPiece.setAnimOffset(0, 0);
                if (isCastling && castlingRook != null) {
                    castlingRook.setAnimOffset(0, 0);
                }
                repaint();
            }
        });
        timer.start();
    }
    
    private void resetInvalidState() {
        invalidTargetRow = -1;
        invalidTargetCol = -1;
        originalRow = -1;
        originalCol = -1;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
    
        chessboard.draw(g2);
    
        // Zeichne das rote Feld für das Ziel eines illegalen Zugs.
        if (invalidTargetRow != -1 && invalidTargetCol != -1) {
            int targetX = invalidTargetCol * Chessboard.SQUARE_SIZE;
            int targetY = invalidTargetRow * Chessboard.SQUARE_SIZE;
            int squareSize = Chessboard.SQUARE_SIZE;
            g2.setColor(new Color(255, 0, 0, 128));
            g2.fillRect(targetX, targetY, squareSize, squareSize);
        }
        
        // Neuer visueller Indikator: Anzeigen des betroffenen Figuren-Namens und eines Pfeils.
        if (originalRow != -1 && originalCol != -1) {
            int origX = originalCol * Chessboard.SQUARE_SIZE;
            int origY = originalRow * Chessboard.SQUARE_SIZE;
            // Ermittele die betroffene Figur
            Piece piece = chessboard.getBoard()[originalRow][originalCol];
            if (piece != null) {
                // Zeichne einen dicken, blauen Pfeil, der von oberhalb des Feldes zur Mitte der Figur zeigt.
                int centerX = origX + Chessboard.SQUARE_SIZE / 2;
                int centerY = origY + Chessboard.SQUARE_SIZE / 2;
                
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(3));  // 3 Pixel dick
                g2.setColor(Color.red);
                // Zeichne die Pfeillinie
                g2.drawLine(centerX, origY - 50, centerX, centerY);
                // Zeichne die Pfeilspitze
                int arrowSize = 30;
                g2.fillPolygon(
                    new int[]{centerX - arrowSize / 2, centerX + arrowSize / 2, centerX},
                    new int[]{centerY - arrowSize, centerY - arrowSize, centerY},
                    3
                );
                // Stelle den alten Stroke wieder her.
                g2.setStroke(oldStroke);
            }
        }
    }
     
    @Override
    public void run() {
        while (gameThread != null) {
            // Hier können weitere Hintergrundprozesse abgearbeitet werden.
        }
    }
    
    public void startGame() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
    
    public void stopGame() {
        gameThread = null;
    }
}
