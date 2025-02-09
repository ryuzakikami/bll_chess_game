package main.java.bll_chess.piece;

public class King extends Piece {
    private boolean hasMoved = false;

    public King(int color, int col, int row) {
        super(color, col, row);
    }
    
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        
        // Normaler Zug: ein Feld in jede Richtung
        if (dCol <= 1 && dRow <= 1) {
            Piece target = board[newRow][newCol];
            return (target == null || target.getColor() != this.color);
        }
        
        // Rochade: Zug um zwei Felder horizontal (dRow == 0 && dCol == 2)
        if (dRow == 0 && dCol == 2) {
            if (hasMoved) return false;
            
            int rookCol = (newCol > col) ? 7 : 0;
            Piece rookPiece = board[row][rookCol];
            if (!(rookPiece instanceof Rook)) return false;
            Rook rook = (Rook) rookPiece;
            if (rook.hasMoved()) return false;
            
            // Prüfe, ob alle Felder zwischen König und Turm frei sind
            int step = (rookCol == 7) ? 1 : -1;
            for (int c = col + step; c != rookCol; c += step) {
                if (board[row][c] != null) return false;
            }
            
            // Zusätzliche Prüfungen (z.B. ob der König über bedrohte Felder zieht) können ergänzt werden.
            return true;
        }
        
        return false;
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteKing" : "blackKing";
    }
    
    public void markAsMoved() {
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    // Überprüft, ob der König bedroht wird (Schach)
    public boolean isInCheck(Piece[][] board) {
        for (Piece[] boardRow : board) {
            for (Piece piece : boardRow) {
                if (piece != null && piece.getColor() != this.color &&
                    piece.isValidMove(this.col, this.row, board)) {
                    return true;
                }
            }
        }
        return false;
    }
}
