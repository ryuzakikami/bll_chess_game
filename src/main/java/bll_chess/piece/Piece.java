package main.java.bll_chess.piece;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public abstract class Piece {
    protected BufferedImage image;
    protected int color; // 0 = weiß, 1 = schwarz
    protected int col;   // Spalte
    protected int row;   // Zeile

    // Zusätzliche Felder für Animation
    protected int animOffsetX = 0;
    protected int animOffsetY = 0;

    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        this.image = loadImage(getImagePath());
    }

    protected abstract String getImagePath();

    protected BufferedImage loadImage(String imgPath) {
        BufferedImage img = null;
        String fullPath = "src/main/resources/pieces/" + imgPath + ".png";
        File file = new File(fullPath);
        if (file.exists()) {
            try {
                img = ImageIO.read(file);
            } catch (IOException e) {
                System.err.println("Fehler beim Laden des Bildes: " + fullPath);
            }
        } else {
            System.err.println("Bild nicht gefunden: " + fullPath);
        }
        return img;
    }  

    // Anpassung des Sounds auf die Sound-Klasse
    public void playMoveSound() {
        // Nutze die Sound-Klasse und rufe den entsprechenden Sound ab
        if (color == 0) {  // Weiß
            Sound.play(Sound.SoundType.WHITE_MOVE);
        } else {  // Schwarz
            Sound.play(Sound.SoundType.BLACK_MOVE);
        }
    }

    public void playCaptureSound() {
        // Nutze die Sound-Klasse und rufe den entsprechenden Sound ab
        if (color == 0) {  // Weiß
            Sound.play(Sound.SoundType.WHITE_TAKES);
        } else {  // Schwarz
            Sound.play(Sound.SoundType.BLACK_CAPTURES);
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getX(int squareSize) {
        return col * squareSize + animOffsetX;
    }

    public int getY(int squareSize) {
        return row * squareSize + animOffsetY;
    }

    public void setAnimOffset(int offsetX, int offsetY) {
        this.animOffsetX = offsetX;
        this.animOffsetY = offsetY;
    }

    public int getCol() {
        return col;
    }
    public void setCol(int col) {
        this.col = col;
    }
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }

    public abstract boolean isValidMove(int newCol, int newRow, Piece[][] board);

    public boolean isSameColor(Piece targetPiece) {
        return targetPiece != null && this.color == targetPiece.getColor();
    }

    protected boolean isPathBlocked(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int deltaCol = Integer.signum(endCol - startCol);
        int deltaRow = Integer.signum(endRow - startRow);
        int col = startCol + deltaCol;
        int row = startRow + deltaRow;

        while (col != endCol || row != endRow) {
            if (board[row][col] != null) {
                return true;
            }
            col += deltaCol;
            row += deltaRow;
        }
        return false;
    }
}
