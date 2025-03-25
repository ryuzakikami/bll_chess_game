package main.java.bll_chess.piece;

/**
 * Die Klasse Rook repräsentiert den Turm im Schach.
 * Ein Turm bewegt sich horizontal oder vertikal über das Brett.
 */
public class Rook extends Piece {
    // Flag, ob der Turm bereits gezogen wurde (wichtig für Rochade)
    private boolean hasMoved = false;

    /**
     * Konstruktor für den Turm.
     *
     * @param color Farbe des Turms (0 = weiß, 1 = schwarz)
     * @param col   Startspalte
     * @param row   Startzeile
     */
    public Rook(int color, int col, int row) {
        super(color, col, row);
    }

    /**
     * Überprüft, ob ein geplanter Zug des Turms gültig ist.
     * Ein gültiger Zug erfolgt ausschließlich entlang einer Reihe oder Spalte.
     *
     * @param newCol Zielspalte
     * @param newRow Zielzeile
     * @param board  Das Schachbrett als 2D-Array von Piece-Objekten
     * @return true, wenn der Zug gültig ist, sonst false
     */
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // Überprüfen, ob die Zielposition innerhalb der Grenzen des Bretts liegt
        if (newCol < 0 || newCol >= 8 || newRow < 0 || newRow >= 8) {
            return false;
        }

        // Der Turm darf sich nur entlang einer Reihe oder Spalte bewegen
        if (this.col != newCol && this.row != newRow) {
            return false;
        }

        // Bestimme die Bewegungsrichtung: Schritt in Spalte oder Zeile
        int colStep = Integer.signum(newCol - this.col);
        int rowStep = Integer.signum(newRow - this.row);

        int currentCol = this.col + colStep;
        int currentRow = this.row + rowStep;

        // Überprüfen, ob der Weg zwischen Start- und Zielposition frei von anderen Figuren ist
        while (currentCol != newCol || currentRow != newRow) {
            if (board[currentRow][currentCol] != null) {
                return false;  // Ein Hindernis blockiert den Weg
            }
            currentCol += colStep;
            currentRow += rowStep;
        }

        // Prüfen, ob die Zielposition entweder leer ist oder von einer gegnerischen Figur besetzt wird
        Piece target = board[newRow][newCol];
        return target == null || target.getColor() != this.color;
    }

    /**
     * Gibt den Pfad zum Bild des Turms zurück.
     *
     * @return String mit dem Bildpfad, abhängig von der Farbe des Turms
     */
    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteRook" : "blackRook";
    }

    /**
     * Markiert diesen Turm als gezogen.
     * Dies wird z.B. für die Rochade benötigt.
     */
    public void markAsMoved() {
        hasMoved = true;
    }

    /**
     * Liefert den Status, ob der Turm bereits gezogen wurde.
     *
     * @return true, wenn der Turm schon bewegt wurde, sonst false
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Überprüft, ob eine Rochade möglich ist.
     *
     * Die Rochade ist nur möglich, wenn:
     * - der Turm noch nicht bewegt wurde
     * - der Turm und der König in derselben Reihe stehen
     * - keine Figuren zwischen dem König und dem Turm stehen
     *
     * @param board   Das Schachbrett als 2D-Array von Piece-Objekten
     * @param kingCol Spalte des Königs
     * @param kingRow Zeile des Königs
     * @param targetCol Zielspalte, über die der König ziehen würde
     * @return true, wenn die Rochadebedingungen erfüllt sind, sonst false
     */
    public boolean canCastle(Piece[][] board, int kingCol, int kingRow, int targetCol) {
        // Der Turm darf noch nicht gezogen worden sein
        if (hasMoved) {
            return false;
        }

        // Turm und König müssen in derselben Reihe stehen
        if (this.row != kingRow) {
            return false;
        }

        // Bestimme die Bewegungsrichtung zum Ziel (links oder rechts)
        int step = Integer.signum(targetCol - this.col);
        int currentCol = this.col + step;

        // Überprüfe, ob zwischen Turm und Zielposition (bzw. König) keine Figuren stehen
        while (currentCol != targetCol) {
            if (board[kingRow][currentCol] != null) {
                return false;
            }
            currentCol += step;
        }

        return true;
    }
}
