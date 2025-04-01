package main.java.bll_chess.piece;

public class Bishop extends Piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // 1. Pruefe, ob Start- und Zielfeld identisch sind
        if (col == newCol && row == newRow) {
            return false;
        }

        // 2. Pruefe, ob die Bewegung diagonal erfolgt (Δx = ±Δy)
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        if (dCol != dRow) {
            return false;
        }

        // 3. Bestimme die Schrittrichtung
        int colStep = Integer.compare(newCol, col); // +1 (rechts), -1 (links)
        int rowStep = Integer.compare(newRow, row);  // +1 (nach "unten" in der internen Darstellung)

        // 4. Ueberpruefe alle Felder entlang der Diagonalen (außer Start- und Zielfeld)
        int currentCol = col + colStep;
        int currentRow = row + rowStep;
        
        while (currentCol != newCol || currentRow != newRow) {
            // Blockade durch andere Figur?
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentCol += colStep;
            currentRow += rowStep;
        }

        // 5. Zielfeld: Leer oder gegnerische Figur
        Piece target = board[newRow][newCol];
        return target == null || target.getColor() != this.color;
    }

    @Override
    protected String getImagePath() {
        return color == 1 ? "whiteBishop" : "blackBishop";
    }
}