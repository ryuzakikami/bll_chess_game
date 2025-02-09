package main.java.bll_chess.piece;

public class Queen extends Piece {

    public Queen(int color, int col, int row) {
        super(color, col, row);
    }
    
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        
        // Dame bewegt sich wie Turm (horizontal/vertikal) oder Läufer (diagonal).
        if (col == newCol || row == newRow) {
            // Turm-Logik
            if (col == newCol) {
                int step = (newRow > row) ? 1 : -1;
                for (int r = row + step; r != newRow; r += step) {
                    if (board[r][col] != null) {
                        return false;
                    }
                }
            } else { // row == newRow
                int step = (newCol > col) ? 1 : -1;
                for (int c = col + step; c != newCol; c += step) {
                    if (board[row][c] != null) {
                        return false;
                    }
                }
            }
        } else if (dCol == dRow) {
            // Läufer-Logik
            int stepCol = (newCol > col) ? 1 : -1;
            int stepRow = (newRow > row) ? 1 : -1;
            int c = col + stepCol;
            int r = row + stepRow;
            while (c != newCol && r != newRow) {
                if (board[r][c] != null) {
                    return false;
                }
                c += stepCol;
                r += stepRow;
            }
        } else {
            return false;
        }
        
        // Zielfeld prüfen: entweder leer oder gegnerische Figur
        Piece target = board[newRow][newCol];
        return (target == null || target.getColor() != this.color);
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteQueen" : "blackQueen";
    }
}
