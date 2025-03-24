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

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    public static final int SQUARE_SIZE = 100;
    public static final int MARGIN = Chessboard.MARGIN;
    public static final int WIDTH = Chessboard.MAX_COL * SQUARE_SIZE + 2 * MARGIN;
    public static final int HEIGHT = Chessboard.MAX_ROW * SQUARE_SIZE + 2 * MARGIN;

    // Felder zur Fehlererkennung und Blockierung
    private int invalidTargetRow = -1;
    private int invalidTargetCol = -1;
    private int originalRow = -1;
    private int originalCol = -1;
    private boolean movesBlocked = false;
    
    private Chessboard chessboard;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        chessboard = new Chessboard();
    }

    /**
     * Prüft, ob der angegebene Zug ein en-Passant-Zug ist.
     * Wir nutzen movingPiece.getRow()/getCol(), damit die Logik exakt
     * mit Pawn.isValidMove(...) übereinstimmt.
     */
    private boolean isEnPassantMove(Piece movingPiece, int toRow, int toCol) {
        if (!(movingPiece instanceof Pawn)) {
            return false;
        }
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

    public void processMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (movesBlocked) {
            System.out.println("Moves are blocked. Please correct the invalid move first.");
            return;
        }
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
                animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
                System.out.println("Invalid move.");
                return;
            }
        }
        
        if (chessboard.isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
            markInvalidMove(fromRow, fromCol, toRow, toCol);
            System.out.println("Illegal move: Move leaves king in check! Please reverse the move.");
            animateMove(movingPiece, fromRow, fromCol, toRow, toCol, true);
            return;
        }
        if (targetPiece != null || enPassant) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_TAKES : SoundType.BLACK_CAPTURES);
        } else {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_MOVE : SoundType.BLACK_MOVE);
        }

        if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
            Sound.play(movingPiece.getColor() == 0 ? SoundType.WHITE_CASTLE : SoundType.BLACK_CASTLE);
        }
        resetInvalidState();
        animateMove(movingPiece, fromRow, fromCol, toRow, toCol, false);
    }


    private void animateMove(Piece movingPiece, int fromRow, int fromCol,
    int toRow, int toCol, boolean revert) {
int squareSize = Chessboard.SQUARE_SIZE;
int startX = fromCol * squareSize;
int startY = fromRow * squareSize;
int targetX = toCol * squareSize;
int targetY = toRow * squareSize;
int deltaX = targetX - startX;
int deltaY = targetY - startY;

int frames = 30;
int delay = 15;
final int[] frameCount = {0};

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

SwingUtilities.invokeLater(this::repaint);

if (frameCount[0] >= frames) {
timer.stop();
if (!revert) {
// Move the king and the rook on the board
chessboard.movePiece(fromRow, fromCol, toRow, toCol, true);
if (isCastling && castlingRook != null) {
// Move the rook as well
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
                    Sound.play(currentPlayer == 0 ? SoundType.WHITE_CHECK : SoundType.BLACK_CHECK);
                    
                    if (chessboard.isCheckmate(currentPlayer)) {
                        try {
                            Thread.sleep(500); // Pause zwischen den Sounds
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
    }
    
    private void markInvalidMove(int fromRow, int fromCol, int toRow, int toCol) {
        invalidTargetRow = toRow;
        invalidTargetCol = toCol;
        originalRow = fromRow;
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
