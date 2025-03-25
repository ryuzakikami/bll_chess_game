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
        
        // Normale Bewegung: ein Feld in jede Richtung
        if (dCol <= 1 && dRow <= 1) {
            Piece target = board[newRow][newCol];
            return (target == null || target.getColor() != this.color);
        }
        
        // Rochade: Zwei Felder horizontal (dRow == 0 und dCol == 2)
        if (dRow == 0 && dCol == 2) {
            if (hasMoved) return false; // Der König darf sich noch nicht bewegt haben
            
            int rookCol = (newCol > col) ? 7 : 0; // Bestimme die Spalte des Turms (0 oder 7)
            Piece rookPiece = board[row][rookCol];
            
            // Überprüfen, ob an der Turmposition tatsächlich ein Turm steht
            if (!(rookPiece instanceof Rook)) return false;
            Rook rook = (Rook) rookPiece;
            
            // Turm darf sich ebenfalls noch nicht bewegt haben
            if (rook.hasMoved()) return false;
            
            // Überprüfen, ob alle Felder zwischen König und Turm frei sind
            int step = (rookCol == 7) ? 1 : -1;
            for (int c = col + step; c != rookCol; c += step) {
                if (board[row][c] != null) return false; // Ein Feld ist besetzt
            }
            
            // Überprüfen, ob der König während der Rochade nicht im Schach steht oder durch ein bedrohtes Feld zieht
            if (isPathUnderCheck(newCol, board)) return false;
            if (isSquareUnderAttack(col + step, row, board) || isSquareUnderAttack(newCol - step, row, board)) {
                return false;
            }
            
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

    // Überprüft, ob der König im Schach steht
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

    // Überprüft, ob ein bestimmtes Feld von einer gegnerischen Figur angegriffen wird
    private boolean isSquareUnderAttack(int col, int row, Piece[][] board) {
        for (Piece[] rowPieces : board) {
            for (Piece piece : rowPieces) {
                if (piece != null && piece.getColor() != this.color) {
                    if (piece.isValidMove(col, row, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Überprüft, ob der König schachmatt ist
    public boolean isCheckmate(Piece[][] board) {
        if (!isInCheck(board)) return false;

        // Prüfe, ob der König sich mit einem Zug aus dem Schach befreien kann
        for (int colShift = -1; colShift <= 1; colShift++) {
            for (int rowShift = -1; rowShift <= 1; rowShift++) {
                int newCol = col + colShift;
                int newRow = row + rowShift;
                if (isInBounds(newCol, newRow) && isValidMove(newCol, newRow, board)) {
                    return false; // König kann sich befreien
                }
            }
        }
        return true; // Keine Möglichkeit, dem Schach zu entkommen (Schachmatt)
    }

    // Hilfsmethode zur Überprüfung, ob eine Position innerhalb des Schachbretts liegt
    private boolean isInBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    // Überprüft, ob der Pfad des Königs während der Rochade bedroht ist
    private boolean isPathUnderCheck(int newCol, Piece[][] board) {
        int step = (newCol > col) ? 1 : -1;
        // Überprüfe die Felder, über die der König während der Rochade zieht
        for (int c = col + step; c != newCol; c += step) {
            if (isSquareUnderAttack(c, row, board)) {
                return true;
            }
        }
        return false;
    }
}
