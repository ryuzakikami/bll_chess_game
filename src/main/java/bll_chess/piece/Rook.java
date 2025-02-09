package main.java.bll_chess.piece;

public class Rook extends Piece {
    private boolean hasMoved = false;

    public Rook(int color, int col, int row) {
        super(color, col, row);
    }
    
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // Prüfe, ob das Zielfeld innerhalb des Bretts liegt.
        if (newCol < 0 || newCol >= 8 || newRow < 0 || newRow >= 8) {
            return false;
        }
        
        // Der Turm bewegt sich horizontal oder vertikal.
        if (col != newCol && row != newRow) {
            return false;
        }
        
        // Überprüfe, ob der Weg frei ist.
        if (col == newCol) {
            int step = (newRow > row) ? 1 : -1;
            // Schleife von der nächsten Zeile bis zur Zielreihe
            for (int r = row + step; r != newRow; r += step) {
                // Da newRow innerhalb des Bretts liegt, ist hier kein OutOfBounds zu erwarten.
                if (board[r][col] != null) {
                    return false;
                }
            }
        } else { // row == newRow
            int step = (newCol > col) ? 1 : -1;
            // Schleife von der nächsten Spalte bis zur Zielspalte
            for (int c = col + step; c != newCol; c += step) {
                if (board[row][c] != null) {
                    return false;
                }
            }
        }
        
        // Zielfeld prüfen: entweder leer oder eine gegnerische Figur steht dort.
        Piece target = board[newRow][newCol];
        return (target == null || target.getColor() != this.color);
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteRook" : "blackRook";
    }

    // Markiert diesen Turm als bewegt.
    public void markAsMoved() {
        hasMoved = true;
    }

    // Liefert den Status, ob der Turm schon gezogen wurde.
    public boolean hasMoved() {
        return hasMoved;
    }
}
