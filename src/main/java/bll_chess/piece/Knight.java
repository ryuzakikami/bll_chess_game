package main.java.bll_chess.piece;

/**
 * Die Klasse Knight repraesentiert den Springer im Schach.
 */
public class Knight extends Piece {

    public Knight(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // 1. Prüfe, ob das Zielfeld innerhalb des Bretts liegt
        if (!isInBounds(newCol, newRow)) {
            return false;
        }

        // 2. Prüfe L-förmige Bewegung (2-1 Muster)
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        if (!((dCol == 2 && dRow == 1) || (dCol == 1 && dRow == 2))) {
            return false;
        }

        // 3. Springer kann über andere Figuren springen - nur Zielfeld prüfen
        return isValidTarget(board[newRow][newCol]);
    }

    private boolean isInBounds(int col, int row) {
        // Brettgrenzen: 0-7 für beide Koordinaten
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    private boolean isValidTarget(Piece target) {
        // Zielfeld muss leer oder gegnerische Figur sein
        return target == null || target.getColor() != this.color;
    }

    @Override
    protected String getImagePath() {
        return color == 1 ? "whiteKnight" : "blackKnight";
    }
}