package main.java.bll_chess;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import main.java.bll_chess.piece.*;

public class Chessboard implements Serializable {
    public static final int MAX_COL = 8;
    public static final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100;
    public static final int MARGIN = 40;
    
    private Piece[][] board;
    private int currentPlayer = 0;
    private boolean isCheck = false;
    private Stack<ChessMove> moveHistory = new Stack<>();
    private boolean promotionPending = false;
    
    // Für Zobrist-Hashing:
    // Dimensionen: [Farbe 0/1][Piece-Typ (0:Pawn,1:Knight,2:Bishop,3:Rook,4:Queen,5:King)][Feld (0-63)]
    private long[][][] zobristTable;
    private long currentZobristHash = 0;
    // Für Wiederholungsprüfung:
    private ArrayList<Long> positionHistory = new ArrayList<>();
    // Halbzugzähler für 50-Züge-Regel (bei Pawn-Move oder Capture wird er zurückgesetzt)
    private int halfMoveClock = 0;

    public Chessboard() {
        initializeBoard();
        initializeZobrist();
        // Speichere die Ausgangsstellung
        positionHistory.add(currentZobristHash);
    }
    
    private void initializeBoard() {
        board = new Piece[MAX_ROW][MAX_COL];
        placePieces(7, 6, 0, 1);
        // Setze den Halbzugzähler zurück
        halfMoveClock = 0;
    }
    
    private void placePieces(int whiteRow, int whitePawnRow, int blackRow, int blackPawnRow) {
        // Weiße Figuren
        board[whiteRow][0] = new Rook(0, 0, whiteRow);
        board[whiteRow][1] = new Knight(0, 1, whiteRow);
        board[whiteRow][2] = new Bishop(0, 2, whiteRow);
        board[whiteRow][3] = new Queen(0, 3, whiteRow);
        board[whiteRow][4] = new King(0, 4, whiteRow);
        board[whiteRow][5] = new Bishop(0, 5, whiteRow);
        board[whiteRow][6] = new Knight(0, 6, whiteRow);
        board[whiteRow][7] = new Rook(0, 7, whiteRow);
        for (int col = 0; col < MAX_COL; col++) {
            board[whitePawnRow][col] = new Pawn(0, col, whitePawnRow);
        }
        
        // Schwarze Figuren
        board[blackRow][0] = new Rook(1, 0, blackRow);
        board[blackRow][1] = new Knight(1, 1, blackRow);
        board[blackRow][2] = new Bishop(1, 2, blackRow);
        board[blackRow][3] = new Queen(1, 3, blackRow);
        board[blackRow][4] = new King(1, 4, blackRow);
        board[blackRow][5] = new Bishop(1, 5, blackRow);
        board[blackRow][6] = new Knight(1, 6, blackRow);
        board[blackRow][7] = new Rook(1, 7, blackRow);
        for (int col = 0; col < MAX_COL; col++) {
            board[blackPawnRow][col] = new Pawn(1, col, blackPawnRow);
        }
    }
    
