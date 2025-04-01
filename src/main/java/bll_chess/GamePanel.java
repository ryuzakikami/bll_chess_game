package main.java.bll_chess;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
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
 * Die Klasse GamePanel repraesentiert das Spiel-Panel, in dem das Schachbrett
 * gezeichnet und die Spielzuege animiert werden. Zusaetzlich werden hier Eingaben
 * verarbeitet, gueltige und unguelige Zuege markiert, Soundeffekte abgespielt und
 * die aktuelle Stellung als PNG exportiert. Neu: Es werden serielle Daten vom Arduino 
 * empfangen, die Zuege repraesentieren.
 */
public class GamePanel extends JPanel implements Runnable, KeyListener {
    // Thread fuer das Spiel, um kontinuierlich zu aktualisieren
    private Thread gameThread;
    public static final int SQUARE_SIZE = 100;  // Groesse eines Schachfeldes in Pixeln
    public static final int MARGIN = Chessboard.MARGIN;  // Abstand um das Brett
    public static final int WIDTH = Chessboard.MAX_COL * SQUARE_SIZE + 2 * MARGIN;  // Gesamtbreite des Panels
    public static final int HEIGHT = Chessboard.MAX_ROW * SQUARE_SIZE + 2 * MARGIN; // Gesamthoehe des Panels

    // Variablen zur Markierung von ungueligen Zuegen
    private int invalidTargetRow = -1;
    private int invalidTargetCol = -1;
    private int originalRow = -1;
    private int originalCol = -1;
    private boolean movesBlocked = false;
    
    // Das Schachbrett-Objekt, welches die Spiellogik enthaelt
    private Chessboard chessboard;
    // Zaehler fuer den PNG-Export
    private int exportCount = 1;
    
    // Hier wird der vorherige Sensorzustand gespeichert (64 Felder)
    private int[] previousSensorValues = new int[64];

    /**
     * Konstruktor des GamePanel.
     * Setzt die bevorzugte Groesse, initialisiert das Schachbrett, fuegt den KeyListener hinzu
     * und startet die serielle Verbindung zum Arduino.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        chessboard = new Chessboard();
        setFocusable(true);
        addKeyListener(this);
        
        // Initialisiere die Sensorwerte mit 0
        for (int i = 0; i < 64; i++) {
            previousSensorValues[i] = 0;
        }
        
        // Initialisiere den ArduinoConnector (Passe den Portnamen an deine Umgebung an, z.B. "COM6")
        new ArduinoConnector("COM6", this);
    }

    /**
     * Verarbeitet die vom Arduino empfangenen Daten.
     * Erwartet wird ein String im Format:
     * "START,<v0>,<v1>,...,<v63>,END"
     * 
     * Bei genau vier Werten wird ein Zug angenommen.
     *
     * @param data Die empfangenen Daten als String.
     */
    public void processSensorData(String data) {
        data = data.trim();
        
        // Validiere das Datenformat
        if (!data.startsWith("START,") || !data.endsWith(",END")) {
            System.out.println("Ungueltiges Datenformat: " + data);
            return;
        }
        
        // Entferne "START," und ",END"
        String inner = data.substring(6, data.length() - 4);
        String[] parts = inner.split(",");
        
        // Erwarte 4 Werte: FromRow, FromCol, ToRow, ToCol
        if (parts.length != 4) {
            System.out.println("Erwartet 4 Werte (FromRow,FromCol,ToRow,ToCol), erhalten: " + parts.length);
            return;
        }
        
        try {
            // Konvertiere die Werte in Integer
            int fromRow = Integer.parseInt(parts[0].trim());
            int fromCol = parts[1].trim().charAt(0) - 'A'; // Spalte A=0, B=1, etc.
            int toRow = Integer.parseInt(parts[2].trim());
            int toCol = parts[3].trim().charAt(0) - 'A';
            int internalFromRow = 8 - fromRow;
            int internalToRow = 8 - toRow;
            
            // Verarbeite den Zug
            processMove(internalFromRow, fromCol, internalToRow, toCol);
            
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            System.err.println("Fehler beim Parsen der Zugdaten: " + e.getMessage());
        }
    }

