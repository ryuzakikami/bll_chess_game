package main.java.bll_chess.piece;

/**
 * Die Klasse Rook repräsentiert den Turm im Schach.
 * Ein Turm bewegt sich horizontal oder vertikal über das Brett.
 */
public class Rook extends Piece {
    private boolean hasMoved = false;

    public Rook(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        // 1. Prüfe Brettgrenzen
        if (newCol < 0 || newCol >= 8 || newRow < 0 || newRow >= 8) {
            return false;
        }

        // 2. Nur horizontale/vertikale Bewegung erlaubt
        if (col != newCol && row != newRow) return false;

        // 3. Bestimme Schrittrichtung
        int colStep = Integer.compare(newCol, col);
        int rowStep = Integer.compare(newRow, row);

        // 4. Prüfe alle Felder zwischen Start und Ziel
        int currentCol = col + colStep;
        int currentRow = row + rowStep;
        while (currentCol != newCol || currentRow != newRow) {
            if (board[currentRow][currentCol] != null) return false;
            currentCol += colStep;
            currentRow += rowStep;
        }

        // 5. Zielfeld muss leer oder Gegner sein
        Piece target = board[newRow][newCol];
        return target == null || target.getColor() != color;
    }

    @Override
    protected String getImagePath() {
        return color == 1 ? "whiteRook" : "blackRook";
    }

    // --- Rochade-Logik ---
    public boolean canCastle(Piece[][] board, int kingCol, int kingRow) {
        // 1. Turm unbenutzt und gleiche Reihe wie König
        if (hasMoved || row != kingRow) return false;
        
        // 2. Bestimme Richtung (links/rechts)
        int step = Integer.compare(kingCol, col);
        int currentCol = col + step;
        
        // 3. Prüfe freie Felder zwischen Turm und König
        while (currentCol != kingCol) {
            if (board[kingRow][currentCol] != null) return false;
            currentCol += step;
        }
        return true;
    }

    public void markAsMoved() {
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }
}