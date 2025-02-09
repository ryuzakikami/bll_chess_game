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

    // Zusätzliche Felder für die Animation
    protected int animOffsetX = 0;
    protected int animOffsetY = 0;

    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        this.image = loadImage(getImagePath());
    }

    // Abstrakte Methode, um den spezifischen Pfad zum Bild zu erhalten
    protected abstract String getImagePath();

    // Bild laden mit festem Pfad
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
        
        

    // Getter für das Bild
    public BufferedImage getImage() {
        return image;
    }

    // Berechnet die x-Position unter Berücksichtigung des Animations-Offsets
    public int getX(int squareSize) {
        return col * squareSize + animOffsetX;
    }

    // Berechnet die y-Position unter Berücksichtigung des Animations-Offsets
    public int getY(int squareSize) {
        return row * squareSize + animOffsetY;
    }

    // Setter für den Animations-Offset
    public void setAnimOffset(int offsetX, int offsetY) {
        this.animOffsetX = offsetX;
        this.animOffsetY = offsetY;
    }

    // Getter und Setter für Spalte und Zeile
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

    // Prüft, ob ein Zug auf ein Ziel möglich ist (wird in Subklassen überschrieben)
    public abstract boolean isValidMove(int newCol, int newRow, Piece[][] board);

    // Prüft, ob ein Ziel dieselbe Farbe hat
    public boolean isSameColor(Piece targetPiece) {
        return targetPiece != null && this.color == targetPiece.getColor();
    }

    // Überprüft, ob der Weg zwischen zwei Feldern blockiert ist (hilfsweise Methode)
    protected boolean isPathBlocked(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int deltaCol = Integer.signum(endCol - startCol);
        int deltaRow = Integer.signum(endRow - startRow);
        int col = startCol + deltaCol;
        int row = startRow + deltaRow;

        while (col != endCol || row != endRow) {
            if (board[row][col] != null) {
                return true; // Der Weg ist blockiert
            }
            col += deltaCol;
            row += deltaRow;
        }
        return false;
    }
}
