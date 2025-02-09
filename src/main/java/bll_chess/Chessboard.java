package main.java.bll_chess;

import java.awt.*;
import java.io.*;
import java.util.Stack;

import javax.swing.JOptionPane;

import main.java.bll_chess.piece.*;

public class Chessboard implements Serializable {
    public static final int MAX_COL = 8;
    public static final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100;
    
    private Piece[][] board;
    private int currentPlayer = 0;
    private boolean isCheck = false;
    private Stack<ChessMove> moveHistory = new Stack<>();
    private boolean promotionPending = false;

    public static class ChessMove implements Serializable {
        private Piece piece;
        private int fromRow, fromCol, toRow, toCol;
        private Piece capturedPiece;

        public ChessMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, Piece captured) {
            this.piece = piece;
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.capturedPiece = captured;
        }

        public Piece getPiece() { return piece; }
        public int getFromRow() { return fromRow; }
        public int getFromCol() { return fromCol; }
        public int getToRow() { return toRow; }
        public int getToCol() { return toCol; }
        public Piece getCapturedPiece() { return capturedPiece; }
    }

    public Chessboard() {
        initializeBoard();
    }

    private void initializeBoard() {
        board = new Piece[MAX_ROW][MAX_COL];
        
        // Weiße Figuren
        board[0][0] = new Rook(0, 0, 0);
        board[0][1] = new Knight(0, 1, 0);
        board[0][2] = new Bishop(0, 2, 0);
        board[0][3] = new Queen(0, 3, 0);
        board[0][4] = new King(0, 4, 0);
        board[0][5] = new Bishop(0, 5, 0);
        board[0][6] = new Knight(0, 6, 0);
        board[0][7] = new Rook(0, 7, 0);

        for (int col = 0; col < MAX_COL; col++) {
            board[1][col] = new Pawn(0, col, 1);
        }

        // Schwarze Figuren
        board[7][0] = new Rook(1, 0, 7);
        board[7][1] = new Knight(1, 1, 7);
        board[7][2] = new Bishop(1, 2, 7);
        board[7][3] = new Queen(1, 3, 7);
        board[7][4] = new King(1, 4, 7);
        board[7][5] = new Bishop(1, 5, 7);
        board[7][6] = new Knight(1, 6, 7);
        board[7][7] = new Rook(1, 7, 7);

        for (int col = 0; col < MAX_COL; col++) {
            board[6][col] = new Pawn(1, col, 6);
        }
    }
    
    /**
     * Führt einen Zug von (fromRow, fromCol) nach (toRow, toCol) aus.
     * Der Parameter switchTurn legt fest, ob nach dem Zug der Spielerwechsel erfolgen soll.
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, boolean switchTurn) {
        Piece movingPiece = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];
    
        // Alle Bauern verlieren ihre En-Passant-Berechtigung.
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COL; c++) {
                if (board[r][c] instanceof Pawn) {
                    ((Pawn) board[r][c]).setEnPassantEligible(false);
                }
            }
        }
    
        // En Passant
        if (movingPiece instanceof Pawn && Math.abs(toCol - fromCol) == 1 && board[toRow][toCol] == null) {
            int capturedPawnRow = fromRow; 
            captured = board[capturedPawnRow][toCol];
            board[capturedPawnRow][toCol] = null;
        }
    
        moveHistory.push(new ChessMove(movingPiece, fromRow, fromCol, toRow, toCol, captured));
    
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
    
        if (movingPiece != null) {
            movingPiece.setRow(toRow);
            movingPiece.setCol(toCol);
            
                // Prüfung auf Bauernumwandlung
         if (movingPiece instanceof Pawn) {
         handlePawnPromotion(movingPiece, toRow, toCol);
         }
       
            if (movingPiece instanceof Pawn && Math.abs(toRow - fromRow) == 2) {
                ((Pawn) movingPiece).setEnPassantEligible(true);
            }
    
            if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
                handleCastling(fromRow, fromCol, toRow, toCol);
                ((King) movingPiece).markAsMoved();
            } else if (movingPiece instanceof King) {
                ((King) movingPiece).markAsMoved();
            }
        }
    
        isCheck = isKingInCheck(currentPlayer);
        if (switchTurn) {
            switchPlayer();
        }
    }
    


    private void handlePawnPromotion(Piece pawn, int toRow, int toCol) {
      // Prüfen, ob die Umwandlungsreihe erreicht wurde:
      if ((pawn.getColor() == 0 && toRow == MAX_ROW - 1) || (pawn.getColor() == 1 && toRow == 0)) {
        // Promotion als aktiv markieren
        promotionPending = true;
        
        // Öffnet den Promotion-Dialog – dieser ist modal und blockiert den aktuellen Thread,
        // aber wir möchten auch in der Spiellogik keine weiteren Züge zulassen.
        String[] promotionOptions = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Wähle die Figur, in die der Bauer umgewandelt werden soll:",
            "Bauernumwandlung",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            promotionOptions,
            promotionOptions[0]
        );

        Piece promotedPiece;
        switch (choice) {
            case 0:
                promotedPiece = new Queen(pawn.getColor(), toCol, toRow);
                break;
            case 1:
                promotedPiece = new Rook(pawn.getColor(), toCol, toRow);
                break;
            case 2:
                promotedPiece = new Bishop(pawn.getColor(), toCol, toRow);
                break;
            case 3:
                promotedPiece = new Knight(pawn.getColor(), toCol, toRow);
                break;
            default:
                promotedPiece = new Queen(pawn.getColor(), toCol, toRow);
                break;
        }
        board[toRow][toCol] = promotedPiece;
        

        promotionPending = false;
    }
  }
   
    
    private void handleCastling(int fromRow, int fromCol, int toRow, int toCol) {
        int rookCol = (toCol > fromCol) ? 7 : 0;
        int newRookCol = (toCol > fromCol) ? toCol - 1 : toCol + 1;
        Rook rook = (Rook) board[toRow][rookCol];
        
        board[toRow][newRookCol] = rook;
        board[toRow][rookCol] = null;
        
        if (rook != null) {
            rook.setCol(newRookCol);
            rook.markAsMoved();
        }
    }

    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            ChessMove lastMove = moveHistory.pop();
            
            board[lastMove.getFromRow()][lastMove.getFromCol()] = lastMove.getPiece();
            board[lastMove.getToRow()][lastMove.getToCol()] = lastMove.getCapturedPiece();
            
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
        board[move.getToRow()][newRookCol] = null;
        board[move.getToRow()][rookCol] = rook;
        
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
    
    public boolean isKingInCheck(int player) {
        King king = null;
        // Finde den König des Spielers.
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
        // Überprüfe alle gegnerischen Figuren.
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
    
    /**
     * Simuliert einen Zug und prüft, ob der eigene König danach im Schach steht.
     */
    public boolean isMoveLeavingKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece movingPiece = board[fromRow][fromCol];
        if (movingPiece == null) return false;

        Piece captured = board[toRow][toCol];
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
        int originalRow = movingPiece.getRow();
        int originalCol = movingPiece.getCol();
        movingPiece.setRow(toRow);
        movingPiece.setCol(toCol);

        boolean inCheck = isKingInCheck(currentPlayer);

        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = captured;
        movingPiece.setRow(originalRow);
        movingPiece.setCol(originalCol);

        return inCheck;
    }
    
    /**
     * Prüft, ob der Spieler (bei Schach) keine legalen Züge hat.
     */
    public boolean isCheckmate(int player) {
        if (!isKingInCheck(player)) return false;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == player) {
                    for (int newRow = 0; newRow < MAX_ROW; newRow++) {
                        for (int newCol = 0; newCol < MAX_COL; newCol++) {
                            if (piece.isValidMove(newCol, newRow, board)) {
                                if (!isMoveLeavingKingInCheck(row, col, newRow, newCol)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public void draw(Graphics2D g2) {
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Color fieldColor = ((row + col) % 2 == 0) 
                    ? new Color(210, 165, 125) 
                    : new Color(175, 115, 70);
                g2.setColor(fieldColor);
                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    g2.drawImage(piece.getImage(), piece.getX(SQUARE_SIZE), piece.getY(SQUARE_SIZE),
                                 SQUARE_SIZE, SQUARE_SIZE, null);
                }
            }
        }
    }
}
