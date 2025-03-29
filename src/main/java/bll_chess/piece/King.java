package main.java.bll_chess.piece;

public class King extends Piece {
    private boolean hasMoved = false;

    public King(int color, int col, int row) {
        super(color, col, row);
    }

    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        if (!isInBounds(newCol, newRow)) return false;
        
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);

        // Normale Königszüge (1 Feld in alle Richtungen)
        if ((dCol <= 1 && dRow <= 1) && !(dCol == 0 && dRow == 0)) {
            return isValidTarget(board[newRow][newCol]);
        }

        // Rochade (nur wenn König nicht bewegt)
        if (dRow == 0 && dCol == 2 && !hasMoved) {
            return isValidCastling(newCol, board);
        }

        return false;
    }

    private boolean isValidCastling(int newCol, Piece[][] board) {
        int rookCol = (newCol > col) ? 7 : 0; // Turmposition
        int step = (newCol > col) ? 1 : -1;   // Bewegungsrichtung
        
        // 1. Prüfe Turm
        Piece rook = board[row][rookCol];
        if (!(rook instanceof Rook) || ((Rook) rook).hasMoved()) {
            return false;
        }

        // 2. Prüfe freie Felder zwischen König und Turm
        for (int c = col + step; c != rookCol; c += step) {
            if (board[row][c] != null) return false;
        }

        // 3. Prüfe, ob König im Schach steht oder bedrohte Felder passiert
        if (isInCheck(board) || isPathUnderAttack(newCol, board)) {
            return false;
        }

        return true;
    }

    private boolean isValidTarget(Piece target) {
        return target == null || target.getColor() != color;
    }

    @Override
    protected String getImagePath() {
        return color == 1 ? "whiteKing" : "blackKing";
    }

    public void markAsMoved() {
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean isInCheck(Piece[][] board) {
        // Prüfe alle gegnerischen Figuren
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.getColor() != color) {
                    if (piece.isValidMove(col, row, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSquareUnderAttack(int col, int row, Piece[][] board) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.getColor() != color) {
                    if (piece.isValidMove(col, row, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCheckmate(Piece[][] board) {
        if (!isInCheck(board)) return false;

        // Prüfe alle möglichen Königszüge
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newCol = col + dx;
                int newRow = row + dy;
                if (isValidMove(newCol, newRow, board) && !isSquareUnderAttack(newCol, newRow, board)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isInBounds(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    private boolean isPathUnderAttack(int newCol, Piece[][] board) {
        int step = (newCol > col) ? 1 : -1;
        for (int c = col; c != newCol + step; c += step) {
            if (isSquareUnderAttack(c, row, board)) {
                return true;
            }
        }
        return false;
    }
}