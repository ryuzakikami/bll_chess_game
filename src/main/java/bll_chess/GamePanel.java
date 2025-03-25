package main.java.bll_chess;

import java.awt.*;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import main.java.bll_chess.piece.King;
import main.java.bll_chess.piece.Pawn;
import main.java.bll_chess.piece.Piece;
import main.java.bll_chess.piece.Rook;
import main.java.bll_chess.piece.Sound;
import main.java.bll_chess.piece.Sound.SoundType;

/**
 * Die Klasse GamePanel repräsentiert das Spiel-Panel, in dem das Schachbrett
 * gezeichnet und die Spielzüge animiert werden. Zusätzlich werden hier Eingaben
 * verarbeitet, ungültige Züge hervorgehoben und Sounds abgespielt.
 */
public class GamePanel extends JPanel implements Runnable {
    // Thread für das Spiel, um kontinuierlich zu aktualisieren
    private Thread gameThread;
    public static final int SQUARE_SIZE = 100;  // Größe eines Schachfeldes in Pixeln
    public static final int MARGIN = Chessboard.MARGIN;  // Abstand um das Brett
    public static final int WIDTH = Chessboard.MAX_COL * SQUARE_SIZE + 2 * MARGIN;  // Gesamtbreite des Panels
    public static final int HEIGHT = Chessboard.MAX_ROW * SQUARE_SIZE + 2 * MARGIN; // Gesamthöhe des Panels

    // Variablen zur Markierung von ungültigen Zügen
    private int invalidTargetRow = -1;
    private int invalidTargetCol = -1;
    private int originalRow = -1;
    private int originalCol = -1;
    private boolean movesBlocked = false;
    
    // Das Schachbrett-Objekt, welches die Spiellogik enthält
    private Chessboard chessboard;

    /**
     * Konstruktor des GamePanel.
     * Setzt die bevorzugte Größe und initialisiert das Schachbrett.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        chessboard = new Chessboard();
    }

    /**
     * Prüft, ob der angegebene Zug ein En-Passant-Zug ist.
     * Hierzu wird überprüft, ob der bewegende Bauer diagonal zieht und
     * der benachbarte gegnerische Bauer für En Passant anfällig ist.
     *
     * @param movingPiece Die ziehende Figur
     * @param toRow       Zielreihe
     * @param toCol       Zielspalte
     * @return true, wenn es sich um einen En-Passant-Zug handelt, sonst false
     */
    private boolean isEnPassantMove(Piece movingPiece, int toRow, int toCol) {
        if (!(movingPiece instanceof Pawn)) {
            return false;
        }
        Pawn pawn = (Pawn) movingPiece;
        int currentRow = pawn.getRow();
        int currentCol = pawn.getCol();
        
        // Prüfe, ob diagonal gezogen wird (ein Feld)
        if (Math.abs(toCol - currentCol) == 1 && toRow == currentRow + pawn.getDirection()) {
            // Hole den benachbarten Gegnerbauern
            Piece opponentPiece = chessboard.getBoard()[currentRow][toCol];
            if (opponentPiece instanceof Pawn) {
                Pawn opponentPawn = (Pawn) opponentPiece;
                // En Passant ist möglich, wenn der gegnerische Bauer gerade den Doppelschritt gemacht hat
                // und auf der gleichen Reihe steht
                if (opponentPawn.isEnPassantEligible() && opponentPawn.getRow() == currentRow) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verarbeitet einen Zug vom Feld (fromRow, fromCol) zum Feld (toRow, toCol).
     * Dabei werden diverse Prüfungen (z. B. Spielerzug, ungültige Züge, Schach) durchgeführt,
     * Animationen gestartet und Sounds abgespielt.
     *
     * @param fromRow Startreihe
     * @param fromCol Startspalte
     * @param toRow   Zielreihe
     * @param toCol   Zielspalte
     */
    public void processMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Falls Züge momentan blockiert sind, abbrechen
        if (movesBlocked) {
            System.out.println("Moves are blocked. Please correct the invalid move first.");
            return;
        }
        // Falls gerade eine Bauernumwandlung (Promotion) aussteht, warte auf Abschluss
        if (chessboard.isPromotionPending()) {
            System.out.println("Bitte warten Sie, bis die Umwandlung abgeschlossen ist.");
            return;
        }

        Piece movingPiece = chessboard.getBoard()[fromRow][fromCol];
        if (movingPiece == null) {
            System.out.println("processMove: No piece at the source position!");
            resetInvalidState();
            repaint();
            return;
        }

        System.out.println("processMove: Moving piece color = " + movingPiece.getColor());
        
        // Prüfe, ob die Figur zum aktuellen Spieler gehört
        if (!chessboard.isValidTurn(movingPiece)) {
            System.err.println("Falscher Spieler am Zug!");
            return;
        }

        // Prüfe, ob der Zug ein En-Passant-Zug ist
        boolean enPassant = isEnPassantMove(movingPiece, toRow, toCol);
        Piece targetPiece = chessboard.getBoard()[toRow][toCol];
        // Falls es sich nicht um En Passant handelt, führe Standard-Prüfungen durch
        if (!enPassant) {
            if (movingPiece.isSameColor(targetPiece) ||
                !movingPiece.isValidMove(toCol, toRow, chessboard.getBoard())) {
                // Markiere und animiere einen ungültigen Zug
                markInvalidMove(fromRow, fromCol, toRow, toCol);
                animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
                System.out.println("Invalid move.");
                return;
            }
        }
        
        // Prüfe, ob der Zug den König im Schach belässt
        if (chessboard.isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
            markInvalidMove(fromRow, fromCol, toRow, toCol);
            System.out.println("Illegal move: Move leaves king in check! Please reverse the move.");
            animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
            return;
        }
        // Spiele den entsprechenden Sound: Schlag- oder Zug-Sound
        if (targetPiece != null || enPassant) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_TAKES : SoundType.BLACK_CAPTURES);
        } else {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_MOVE : SoundType.BLACK_MOVE);
        }

