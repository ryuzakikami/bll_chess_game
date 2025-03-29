package main.java.bll_chess.piece;

/**
 * Die Klasse Pawn repräsentiert den Bauern im Schach.
 * Ein Bauer bewegt sich primär vorwärts und schlägt diagonal.
 * Zusätzlich gibt es spezielle Regeln wie den Doppelschritt und En Passant.
 */
public class Pawn extends Piece {
    private boolean hasMoved;
    private int lastRow;
    // Richtung korrigiert: Weiß (+1) bewegt sich nach "unten" im internen Array (visuell aufwärts)
    private final int direction;
    private boolean enPassantEligible;

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        this.hasMoved = false;
        this.lastRow = row;
        // Weiß (0) bewegt sich intern nach +1 (visuell nach oben), Schwarz (1) nach -1
        this.direction = (color == 0) ? 1 : -1;
        this.enPassantEligible = false;
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whitePawn" : "blackPawn";
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        if (!isWithinBounds(newCol, newRow)) return false;

        // Normale Bewegung (1 Feld)
        if (newCol == col && newRow == row + direction) {
            return board[newRow][newCol] == null;
        }

        // Doppelschritt (2 Felder, nur von Startreihe)
        if (newCol == col && newRow == row + 2 * direction && !hasMoved) {
            int intermediateRow = row + direction;
            return board[intermediateRow][col] == null && board[newRow][col] == null;
        }

        // Diagonaler Schlag (inkl. En Passant)
        if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
            // Standard-Schlagzug
            Piece target = board[newRow][newCol];
            if (target != null && target.getColor() != color) {
                return true;
            }

            // En Passant: Prüfe gegnerischen Bauer in derselben *ursprünglichen* Reihe
            Piece enPassantTarget = board[row][newCol];
            if (enPassantTarget instanceof Pawn) {
                Pawn enemyPawn = (Pawn) enPassantTarget;
                return enemyPawn.isEnPassantEligible() && enemyPawn.getRow() == row;
            }
        }

        return false;
    }

    // --- Hilfsmethoden ---
    private boolean isWithinBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    // --- Getter & Setter ---
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