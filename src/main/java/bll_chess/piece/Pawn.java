package main.java.bll_chess.piece;

public class Pawn extends Piece {
    private boolean hasMoved;
    private int lastRow; // Speichert die letzte Reihe (optional)
    private final int direction; // -1 für weiß, +1 für schwarz
    private boolean enPassantEligible; // Flag für En Passant

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        this.hasMoved = false;
        this.lastRow = row;
        // Weiße Bauern bewegen sich nach oben (-1), schwarze nach unten (+1)
        this.direction = (color == 0) ? -1 : 1;
        this.enPassantEligible = false;
    }
    
    @Override
    protected String getImagePath() {
        return color == 0 ? "whitePawn" : "blackPawn";
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        if (!isWithinBounds(newCol, newRow)) {
            return false;
        }
        
        // Normale Bewegung (ein Feld vorwärts)
        if (newCol == col && newRow == row + direction) {
            if (board[newRow][newCol] == null) {
                return true;
            }
        }
        
        // Doppelschritt vom Startfeld
        if (newCol == col && newRow == row + 2 * direction && !hasMoved) {
            if (board[row + direction][col] == null && board[newRow][newCol] == null) {
                return true;
            }
        }
        
        // Diagonaler Schlag
        if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
            Piece targetPiece = board[newRow][newCol];
            if (targetPiece != null && targetPiece.getColor() != this.color) {
                return true;
            }
        }
        
        // En Passant prüfen:
        // Der gegnerische Bauer muss sich in der aktuellen Zeile (row) befinden.
        if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
            Piece opponentPawn = board[row][newCol];
            if (opponentPawn instanceof Pawn) {
                Pawn opponent = (Pawn) opponentPawn;
                if (opponent.isEnPassantEligible() && opponent.getRow() == row) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Überprüft, ob ein Zug innerhalb des Bretts liegt
    private boolean isWithinBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    public boolean isEnPassantEligible() {
        return enPassantEligible;
    }

    public void setEnPassantEligible(boolean eligible) {
        this.enPassantEligible = eligible;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public int getLastRow() {
        return lastRow;
    }

    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    public int getDirection() {
        return direction;
    }
}
