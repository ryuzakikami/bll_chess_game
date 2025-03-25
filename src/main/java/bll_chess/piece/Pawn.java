package main.java.bll_chess.piece;

/**
 * Die Klasse Pawn repräsentiert den Bauern im Schach.
 * Ein Bauer bewegt sich primär vorwärts und schlägt diagonal.
 * Zusätzlich gibt es spezielle Regeln wie den Doppelschritt und En Passant.
 */
public class Pawn extends Piece {
    // Flag, ob der Bauer bereits gezogen wurde
    private boolean hasMoved;
    // Speichert die letzte Zeile, in der sich der Bauer befand (optional, für Animation oder Logik)
    private int lastRow;
    // Bewegungsrichtung: -1 für weiße Bauern (nach oben), +1 für schwarze Bauern (nach unten)
    private final int direction;
    // Flag, ob der Bauer für En Passant anfällig ist
    private boolean enPassantEligible;

    /**
     * Konstruktor für den Bauern.
     *
     * @param color Farbe des Bauern (0 = weiß, 1 = schwarz)
     * @param col   Startspalte
     * @param row   Startzeile
     */
    public Pawn(int color, int col, int row) {
        super(color, col, row);
        this.hasMoved = false;
        this.lastRow = row;
        // Weiße Bauern bewegen sich nach oben (-1), schwarze nach unten (+1)
        this.direction = (color == 0) ? -1 : 1;
        this.enPassantEligible = false;
    }
    
    /**
     * Gibt den Pfad zum Bild des Bauern zurück.
     *
     * @return String mit dem Bildpfad, abhängig von der Farbe des Bauern
     */
    @Override
    protected String getImagePath() {
        return color == 0 ? "whitePawn" : "blackPawn";
    }

    /**
     * Überprüft, ob ein geplanter Zug des Bauern gültig ist.
     *
     * Es werden folgende Züge berücksichtigt:
     * - Normale Bewegung: Ein Feld vorwärts, wenn das Feld frei ist.
     * - Doppelschritt: Zwei Felder vorwärts vom Ausgangsplatz, wenn beide Felder frei sind.
     * - Diagonaler Schlag: Ein Feld diagonal vorwärts, wenn eine gegnerische Figur steht.
     * - En Passant: Diagonaler Zug, wenn ein gegnerischer Bauer en passant geschlagen werden kann.
     *
     * @param newCol Zielspalte
     * @param newRow Zielzeile
     * @param board  Das Schachbrett als 2D-Array von Piece-Objekten
     * @return true, wenn der Zug gültig ist, sonst false
     */
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // Überprüfe, ob die Zielposition innerhalb des Schachbretts liegt
        if (!isWithinBounds(newCol, newRow)) {
            return false;
        }
        
        // Normale Bewegung: Ein Feld vorwärts in Richtung der Bewegungsrichtung
        if (newCol == col && newRow == row + direction) {
            if (board[newRow][newCol] == null) {
                return true;
            }
        }
        
        // Doppelschritt vom Startfeld: Zwei Felder vorwärts, falls der Bauer noch nicht gezogen wurde
        if (newCol == col && newRow == row + 2 * direction && !hasMoved) {
            if (board[row + direction][col] == null && board[newRow][newCol] == null) {
                return true;
            }
        }
        
        // Diagonaler Schlag: Ein Feld diagonal vorwärts, wenn auf dem Zielfeld eine gegnerische Figur steht
        if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
            Piece targetPiece = board[newRow][newCol];
            if (targetPiece != null && targetPiece.getColor() != this.color) {
                return true;
            }
        }
        
        // En Passant:
        // Der Zug erfolgt diagonal vorwärts, auch wenn das Zielfeld leer ist.
        // Voraussetzung ist, dass der gegnerische Bauer neben dem eigenen Bauern steht
        // und gerade einen Doppelschritt gemacht hat.
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
    
    /**
     * Überprüft, ob eine gegebene Position innerhalb des Schachbretts liegt.
     *
     * @param col Spalte
     * @param row Zeile
     * @return true, wenn die Position innerhalb des 8x8-Bretts liegt, sonst false
     */
    private boolean isWithinBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    /**
     * Liefert den Status, ob der Bauer für En Passant anfällig ist.
     *
     * @return true, wenn En Passant möglich ist, sonst false
     */
    public boolean isEnPassantEligible() {
        return enPassantEligible;
    }

    /**
     * Setzt, ob der Bauer für En Passant anfällig ist.
     *
     * @param eligible true, wenn der Bauer anfällig sein soll, sonst false
     */
    public void setEnPassantEligible(boolean eligible) {
        this.enPassantEligible = eligible;
    }

    /**
     * Gibt zurück, ob der Bauer bereits bewegt wurde.
     *
     * @return true, wenn der Bauer schon gezogen wurde, sonst false
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Setzt den Status, ob der Bauer bereits bewegt wurde.
     *
     * @param hasMoved true, wenn der Bauer gezogen wurde, sonst false
     */
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    /**
     * Gibt die letzte Zeile zurück, in der sich der Bauer befand.
     *
     * @return die letzte Zeile als int
     */
    public int getLastRow() {
        return lastRow;
    }

    /**
     * Setzt die letzte Zeile, in der sich der Bauer befand.
     *
     * @param lastRow die letzte Zeile als int
     */
    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    /**
     * Gibt die Bewegungsrichtung des Bauern zurück.
     * Für weiße Bauern ist dies -1 (nach oben), für schwarze +1 (nach unten).
     *
     * @return Richtung als int (-1 oder +1)
     */
    public int getDirection() {
        return direction;
    }
}