    /**
     * Verarbeitet einen Zug vom Feld (fromRow, fromCol) zum Feld (toRow, toCol).
     * Dabei werden diverse Pruefungen (z. B. Spielerzug, unguelige Zuege, Schach) durchgefuehrt,
     * Animationen gestartet und Soundeffekte abgespielt.
     *
     * @param fromRow Startreihe
     * @param fromCol Startspalte
     * @param toRow   Zielreihe
     * @param toCol   Zielspalte
     */
    public void processMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Falls Zuege momentan blockiert sind, abbrechen
        if (movesBlocked) {
            System.out.println("Zuege sind blockiert. Bitte korrigiere den ungueligen Zug zuerst.");
            return;
        }
        // Falls gerade eine Bauernumwandlung (Promotion) aussteht, warte auf Abschluss
        if (chessboard.isPromotionPending()) {
            System.out.println("Bitte warten Sie, bis die Umwandlung abgeschlossen ist.");
            return;
        }

        Piece movingPiece = chessboard.getBoard()[fromRow][fromCol];
        if (movingPiece == null) {
            System.out.println("processMove: Keine Figur an der Ausgangsposition gefunden!");
            resetInvalidState();
            repaint();
            return;
        }

        System.out.println("processMove: Verschiebe Figur mit Farbe = " + movingPiece.getColor());
        
        // Pruefe, ob die Figur zum aktuellen Spieler gehoert
        if (!chessboard.isValidTurn(movingPiece)) {
            System.err.println("Falscher Spieler am Zug!");
            return;
        }

        // Pruefe, ob der Zug ein En-Passant-Zug ist
        boolean enPassant = isEnPassantMove(movingPiece, toRow, toCol);
        Piece targetPiece = chessboard.getBoard()[toRow][toCol];
        // Falls es sich nicht um En Passant handelt, fuehre Standard-Pruefungen durch
        if (!enPassant) {
            if (movingPiece.isSameColor(targetPiece) ||
                !movingPiece.isValidMove(toCol, toRow, chessboard.getBoard())) {
                // Markiere und animiere einen ungueligen Zug
                markInvalidMove(fromRow, fromCol, toRow, toCol);
                animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
                System.out.println("Ungueltiger Zug.");
                return;
            }
        }
        
