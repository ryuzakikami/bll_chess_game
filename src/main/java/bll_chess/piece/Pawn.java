package main.java.bll_chess.piece;

public class Pawn extends Piece {
    private boolean hasMoved;
    private int lastRow; // Speichert die letzte Reihe, in der sich der Bauer befand (optional)
    private final int direction; // Richtung der Bewegung basierend auf der Farbe
    private boolean enPassantEligible; // Flag für En Passant

    // En Passant Reihen (wobei ein Doppelzug den Bauern auf diese Reihe bringt)
    //private static final int WHITE_EN_PASSANT_ROW = 3;
    //private static final int BLACK_EN_PASSANT_ROW = 4;

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        this.hasMoved = false;
        this.lastRow = row;
        this.direction = (color == 0) ? 1 : -1; // Weiße Bauern bewegen sich nach unten, schwarze nach oben
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
                return true; // Normale Bewegung
            }
        }
    
        // Doppelschritt vom Startfeld
        if (newCol == col && newRow == row + 2 * direction && !hasMoved) {
            if (board[row + direction][col] == null && board[newRow][newCol] == null) {
                return true; // Doppelschritt
            }
        }
    
        // Diagonaler Schlag
        if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
            Piece targetPiece = board[newRow][newCol];
            if (targetPiece != null && targetPiece.getColor() != this.color) {
                return true; // Normaler Schlag
            }
        }
    
        // En Passant prüfen
     if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
     // Der gegnerische Bauer muss sich in der aktuellen Reihe befinden
     Piece opponentPawn = board[row][newCol];
     if (opponentPawn instanceof Pawn) {
        Pawn opponent = (Pawn) opponentPawn;
        // Überprüfen, ob der gegnerische Bauer gerade zwei Felder vorgerückt ist
        // -> Hier wird die aktuelle Reihe (row) geprüft, nicht row + direction
        if (opponent.isEnPassantEligible() && opponent.getRow() == row) {
            return true; // En Passant möglich
         }
      }
     }
        return false; // Ungültiger Zug
    }
    
    
    // Überprüft, ob ein Zug innerhalb der Brettgrenzen liegt
    private boolean isWithinBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    // En Passant Eligibility: hier explizit das Flag.
    public boolean isEnPassantEligible() {
        return enPassantEligible;
    }

    public void setEnPassantEligible(boolean eligible) {
        this.enPassantEligible = eligible;
    }

    // Getter und Setter für andere Attribute
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
