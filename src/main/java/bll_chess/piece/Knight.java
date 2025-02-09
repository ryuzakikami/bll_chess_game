package main.java.bll_chess.piece;

public class Knight extends Piece {

    public Knight(int color, int col, int row) {
        super(color, col, row);
    }
    
    @Override
    public boolean isValidMove(int newCol, int newRow, Piece[][] board) {
        //System.out.println("Knight at (" + col + "," + row + ") checking move to (" + newCol + "," + newRow + ")");
        int dCol = Math.abs(newCol - col);
        int dRow = Math.abs(newRow - row);
        
        if (!((dCol == 2 && dRow == 1) || (dCol == 1 && dRow == 2))) {
            return false;
        }
        
        Piece target = board[newRow][newCol];
        if (target != null) {
            //System.out.println("Target color: " + target.getColor() + ", Knight color: " + this.color);
            if (target.getColor() == this.color) {
                //System.err.println("Fail: Same color");
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getImagePath() {
        return color == 0 ? "whiteKnight" : "blackKnight";
    }
}
