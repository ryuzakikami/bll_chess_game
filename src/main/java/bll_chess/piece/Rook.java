package main.java.bll_chess.piece;

public class Rook extends Piece {
    private boolean hasMoved = false;

    public Rook(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        if (newCol < 0 || newCol >= 8 || newRow < 0 || newRow >= 8) {
            return false;
        }

        // Überprüfen, ob der Turm entlang einer Reihe oder einer Spalte gezogen wird
        if (this.col != newCol && this.row != newRow) {
            return false;
        }

        // Bewegungsrichtung (entlang der Reihe oder der Spalte)
        int colStep = Integer.signum(newCol - this.col);
        int rowStep = Integer.signum(newRow - this.row);

        int currentCol = this.col + colStep;
        int currentRow = this.row + rowStep;

        // Überprüfen, ob der Weg blockiert ist
        while (currentCol != newCol || currentRow != newRow) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentCol += colStep;
            currentRow += rowStep;
        }

        // Zielposition prüfen (ob sie leer ist oder eine gegnerische Figur steht)
        Piece target = board[newRow][newCol];
        return target == null || target.getColor() != this.color;
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteRook" : "blackRook";
    }

    // Markiert diesen Turm als bewegt
    public void markAsMoved() {
        hasMoved = true;
    }

    // Liefert den Status, ob der Turm schon gezogen wurde
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Castling-Logik: Der Turm wird beim Castling nur bewegt, wenn der König sich bewegt hat und der Turm noch nicht gezogen wurde.
     */
    public boolean canCastle(Piece[][] board, int kingCol, int kingRow, int targetCol) {
        // Castling ist nur möglich, wenn der Turm noch nicht bewegt wurde und keine Figuren zwischen König und Turm stehen
        if (hasMoved) {
            return false;
        }

        // Überprüfen, ob der Turm und der König auf derselben Reihe sind
        if (this.row != kingRow) {
            return false;
        }

        // Bestimmen, ob das Castling-Ziel links oder rechts vom Turm ist
        int step = Integer.signum(targetCol - this.col);
        int currentCol = this.col + step;

        // Überprüfen, ob zwischen dem König und dem Turm keine Figuren stehen
        while (currentCol != targetCol) {
            if (board[kingRow][currentCol] != null) {
                return false;
            }
            currentCol += step;
        }

        return true;
    }
}
