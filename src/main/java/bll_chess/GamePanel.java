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
import main.java.bll_chess.ArduinoConnector;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private Thread gameThread;
    public static final int SQUARE_SIZE = 100;
    public static final int MARGIN = Chessboard.MARGIN;
    public static final int WIDTH = Chessboard.MAX_COL * SQUARE_SIZE + 2 * MARGIN;
    public static final int HEIGHT = Chessboard.MAX_ROW * SQUARE_SIZE + 2 * MARGIN;

    private int invalidTargetRow = -1;
    private int invalidTargetCol = -1;
    private int originalRow = -1;
    private int originalCol = -1;
    private boolean movesBlocked = false;
    private boolean sensorFirstMove = true;


    private Chessboard chessboard;
    private int exportCount = 1;
    private int[] previousSensorValues = new int[64];

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        chessboard = new Chessboard();
        setFocusable(true);
        addKeyListener(this);

        for (int i = 0; i < 64; i++) {
            previousSensorValues[i] = 0;
        }

        // ArduinoConnector starten
        new ArduinoConnector(this);
    }
    public void processSensorData(String data) {
    data = data.trim();
    System.out.println("processSensorData aufgerufen mit: <" + data + ">");
    if (!data.startsWith("START,") || !data.endsWith(",END")) {
        System.out.println("Ungueltiges Datenformat: " + data);
        return;
    }

    String inner = data.substring(6, data.length() - 4);
    String[] parts = inner.split(",");
    if (parts.length != 4) {
        System.out.println("Erwartet 4 Werte (FromRow,FromCol,ToRow,ToCol), erhalten: " + parts.length);
        return;
    }
  
    try {
        int fromRow = Integer.parseInt(parts[0].trim());
        int fromCol = parts[1].trim().charAt(0) - 'A';
        int toRow   = Integer.parseInt(parts[2].trim());
        int toCol   = parts[3].trim().charAt(0) - 'A';
        int internalFromRow = fromRow;
        int internalToRow   = toRow;
        System.out.println("DEBUG in processSensorData -> fromRow=" + internalFromRow +
                           ", fromCol=" + fromCol + ", toRow=" + internalToRow + ", toCol=" + toCol);

        // Wenn es der allererste Sensor-Move ist, stelle currentPlayer=0
        if (sensorFirstMove) {
            chessboard.setCurrentPlayer(1);
            sensorFirstMove = false;
        }

        processMove(internalFromRow, fromCol, internalToRow, toCol);
        // Nach processMove neu zeichnen:
        SwingUtilities.invokeLater(this::repaint);

    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
        System.err.println("Fehler beim Parsen der Zugdaten: " + e.getMessage());
    }
}


    public void processMove(int fromRow, int fromCol, int toRow, int toCol) {
        System.out.println("DEBUG in processMove: fromRow=" + fromRow + 
                           ", fromCol=" + fromCol + ", toRow=" + toRow + ", toCol=" + toCol);
        if (movesBlocked) {
            System.out.println("Zuege sind blockiert. Bitte korrigiere den ungueligen Zug zuerst.");
            return;
        }
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
        if (!chessboard.isValidTurn(movingPiece)) {
            System.err.println("Falscher Spieler am Zug!");
            return;
        }

        boolean enPassant = isEnPassantMove(movingPiece, toRow, toCol);
        Piece targetPiece = chessboard.getBoard()[toRow][toCol];
        if (!enPassant) {
            if (movingPiece.isSameColor(targetPiece) ||
                !movingPiece.isValidMove(toCol, toRow, chessboard.getBoard())) {
                markInvalidMove(fromRow, fromCol, toRow, toCol);
                animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true, false);
                System.out.println("Ungueltiger Zug.");
                return;
            }
        }

        if (chessboard.isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
            markInvalidMove(fromRow, fromCol, toRow, toCol);
            System.out.println("Ungueltiger Zug: Zug laesst Koenig im Schach! Bitte Zug rueckgaengig machen.");
            animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true, false);
            return;
        }

        if (targetPiece != null || enPassant) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_TAKES : SoundType.BLACK_CAPTURES);
        } else {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_MOVE : SoundType.BLACK_MOVE);
        }

        boolean isCastling = (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2);
        int rookFromCol = -1, rookToCol = -1;
        if (isCastling) {
            rookFromCol = (toCol > fromCol) ? 7 : 0;
            rookToCol   = (toCol > fromCol) ? toCol - 1 : toCol + 1;
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_CASTLE : SoundType.BLACK_CASTLE);
        }

        if (isCastling) {
            chessboard.movePiece(fromRow, fromCol, toRow, toCol, false);
            chessboard.movePiece(fromRow, rookFromCol, toRow, rookToCol, true);
        } else {
            chessboard.movePiece(fromRow, fromCol, toRow, toCol, true);
        }

        resetInvalidState();
        animateMove(movingPiece, fromRow, fromCol, toRow, toCol, false, true);
    }

    private boolean isEnPassantMove(Piece movingPiece, int toRow, int toCol) {
        if (!(movingPiece instanceof Pawn)) return false;
        Pawn pawn = (Pawn) movingPiece;
        int currentRow = pawn.getRow();
        int currentCol = pawn.getCol();
        if (Math.abs(toCol - currentCol) == 1 && toRow == currentRow + pawn.getDirection()) {
            Piece opponentPiece = chessboard.getBoard()[currentRow][toCol];
            if (opponentPiece instanceof Pawn) {
                Pawn opponentPawn = (Pawn) opponentPiece;
                if (opponentPawn.isEnPassantEligible() && opponentPawn.getRow() == currentRow) {
                    return true;
                }
            }
        }
        return false;
    }

    private void animateMove(Piece movingPiece, int fromRow, int fromCol,
                             int toRow, int toCol, boolean revert, boolean skipMove) {
        int squareSize = Chessboard.SQUARE_SIZE;
        int startX = fromCol * squareSize;
        int startY = fromRow * squareSize;
        int targetX = toCol * squareSize;
        int targetY = toRow * squareSize;
        int deltaX = targetX - startX;
        int deltaY = targetY - startY;

        int frames = 60;
        int delay = 15;
        final int[] frameCount = {0};

        final boolean isCastling = (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2);
        final Rook castlingRook;
        final int rookFromCol, rookToCol;
        if (isCastling) {
            rookFromCol = (toCol > fromCol) ? 7 : 0;
            rookToCol   = (toCol > fromCol) ? toCol - 1 : toCol + 1;
            castlingRook = (Rook) chessboard.getBoard()[fromRow][rookFromCol];
        } else {
            rookFromCol = -1;
            rookToCol   = -1;
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

            SwingUtilities.invokeLater(this::repaint);

            if (frameCount[0] >= frames) {
                timer.stop();
                if (!revert && !skipMove) {
                    chessboard.movePiece(fromRow, fromCol, toRow, toCol, true);
                    if (isCastling && castlingRook != null) {
                        chessboard.movePiece(fromRow, rookFromCol, toRow, rookToCol, true);
                    }
                    checkGameStatus();
                }
                movingPiece.setAnimOffset(0, 0);
                if (isCastling && castlingRook != null) {
                    castlingRook.setAnimOffset(0, 0);
                }
                SwingUtilities.invokeLater(this::repaint);
            }
        });
        timer.start();
    }

    private void checkGameStatus() {
        SwingUtilities.invokeLater(() -> {
            int currentPlayer = chessboard.getCurrentPlayer();
            boolean inCheck = chessboard.isKingInCheck(currentPlayer);

            if (inCheck) {
                new Thread(() -> {
                    Sound.play(currentPlayer == 1 ? SoundType.WHITE_CHECK : SoundType.BLACK_CHECK);
                    if (chessboard.isCheckmate(currentPlayer)) {
                        try {
                            Thread.sleep(500);
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

    private void resetInvalidState() {
        invalidTargetRow = -1;
        invalidTargetCol = -1;
        originalRow = -1;
        originalCol = -1;
        movesBlocked = false;
        SwingUtilities.invokeLater(this::repaint);
    }

    private void markInvalidMove(int fromRow, int fromCol, int toRow, int toCol) {
        invalidTargetRow = 7-toRow;
        invalidTargetCol = toCol;
        originalRow = 7-fromRow;
        originalCol = fromCol;
        movesBlocked = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(new Color(100, 100, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());

        chessboard.draw(g2);

        if (invalidTargetRow != -1 && invalidTargetCol != -1) {
            int squareSize = Chessboard.SQUARE_SIZE;
            int targetX = MARGIN + invalidTargetCol * squareSize;
            int targetY = MARGIN + invalidTargetRow * squareSize;
            g2.setColor(new Color(255, 0, 0, 128));
            g2.fillRect(targetX, targetY, squareSize, squareSize);
        }

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

    public void exportToPNG(String filePath) {
        int width = getWidth();
        int height = getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        this.paint(g2d);
        g2d.dispose();
        try {
            File file = new File(filePath);
            ImageIO.write(image, "png", file);
            System.out.println("Schachbrett erfolgreich exportiert: " + filePath);
        } catch (IOException e) {
            System.err.println("Fehler beim Exportieren der PNG: " + e.getMessage());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_S) {
            exportToPNG("./Desktop/schachpngs/schachbrett_" + exportCount + ".png");
            exportCount++;
        }
    }
    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }

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
