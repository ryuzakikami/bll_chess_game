package main.java.bll_chess;
import main.java.bll_chess.piece.*;

public class ChessMove {
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
    
    // Getter-Methoden
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
    public int getFromCol() {
        return fromCol;
    }
    public int getFromRow() {
        return fromRow;
    }
    public Piece getPiece() {
        return piece;
    }
    public int getToCol() {
        return toCol;
    }
    public int getToRow() {
        return toRow;
    }
}