        // Zusätzlicher Sound bei Rochade
        if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_CASTLE : SoundType.BLACK_CASTLE);
        }
        // Setze die Markierung für ungültige Züge zurück
        resetInvalidState();
        // Starte die Zug-Animation
        animateMove(movingPiece, fromRow, fromCol, toRow, toCol, false);
    }

    /**
     * Animiert den Zug einer Figur von der Start- zur Zielposition.
     * Dabei werden auch spezielle Animationen für Rochade berücksichtigt.
     *
     * @param movingPiece Die bewegte Figur
     * @param fromRow     Startreihe
     * @param fromCol     Startspalte
     * @param toRow       Zielreihe
     * @param toCol       Zielspalte
     * @param revert      Flag, ob die Animation rückgängig gemacht werden soll (bei ungültigen Zügen)
     */
    private void animateMove(Piece movingPiece, int fromRow, int fromCol,
                             int toRow, int toCol, boolean revert) {
        int squareSize = Chessboard.SQUARE_SIZE;
        int startX = fromCol * squareSize;
        int startY = fromRow * squareSize;
        int targetX = toCol * squareSize;
        int targetY = toRow * squareSize;
        int deltaX = targetX - startX;
        int deltaY = targetY - startY;

        int frames = 30;  // Anzahl der Animationsframes
        int delay = 15;   // Verzögerung in Millisekunden zwischen den Frames
        final int[] frameCount = {0};

        // Prüfe, ob es sich um eine Rochade handelt
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

        // Timer für die Animations-Frames
        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            frameCount[0]++;
            float t = frameCount[0] / (float) frames;

            // Berechne den aktuellen Offset basierend auf der Animationszeit
            int offsetX = Math.round(deltaX * t);
            int offsetY = Math.round(deltaY * t);
            movingPiece.setAnimOffset(offsetX, offsetY);

            // Falls Rochade, animiere auch den Turm
            if (isCastling && castlingRook != null) {
                int rookDeltaX = (rookToCol - rookFromCol) * squareSize;
                int rookOffsetX = Math.round(rookDeltaX * t);
                castlingRook.setAnimOffset(rookOffsetX, 0);
            }

            SwingUtilities.invokeLater(this::repaint);

            if (frameCount[0] >= frames) {
                timer.stop();
                if (!revert) {
                    // Führe den Zug auf dem Schachbrett aus (und wechsle den Spieler)
                    chessboard.movePiece(fromRow, fromCol, toRow, toCol, true);
                    if (isCastling && castlingRook != null) {
                        // Bei Rochade auch den Turm bewegen
                        chessboard.movePiece(fromRow, rookFromCol, toRow, rookToCol, true);
                    }
                    checkGameStatus();
                }
                // Setze Animationen zurück
                movingPiece.setAnimOffset(0, 0);
                if (isCastling && castlingRook != null) {
                    castlingRook.setAnimOffset(0, 0);
                }
                SwingUtilities.invokeLater(this::repaint);
            }
        });
        timer.start();
    }

    /**
     * Prüft den Spielstatus (Schach, Schachmatt, Patt) und spielt ggf. entsprechende Sounds.
     */
    private void checkGameStatus() {
        SwingUtilities.invokeLater(() -> {
            int currentPlayer = chessboard.getCurrentPlayer();
            boolean inCheck = chessboard.isKingInCheck(currentPlayer);
    
            if (inCheck) {
                new Thread(() -> {
                    Sound.play(currentPlayer == 0 ? SoundType.WHITE_CHECK : SoundType.BLACK_CHECK);
                    
                    if (chessboard.isCheckmate(currentPlayer)) {
                        try {
                            Thread.sleep(500); // Kurze Pause zwischen den Sounds
                            Sound.play(SoundType.CHECKMATE);
                            Thread.sleep(500);
                            Sound.play(SoundType.GAME_OVER);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).start();
            } else if (chessboard.isStalemate(currentPlayer)) {
                new Thread(() -> Sound.play(SoundType.GAME_OVER_STALEMATE)).start();
            }
        });
    }

    /**
     * Setzt die Variablen zur Markierung ungültiger Züge zurück.
     */
    private void resetInvalidState() {
        invalidTargetRow = -1;
        invalidTargetCol = -1;
        originalRow = -1;
        originalCol = -1;
        movesBlocked = false;
    }
    
    /**
     * Markiert einen ungültigen Zug, indem die Zielposition und Ursprungsposition gespeichert
     * und das Flag movesBlocked gesetzt wird.
     *
     * @param fromRow Startreihe
     * @param fromCol Startspalte
     * @param toRow   Zielreihe
     * @param toCol   Zielspalte
     */
    private void markInvalidMove(int fromRow, int fromCol, int toRow, int toCol) {
        invalidTargetRow = toRow;
        invalidTargetCol = toCol;
        originalRow = fromRow;
        originalCol = fromCol;
        movesBlocked = true;
        repaint();
    }
    
    /**
     * Überschreibt die paintComponent-Methode, um das Schachbrett, die Figuren,
     * Markierungen für ungültige Züge sowie grafische Pfeile anzuzeigen.
     *
     * @param g Graphics-Objekt
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Fülle den Hintergrund in Grautönen
        g2.setPaint(new Color(100, 100, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Zeichne das Schachbrett und die Figuren
        chessboard.draw(g2);

        // Falls ein ungültiger Zug markiert wurde, zeichne ein halbtransparentes rotes Feld
        if (invalidTargetRow != -1 && invalidTargetCol != -1) {
            int squareSize = Chessboard.SQUARE_SIZE;
            int targetX = MARGIN + invalidTargetCol * squareSize;
            int targetY = MARGIN + invalidTargetRow * squareSize;
            g2.setColor(new Color(255, 0, 0, 128));
            g2.fillRect(targetX, targetY, squareSize, squareSize);
        }

        // Zeichne einen blauen Pfeil an der ursprünglichen Position der Figur, falls vorhanden
        if (originalRow != -1 && originalCol != -1) {
            int squareSize = Chessboard.SQUARE_SIZE;
            int pieceCenterX = MARGIN + originalCol * squareSize + squareSize / 2;
            int pieceCenterY = MARGIN + originalRow * squareSize + squareSize / 2;

            g2.setColor(Color.BLUE);
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(3));

            int arrowStartY = pieceCenterY - 40;
            g2.drawLine(pieceCenterX, arrowStartY, pieceCenterX, pieceCenterY);

            Polygon arrowHead = new Polygon();
            arrowHead.addPoint(pieceCenterX, pieceCenterY);
            arrowHead.addPoint(pieceCenterX - 10, pieceCenterY - 20);
            arrowHead.addPoint(pieceCenterX + 10, pieceCenterY - 20);
            g2.fillPolygon(arrowHead);

            g2.setStroke(oldStroke);
        }
    }

    /**
     * Die run()-Methode des Runnable-Interfaces sorgt dafür, dass das Panel
     * etwa 60 Mal pro Sekunde (ca. 60 FPS) neu gezeichnet wird.
     */
    @Override
    public void run() {
        while (gameThread != null) {
            try {
                Thread.sleep(16); // ca. 60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            repaint();
        }
    }

    /**
     * Startet den Spiel-Thread.
     */
    public void startGame() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    /**
     * Stoppt den Spiel-Thread.
     */
    public void stopGame() {
        gameThread = null;
    }
}