    // Initialisiert die Zobrist-Tabelle mit zufälligen Long-Werten.
    private void initializeZobrist() {
        Random rand = new Random();
        zobristTable = new long[2][6][MAX_ROW * MAX_COL];
        for (int color = 0; color < 2; color++) {
            for (int type = 0; type < 6; type++) {
                for (int pos = 0; pos < MAX_ROW * MAX_COL; pos++) {
                    zobristTable[color][type][pos] = rand.nextLong();
                }
            }
        }
        // Berechne den Hash der Anfangsstellung
        currentZobristHash = 0;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    int type = getPieceType(piece);
                    int pos = row * MAX_COL + col;
                    currentZobristHash ^= zobristTable[piece.getColor()][type][pos];
                }
            }
        }
    }
    
    // Liefert den Typ des Pieces (0: Pawn, 1: Knight, 2: Bishop, 3: Rook, 4: Queen, 5: King)
    private int getPieceType(Piece piece) {
        if (piece instanceof Pawn) return 0;
        if (piece instanceof Knight) return 1;
        if (piece instanceof Bishop) return 2;
        if (piece instanceof Rook) return 3;
        if (piece instanceof Queen) return 4;
        if (piece instanceof King) return 5;
        return -1;
    }
    
    // Aktualisiert den Zobrist-Hash für ein Piece, das von einer Position verschwindet oder erscheint.
    private void updateZobrist(Piece piece, int row, int col) {
        if (piece == null) return;
        int type = getPieceType(piece);
        int pos = row * MAX_COL + col;
        currentZobristHash ^= zobristTable[piece.getColor()][type][pos];
    }
    
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, boolean switchTurn) {
        Piece movingPiece = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];
    
        // Setze En Passant für alle Bauern zurück
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COL; c++) {
                if (board[r][c] instanceof Pawn) {
                    ((Pawn) board[r][c]).setEnPassantEligible(false);
                }
            }
        }
    
        // En Passant: Falls diagonal bewegt, aber Zielfeld leer
        if (movingPiece instanceof Pawn && Math.abs(toCol - fromCol) == 1 && board[toRow][toCol] == null) {
            int capturedPawnRow = fromRow; 
            captured = board[capturedPawnRow][toCol];
            // Aktualisiere Hash: Entferne den gefangenen Bauern
            updateZobrist(captured, capturedPawnRow, toCol);
            board[capturedPawnRow][toCol] = null;
        }
    
        // Aktualisiere Zobrist: Entferne das Piece von der alten Position
        updateZobrist(movingPiece, fromRow, fromCol);
        if (captured != null) {
            updateZobrist(captured, toRow, toCol);
        }
    
        moveHistory.push(new ChessMove(movingPiece, fromRow, fromCol, toRow, toCol, captured));
    
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
    
        // Aktualisiere Zobrist: Füge das Piece an der neuen Position hinzu
        updateZobrist(movingPiece, toRow, toCol);
    
        if (movingPiece != null) {
            movingPiece.setRow(toRow);
            movingPiece.setCol(toCol);
            
            // Bauernumwandlung prüfen
            if (movingPiece instanceof Pawn) {
                handlePawnPromotion(movingPiece, toRow, toCol);
            }
       
            // Wenn der Bauer zwei Felder vorrückt, setze En Passant
            if (movingPiece instanceof Pawn && Math.abs(toRow - fromRow) == 2) {
                ((Pawn) movingPiece).setEnPassantEligible(true);
            }
    
            // Rochade
            if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
                handleCastling(fromRow, fromCol, toRow, toCol);
                ((King) movingPiece).markAsMoved();
            } else if (movingPiece instanceof King) {
                ((King) movingPiece).markAsMoved();
            }
        }
        
        // Aktualisiere Halbzugzähler:
        if (movingPiece instanceof Pawn || captured != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // Speichere die aktuelle Stellungshash für Wiederholungsprüfung
        positionHistory.add(currentZobristHash);
    
        isCheck = isKingInCheck(currentPlayer);
        if (switchTurn) {
            switchPlayer();
        }
    }
    
    private void handlePawnPromotion(Piece pawn, int toRow, int toCol) {
        if ((pawn.getColor() == 0 && toRow == 0) || (pawn.getColor() == 1 && toRow == MAX_ROW - 1)) {
            promotionPending = true;
            board[toRow][toCol] = null; 
            String colorPrefix = (pawn.getColor() == 0) ? "white" : "black";
            ImageIcon queenIcon  = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Queen.png");
            ImageIcon rookIcon   = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Rook.png");
            ImageIcon bishopIcon = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Bishop.png");
            ImageIcon knightIcon = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Knight.png");
            Object[] promotionOptions = { queenIcon, rookIcon, bishopIcon, knightIcon};
        
            int choice = JOptionPane.showOptionDialog(
                null,
                "Wähle die Figur, in die der Bauer umgewandelt werden soll:",
                "Beförderung",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                promotionOptions,
                promotionOptions[0]
            );
        
            Piece promotedPiece;
            switch (choice) {
                case 0: promotedPiece = new Queen(pawn.getColor(), toCol, toRow); break;
                case 1: promotedPiece = new Rook(pawn.getColor(), toCol, toRow); break;
                case 2: promotedPiece = new Bishop(pawn.getColor(), toCol, toRow); break;
                case 3: promotedPiece = new Knight(pawn.getColor(), toCol, toRow); break;
                default: promotedPiece = new Queen(pawn.getColor(), toCol, toRow); break;
            }
            board[toRow][toCol] = promotedPiece;
            // Aktualisiere Zobrist: Füge das beförderte Piece hinzu
            updateZobrist(promotedPiece, toRow, toCol);
            promotionPending = false;
        }
    }
    
    private void handleCastling(int fromRow, int fromCol, int toRow, int toCol) {
        // Castling should be done only for kings
        if (!(board[fromRow][fromCol] instanceof King)) {
            return;
        }
        
        King king = (King) board[fromRow][fromCol];
        Rook rook = null;
        int rookCol = (toCol > fromCol) ? 7 : 0; // Determine whether it's kingside or queenside
    
        // Castling is only allowed if both the king and the rook haven't moved
        if (king.hasMoved() || (rook = (Rook) board[toRow][rookCol]) == null || rook.hasMoved()) {
            return;
        }
    
        // Check that there are no pieces between the king and the rook
        int step = (toCol > fromCol) ? 1 : -1;
        for (int col = fromCol + step; col != toCol; col += step) {
            if (board[fromRow][col] != null) {
                return; // There's a piece between the king and rook
            }
        }
    
        // Ensure the king is not in check, and the squares the king moves across aren't under attack
        if (isKingInCheck(currentPlayer) || isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
            return;
        }
    
        // Move the king
        board[toRow][toCol] = king;
        board[fromRow][fromCol] = null;
        king.setCol(toCol);
        king.markAsMoved();
    
        // Move the rook
        board[toRow][toCol - step] = rook;
        board[fromRow][rookCol] = null;
        rook.setCol(toCol - step);
        rook.markAsMoved();
    
        // Update the Zobrist hash for both moves
        updateZobrist(king, toRow, toCol);
        updateZobrist(rook, toRow, toCol - step);
    }
    
    
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            ChessMove lastMove = moveHistory.pop();
            // Aktualisiere Hash: Entferne Piece von der Zielposition
            updateZobrist(lastMove.getPiece(), lastMove.getToRow(), lastMove.getToCol());
            board[lastMove.getFromRow()][lastMove.getFromCol()] = lastMove.getPiece();
            board[lastMove.getToRow()][lastMove.getToCol()] = lastMove.getCapturedPiece();
            // Aktualisiere Hash: Füge Piece an der alten Position hinzu
            updateZobrist(lastMove.getPiece(), lastMove.getFromRow(), lastMove.getFromCol());
            if (lastMove.getCapturedPiece() != null) {
                updateZobrist(lastMove.getCapturedPiece(), lastMove.getToRow(), lastMove.getToCol());
            }
            lastMove.getPiece().setRow(lastMove.getFromRow());
            lastMove.getPiece().setCol(lastMove.getFromCol());
            
            if (lastMove.getPiece() instanceof King && Math.abs(lastMove.getToCol() - lastMove.getFromCol()) == 2) {
                resetRookPosition(lastMove);
            }
            
            switchPlayer();
        }
    }
    
    private void resetRookPosition(ChessMove move) {
        int rookCol = (move.getToCol() > move.getFromCol()) ? 7 : 0;
        int newRookCol = (move.getToCol() > move.getFromCol()) ? move.getToCol() - 1 : move.getToCol() + 1;
        
        Rook rook = (Rook) board[move.getToRow()][newRookCol];
        // Aktualisiere Hash: Entferne den Turm von der aktuellen Position
        updateZobrist(rook, move.getToRow(), newRookCol);
        board[move.getToRow()][newRookCol] = null;
        board[move.getToRow()][rookCol] = rook;
        // Aktualisiere Hash: Füge den Turm an der alten Position hinzu
        updateZobrist(rook, move.getToRow(), rookCol);
        
        if (rook != null) {
            rook.setCol(rookCol);
        }
    }
    
    public Piece[][] getBoard() {
        return board;
    }
    
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    public boolean isPromotionPending() {
        return promotionPending;
    }
    
    public void switchPlayer() {
        currentPlayer = (currentPlayer == 0) ? 1 : 0;
    }
    
    public boolean isValidTurn(Piece piece) {
        if (piece == null) {
            System.out.println("isValidTurn: Piece is null");
            return false;
        }
        System.out.println("isValidTurn: Piece color = " + piece.getColor() + ", currentPlayer = " + currentPlayer);
        if (piece.getColor() != currentPlayer) {
            System.out.println("isValidTurn: Falscher Spieler am Zug!");
            return false;
        }
        return true;
    }
    
    /**
     * Prüft, ob der König des Spielers im Schach steht.
     * Optimierungsidee: Statt alle Gegnerfiguren zu prüfen, kann man gezielt über relevante Richtungen prüfen.
     */
    public boolean isKingInCheck(int player) {
        King king = null;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece instanceof King && piece.getColor() == player) {
                    king = (King) piece;
                    break;
                }
            }
            if (king != null) break;
        }
        if (king == null) return false;
    
        int opponentColor = 1 - player;
        // Standard-Variante: Prüfe alle gegnerischen Figuren.
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == opponentColor) {
                    if (piece.isValidMove(king.getCol(), king.getRow(), board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isCheckmate(int player) {
        if (!isKingInCheck(player)) {
            return false;
        }
    
        // Loop through all pieces of the player
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == player) {
                    // Check for valid moves for each piece
                    for (int newRow = 0; newRow < MAX_ROW; newRow++) {
                        for (int newCol = 0; newCol < MAX_COL; newCol++) {
                            if (piece.isValidMove(newCol, newRow, board)) {
                                if (!isMoveLeavingKingInCheck(row, col, newRow, newCol)) {
                                    return false; // There's at least one valid move
                                }
                            }
                        }
                    }
                }
            }
        }
        return true; // No valid moves left
    }
    
    public boolean isStalemate(int player) {
        if (isKingInCheck(player)) {
            return false; // If the king is in check, it's not stalemate
        }
    
        // Loop through all pieces of the player
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == player) {
                    // Check for valid moves for each piece
                    for (int newRow = 0; newRow < MAX_ROW; newRow++) {
                        for (int newCol = 0; newCol < MAX_COL; newCol++) {
                            if (piece.isValidMove(newCol, newRow, board)) {
                                if (!isMoveLeavingKingInCheck(row, col, newRow, newCol)) {
                                    return false; // There's at least one valid move
                                }
                            }
                        }
                    }
                }
            }
        }
        return true; // No valid moves left, no check, it's stalemate
    }
    
    
    /**
     * Prüft, ob die Stellung dreifach wiederholt wurde.
     */
    public boolean isThreefoldRepetition() {
        int count = 0;
        for (Long hash : positionHistory) {
            if (hash == currentZobristHash) {
                count++;
            }
        }
        return count >= 3;
    }
    
    /**
     * Prüft die 50-Züge-Regel.
     */
    public boolean isFiftyMoveRule() {
        return halfMoveClock >= 100;
    }
    
    boolean isMoveLeavingKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece tempPiece = board[toRow][toCol];
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;
    
        boolean isInCheck = isKingInCheck(currentPlayer);
    
        // Restore the board state
        board[fromRow][fromCol] = board[toRow][toCol];
        board[toRow][toCol] = tempPiece;
    
        return isInCheck; // Return true if the king is in check after the move
    }
    
    
    public void draw(Graphics2D g2) {
        int margin = MARGIN;
        int boardSize = MAX_COL * SQUARE_SIZE;
    
        // Zeichne die Schachbrettfelder
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Color fieldColor = ((row + col) % 2 == 0)
                        ? new Color(210, 165, 125)
                        : new Color(175, 115, 70);
                int x = margin + col * SQUARE_SIZE;
                int y = margin + row * SQUARE_SIZE;
                g2.setColor(fieldColor);
                g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    
        // Zeichne die Figuren – getX/getY beinhalten bereits den Animationsoffset.
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    int x = margin + piece.getX(SQUARE_SIZE);
                    int y = margin + piece.getY(SQUARE_SIZE);
                    g2.drawImage(piece.getImage(), x, y, SQUARE_SIZE, SQUARE_SIZE, null);
                }
            }
        }
    
        // Zeichne einen Rahmen um das Brett
        g2.setColor(Color.BLACK);
        g2.drawRect(margin, margin, boardSize, boardSize);
    
        // Beschriftung: Spalten (a-h) und Reihen (1-8)
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        for (int col = 0; col < MAX_COL; col++) {
            String label = String.valueOf((char) ('a' + col));
            int x = margin + col * SQUARE_SIZE + SQUARE_SIZE / 2 - 5;
            int yBottom = margin + boardSize + 20;
            int yTop = margin - 10;
            g2.drawString(label, x, yBottom);
            g2.drawString(label, x, yTop);
        }
        for (int row = 0; row < MAX_ROW; row++) {
            String label = String.valueOf(MAX_ROW - row);
            int y = margin + row * SQUARE_SIZE + SQUARE_SIZE / 2 + 5;
            int xRight = margin + boardSize + 5;
            int xLeft = margin - 20;
            g2.drawString(label, xRight, y);
            g2.drawString(label, xLeft, y);
        }
    }
}
