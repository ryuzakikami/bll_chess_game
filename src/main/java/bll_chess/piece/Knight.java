package main.java.bll_chess.piece;

/**
 * Die Klasse Knight repräsentiert den Springer im Schach.
 */
public class Knight extends Piece {

    /**
     * Konstruktor für den Springer.
     * 
     * @param color Die Farbe des Springers (0 = weiß, 1 = schwarz)
     * @param col   Die Spalte, in der der Springer steht
     * @param row   Die Zeile, in der der Springer steht
     */
    public Knight(int color, int col, int row) {
        super(color, col, row);
    }
    
    /**
     * Überprüft, ob ein geplanter Zug des Springers gültig ist.
     * 
     * Der Springer bewegt sich in einem "L"-förmigen Muster: zwei Felder in eine Richtung und
     * dann ein Feld in die senkrechte Richtung.
     * 
     * @param newCol Zielspalte
     * @param newRow Zielzeile
     * @param board  Das Schachbrett als 2D-Array von Piece-Objekten
     * @return true, wenn der Zug gültig ist, sonst false
     */
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // Berechne die absolute Differenz in Spalte und Zeile zwischen aktueller Position und Zielposition
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        
        // Überprüfe, ob der Zug den L-förmigen Bewegungsregeln des Springers entspricht
        if (!((dCol == 2 && dRow == 1) || (dCol == 1 && dRow == 2))) {
            return false;
        }
        
        // Überprüfe, ob am Ziel ein Piece steht
        Piece target = board[newRow][newCol];
        if (target != null) {
            // Falls das Ziel von einer eigenen Figur besetzt ist, ist der Zug ungültig
            if (target.getColor() == this.color) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gibt den Pfad zum Bild des Springers zurück.
     * 
     * @return String mit dem Bildpfad, abhängig von der Farbe des Springers
     */
    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteKnight" : "blackKnight";
    }
}