        // Pruefe, ob der Zug den Koenig im Schach belasst
        if (chessboard.isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
            markInvalidMove(fromRow, fromCol, toRow, toCol);
            System.out.println("Ungueltiger Zug: Zug laesst Koenig im Schach! Bitte Zug rueckgaengig machen.");
            animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
            return;
        }
        // Spiele den entsprechenden Sound: Schlag- oder Zug-Sound
        if (targetPiece != null || enPassant) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_TAKES : SoundType.BLACK_CAPTURES);
        } else {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_MOVE : SoundType.BLACK_MOVE);
        }

        // Zusatzausgabe: Sound bei Rochade
        if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_CASTLE : SoundType.BLACK_CASTLE);
        }
        // Setze die Markierung fuer unguelige Zuege zurueck
        resetInvalidState();
        // Starte die Zug-Animation
        animateMove(movingPiece, fromRow, fromCol, toRow, toCol, false);
    }

    /**
     * Prueft, ob der angegebene Zug ein En-Passant-Zug ist.
     * Hierzu wird geprueft, ob der bewegende Bauer diagonal zieht und
     * der benachbarte gegnerische Bauer fuer En Passant anfaellig ist.
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
        
        // Pruefe, ob diagonal gezogen wird (ein Feld)
        if (Math.abs(toCol - currentCol) == 1 && toRow == currentRow + pawn.getDirection()) {
            // Hole den benachbarten Gegnerbauer
            Piece opponentPiece = chessboard.getBoard()[currentRow][toCol];
            if (opponentPiece instanceof Pawn) {
                Pawn opponentPawn = (Pawn) opponentPiece;
                // En Passant ist moeglich, wenn der gegnerische Bauer gerade den Doppelschritt gemacht hat
                // und auf der gleichen Reihe steht
                if (opponentPawn.isEnPassantEligible() && opponentPawn.getRow() == currentRow) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Animiert den Zug einer Figur von der Start- zur Zielposition.
     * Dabei werden auch spezielle Animationen fuer Rochade beruecksichtigt.
     *
     * @param movingPiece Die bewegte Figur
     * @param fromRow     Startreihe
     * @param fromCol     Startspalte
     * @param toRow       Zielreihe
     * @param toCol       Zielspalte
     * @param revert      Flag, ob die Animation rueckgaengig gemacht werden soll (bei ungueligen Zuegen)
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

        int frames = 60;  // Anzahl der Animationsframes
        int delay = 15;   // Verzoegerung in Millisekunden zwischen den Frames
        final int[] frameCount = {0};

        // Pruefe, ob es sich um eine Rochade handelt
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

        // Timer fuer die Animations-Frames
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

            SwingUtilities.invokeLater(() -> repaint());

            if (frameCount[0] >= frames) {
                timer.stop();
                if (!revert) {
                    // Fuehre den Zug auf dem Schachbrett aus (und wechsle den Spieler)
                    chessboard.movePiece(fromRow, fromCol, toRow, toCol, true);
                    if (isCastling && castlingRook != null) {
                        // Bei Rochade auch den Turm bewegen
                        chessboard.movePiece(fromRow, rookFromCol, toRow, rookToCol, true);
                    }
                    checkGameStatus();
                }
                // Setze Animationen zurueck
                movingPiece.setAnimOffset(0, 0);
                if (isCastling && castlingRook != null) {
                    castlingRook.setAnimOffset(0, 0);
                }
                SwingUtilities.invokeLater(() -> repaint());
            }
        });
        timer.start();
    }

    /**
     * Prueft den Spielstatus (Schach, Schachmatt, Patt) und spielt ggf. entsprechende Soundeffekte.
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
                            Thread.sleep(500); // Kurze Pause zwischen den Soundeffekten
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
     * Setzt die Variablen zur Markierung ungueliger Zuege zurueck.
     */
    private void resetInvalidState() {
        invalidTargetRow = -1;
        invalidTargetCol = -1;
        originalRow = -1;
        originalCol = -1;
        movesBlocked = false;
    }
    
    /**
     * Markiert einen ungueligen Zug, indem die Zielposition und Ausgangsposition gespeichert
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
     * Ueberschreibt die paintComponent-Methode, um das Schachbrett, die Figuren,
     * Markierungen fuer unguelige Zuege sowie grafische Pfeile anzuzeigen.
     *
     * @param g Graphics-Objekt
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Fuelle den Hintergrund in Grautoenen
        g2.setPaint(new Color(100, 100, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Zeichne das Schachbrett und die Figuren
        chessboard.draw(g2);
        
        // Falls ein ungueliger Zug markiert wurde, zeichne ein halbtransparentes rotes Feld
        if (invalidTargetRow != -1 && invalidTargetCol != -1) {
            int squareSize = Chessboard.SQUARE_SIZE;
            int targetX = MARGIN + invalidTargetCol * squareSize;
            int targetY = MARGIN + invalidTargetRow * squareSize;
            g2.setColor(new Color(255, 0, 0, 128));
            g2.fillRect(targetX, targetY, squareSize, squareSize);
        }
        
        // Zeichne einen blauen Pfeil an der urspruenglichen Position der Figur, falls vorhanden
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
     * Exportiert das aktuelle Schachbrett als PNG-Bild.
     * Dabei wird das Panel in ein BufferedImage gerendert und anschliessend als PNG gespeichert.
     *
     * @param filePath Pfad inklusive Dateinamen, unter dem das Bild gespeichert werden soll.
     */
    public void exportToPNG(String filePath) {
        int width = getWidth();
        int height = getHeight();
        
        // Erstelle ein BufferedImage und rendere das Panel darauf
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        this.paint(g2d); // Das Panel wird auf das Bild gemalt
        g2d.dispose();
        
        // Speichere das Bild als PNG
        try {
            File file = new File(filePath);
            ImageIO.write(image, "png", file);
            System.out.println("Schachbrett erfolgreich exportiert: " + filePath);
        } catch (IOException e) {
            System.err.println("Fehler beim Exportieren der PNG: " + e.getMessage());
        }
    }
    
    /**
     * KeyListener-Methode: Wird aufgerufen, wenn eine Taste gedrueckt wird.
     * Exportiert das Schachbrett als PNG, wenn die Taste "S" gedrueckt wird.
     *
     * @param e Das KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_S) { // Wenn "S" gedrueckt wird
            // Exportiere das Schachbrett-Bild mit einem fortlaufenden Namen
            exportToPNG("./Desktop/schachpngs/schachbrett_" + exportCount + ".png");
            exportCount++;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) { }
    
    @Override
    public void keyTyped(KeyEvent e) { }
    
    /**
     * Die run()-Methode des Runnable-Interfaces sorgt dafuer, dass das Panel
     * etwa 60 Mal pro Sekunde (ca. 60 FPS) neu gezeichnet wird.
     */
    @Override
    public void run() {
        while (gameThread != null) {
            try {
                Thread.sleep(16); 
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
