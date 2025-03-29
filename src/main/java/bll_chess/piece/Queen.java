package main.java.bll_chess.piece;

/**
 * Die Klasse Queen repräsentiert die Dame im Schach.
 * Sie kombiniert die Bewegungsmöglichkeiten von Turm und Läufer.
 */
public class Queen extends Piece {

    public Queen(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // 1. Prüfe, ob das Zielfeld innerhalb des Bretts liegt
        if (newCol < 0 || newCol >= 8 || newRow < 0 || newRow >= 8) {
            return false;
        }

        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);

        // 2. Prüfe Turm-Bewegung (gerade Linie)
        if (col == newCol || row == newRow) {
            return checkStraightLine(newCol, newRow, board);
        }

        // 3. Prüfe Läufer-Bewegung (diagonale Linie)
        if (dCol == dRow) {
            return checkDiagonalLine(newCol, newRow, board);
        }

        return false;
    }

    private boolean checkStraightLine(int newCol, int newRow, Piece[][] board) {
        int step;
        if (col == newCol) { // Vertikale Bewegung
            step = (newRow > row) ? 1 : -1;
            for (int r = row + step; r != newRow; r += step) {
                if (board[r][col] != null) return false;
            }
        } else { // Horizontale Bewegung
            step = (newCol > col) ? 1 : -1;
            for (int c = col + step; c != newCol; c += step) {
                if (board[row][c] != null) return false;
            }
        }
        return isValidTarget(board[newRow][newCol]);
    }

    private boolean checkDiagonalLine(int newCol, int newRow, Piece[][] board) {
        int stepCol = (newCol > col) ? 1 : -1;
        int stepRow = (newRow > row) ? 1 : -1;
        
        int currentCol = col + stepCol;
        int currentRow = row + stepRow;
        
        while (currentCol != newCol && currentRow != newRow) {
            if (board[currentRow][currentCol] != null) return false;
            currentCol += stepCol;
            currentRow += stepRow;
        }
        return isValidTarget(board[newRow][newCol]);
    }

    private boolean isValidTarget(Piece target) {
        return target == null || target.getColor() != this.color;
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteQueen" : "blackQueen";
    }
}