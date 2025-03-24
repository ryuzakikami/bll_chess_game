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
        
        // Normal move: one square in any direction
        if (dCol <= 1 && dRow <= 1) {
            Piece target = board[newRow][newCol];
            return (target == null || target.getColor() != this.color);
        }
        
        // Castling: move two squares horizontally (dRow == 0 && dCol == 2)
        if (dRow == 0 && dCol == 2) {
            if (hasMoved) return false; // King must not have moved
            
            int rookCol = (newCol > col) ? 7 : 0; // Determine the rook's column (either 0 or 7)
            Piece rookPiece = board[row][rookCol];
            
            // Check if the piece at the rook's position is a rook
            if (!(rookPiece instanceof Rook)) return false;
            
            Rook rook = (Rook) rookPiece;
            
            // Rook must also not have moved
            if (rook.hasMoved()) return false;
            
            // Check that the squares between the king and rook are empty
            int step = (rookCol == 7) ? 1 : -1;
            for (int c = col + step; c != rookCol; c += step) {
                if (board[row][c] != null) return false; // If there is any piece in the way, it's an invalid move
            }
            
            // Check if the king is in check while castling or if the squares the king moves through are attacked
            if (isPathUnderCheck(newCol, newRow, board)) return false;
            
            // Check if the squares the king travels over are attacked (castling involves the king passing through squares)
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

    // Überprüft, ob der König auf seinem Zugpfad in Schach steht
    private boolean isSquareUnderAttack(int col, int row, Piece[][] board) {
        // Check if a given square is under attack by any piece
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

    // Überprüft, ob der König an einer bestimmten Position im Schach steht
    private boolean isInCheckAtPosition(int targetCol, int targetRow, Piece[][] board) {
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece != null && piece.getColor() != this.color &&
                    piece.isValidMove(targetCol, targetRow, board)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Überprüft, ob der König in Schachmatt ist
    public boolean isCheckmate(Piece[][] board) {
        if (!isInCheck(board)) return false;

        // Überprüfe, ob der König sich in einem Zug aus dem Schach befreien kann
        for (int colShift = -1; colShift <= 1; colShift++) {
            for (int rowShift = -1; rowShift <= 1; rowShift++) {
                int newCol = col + colShift;
                int newRow = row + rowShift;
                if (isInBounds(newCol, newRow) && isValidMove(newCol, newRow, board)) {
                    return false; // König kann sich aus dem Schach befreien
                }
            }
        }
        return true; // Keine Möglichkeit, dem Schach zu entkommen (Schachmatt)
    }

    // Hilfsmethode zur Überprüfung, ob die Position innerhalb des Schachbretts liegt
    private boolean isInBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    // Check if the king's path is under check during castling
    private boolean isPathUnderCheck(int newCol, int newRow, Piece[][] board) {
        int step = (newCol > col) ? 1 : -1;
        
        // Check the squares the king passes over (those are the columns between the current position and the new position)
        for (int c = col + step; c != newCol; c += step) {
            if (isSquareUnderAttack(c, row, board)) {
                return true;
            }
        }
        return false;
    }
}
