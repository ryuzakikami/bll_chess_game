package main.java.bll_chess.piece;

public class Bishop extends Piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // 1. Prüfe, ob die eigenen (Start-)Koordinaten gültig sind.
        if (this.col < 0 || this.col >= 8 || this.row < 0 || this.row >= 8) {
            return false;
        }
        // 2. Prüfe, ob das Zielfeld innerhalb des Bretts liegt.
        if (newCol < 0 || newCol >= 8 || newRow < 0 || newRow >= 8) {
            return false;
        }
        
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        // Der Läufer bewegt sich nur diagonal: dCol muss gleich dRow sein.
        if (dCol != dRow) {
            return false;
        }
        
        int colStep = (newCol - col) > 0 ? 1 : -1;
        int rowStep = (newRow - row) > 0 ? 1 : -1;
        int currentCol = col + colStep;
        int currentRow = row + rowStep;
        
        // 3. Überprüfe alle Felder entlang der Diagonalen bis zum Ziel.
        while (currentCol != newCol && currentRow != newRow) {
            // Überprüfe, ob currentRow/currentCol innerhalb des Brettes liegen.
            if (currentRow < 0 || currentRow >= 8 || currentCol < 0 || currentCol >= 8) {
                return false;
            }
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentCol += colStep;
            currentRow += rowStep;
        }
        
        // 4. Prüfe das Zielfeld: Entweder leer oder enthält eine gegnerische Figur.
        Piece target = board[newRow][newCol];
        return (target == null || target.getColor() != this.color);
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteBishop" : "blackBishop";
    }
}
