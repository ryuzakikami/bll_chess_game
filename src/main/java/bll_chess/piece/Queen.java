package main.java.bll_chess.piece;

/**
 * Die Klasse Queen repräsentiert die Dame im Schach.
 * Sie kombiniert die Bewegungsmöglichkeiten von Turm und Läufer.
 */
public class Queen extends Piece {

    /**
     * Konstruktor für die Dame.
     * 
     * @param color Farbe der Dame (0 = weiß, 1 = schwarz)
     * @param col   Startspalte
     * @param row   Startzeile
     */
    public Queen(int color, int col, int row) {
        super(color, col, row);
    }
    
    /**
     * Überprüft, ob ein geplanter Zug der Dame gültig ist.
     * 
     * Die Dame bewegt sich entweder horizontal, vertikal (wie ein Turm) oder diagonal (wie ein Läufer).
     * Dabei darf der Weg zum Ziel nicht durch andere Figuren blockiert sein.
     * 
     * @param newCol Zielspalte
     * @param newRow Zielzeile
     * @param board  Das Schachbrett als 2D-Array von Piece-Objekten
     * @return true, wenn der Zug gültig ist, sonst false
     */
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        
        // Die Dame bewegt sich wie ein Turm (horizontal/vertikal) oder wie ein Läufer (diagonal).
        if (col == newCol || row == newRow) {
            // Turm-Logik: Überprüfe, ob der Weg frei ist
            if (col == newCol) {
                int step = (newRow > row) ? 1 : -1;
                for (int r = row + step; r != newRow; r += step) {
                    if (board[r][col] != null) {
                        return false; // Ein Feld im Weg ist blockiert
                    }
                }
            } else { // row == newRow
                int step = (newCol > col) ? 1 : -1;
                for (int c = col + step; c != newCol; c += step) {
                    if (board[row][c] != null) {
                        return false; // Ein Feld im Weg ist blockiert
                    }
                }
            }
        } else if (dCol == dRow) {
            // Läufer-Logik: Diagonale Bewegung, überprüfe, ob der diagonale Pfad frei ist
            int stepCol = (newCol > col) ? 1 : -1;
            int stepRow = (newRow > row) ? 1 : -1;
            int c = col + stepCol;
            int r = row + stepRow;
            while (c != newCol && r != newRow) {
                if (board[r][c] != null) {
                    return false; // Ein diagonales Feld ist blockiert
                }
                c += stepCol;
                r += stepRow;
            }
        } else {
            return false; // Bewegung entspricht weder Turm- noch Läuferbewegung
        }
        
        // Überprüfe das Zielfeld: Es muss entweder leer sein oder eine gegnerische Figur enthalten
        Piece target = board[newRow][newCol];
        return (target == null || target.getColor() != this.color);
    }

    /**
     * Gibt den Pfad zum Bild der Dame zurück.
     * 
     * @return String mit dem Bildpfad, abhängig von der Farbe der Dame
     */
    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteQueen" : "blackQueen";
    }
}
