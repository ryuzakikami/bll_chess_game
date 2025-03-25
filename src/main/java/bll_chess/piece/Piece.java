package main.java.bll_chess.piece;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Abstrakte Basisklasse für alle Schachfiguren.
 */
public abstract class Piece {
    protected BufferedImage image;  // Bild der Figur
    protected int color;            // Farbe der Figur (0 = weiß, 1 = schwarz)
    protected int col;              // Spalte der Figur
    protected int row;              // Zeile der Figur

    // Zusätzliche Felder für Animationseffekte
    protected int animOffsetX = 0;
    protected int animOffsetY = 0;

    /**
     * Konstruktor für eine Schachfigur.
     * Lädt das entsprechende Bild der Figur.
     * 
     * @param color Farbe der Figur (0 = weiß, 1 = schwarz)
     * @param col   Startspalte
     * @param row   Startzeile
     */
    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        this.image = loadImage(getImagePath());
    }

    /**
     * Abstrakte Methode, die von Unterklassen implementiert wird,
     * um den spezifischen Bildpfad der Figur zurückzugeben.
     * 
     * @return Der Bildpfad als String
     */
    protected abstract String getImagePath();

    /**
     * Lädt das Bild der Figur aus dem Ressourcenverzeichnis.
     * 
     * @param imgPath Pfad zum Bild (ohne Dateiendung)
     * @return BufferedImage des geladenen Bildes oder null, falls nicht gefunden
     */
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

    /**
     * Spielt den Zug-Sound ab, abhängig von der Farbe der Figur.
     */
    public void playMoveSound() {
        // Nutze die Sound-Klasse, um den entsprechenden Zug-Sound abzuspielen
        if (color == 0) {  // Weiß
            Sound.play(Sound.SoundType.WHITE_MOVE);
        } else {  // Schwarz
            Sound.play(Sound.SoundType.BLACK_MOVE);
        }
    }

    /**
     * Spielt den Schlag-Sound ab, abhängig von der Farbe der Figur.
     */
    public void playCaptureSound() {
        // Nutze die Sound-Klasse, um den entsprechenden Schlag-Sound abzuspielen
        if (color == 0) {  // Weiß
            Sound.play(Sound.SoundType.WHITE_TAKES);
        } else {  // Schwarz
            Sound.play(Sound.SoundType.BLACK_CAPTURES);
        }
    }

    /**
     * Gibt das Bild der Figur zurück.
     * 
     * @return BufferedImage des Bildes
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Berechnet die x-Koordinate auf dem Bildschirm, basierend auf der Spalte und Animations-Offset.
     * 
     * @param squareSize Größe eines Schachbrettfeldes in Pixeln
     * @return x-Koordinate in Pixeln
     */
    public int getX(int squareSize) {
        return col * squareSize + animOffsetX;
    }

    /**
     * Berechnet die y-Koordinate auf dem Bildschirm, basierend auf der Zeile und Animations-Offset.
     * 
     * @param squareSize Größe eines Schachbrettfeldes in Pixeln
     * @return y-Koordinate in Pixeln
     */
    public int getY(int squareSize) {
        return row * squareSize + animOffsetY;
    }

    /**
     * Setzt den Animationsversatz (Offset) für die Darstellung der Figur.
     * 
     * @param offsetX Versatz in x-Richtung
     * @param offsetY Versatz in y-Richtung
     */
    public void setAnimOffset(int offsetX, int offsetY) {
        this.animOffsetX = offsetX;
        this.animOffsetY = offsetY;
    }

    // Getter und Setter für Spalte, Zeile und Farbe
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

    /**
     * Abstrakte Methode, die in den Unterklassen implementiert werden muss, um zu prüfen,
     * ob ein Zug an eine neue Position gültig ist.
     * 
     * @param newCol Zielspalte
     * @param newRow Zielzeile
     * @param board  Das Schachbrett als 2D-Array von Piece-Objekten
     * @return true, wenn der Zug gültig ist, sonst false
     */
    public abstract boolean isValidMove(int newCol, int newRow, Piece[][] board);

    /**
     * Überprüft, ob die angegebene Figur dieselbe Farbe wie diese Figur hat.
     * 
     * @param targetPiece Die zu überprüfende Figur
     * @return true, wenn beide Figuren dieselbe Farbe haben, sonst false
     */
    public boolean isSameColor(Piece targetPiece) {
        return targetPiece != null && this.color == targetPiece.getColor();
    }

    /**
     * Überprüft, ob auf dem Weg zwischen zwei Feldern eine Figur steht.
     * 
     * Dies wird z.B. bei Figuren wie Dame, Turm und Läufer genutzt, die auf ihrem Weg nicht übersprungen werden dürfen.
     * 
     * @param startCol Startspalte
     * @param startRow Startzeile
     * @param endCol   Endspalte
     * @param endRow   Endzeile
     * @param board    Das Schachbrett als 2D-Array von Piece-Objekten
     * @return true, wenn der Pfad blockiert ist, sonst false
     */
    protected boolean isPathBlocked(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int deltaCol = Integer.signum(endCol - startCol);
        int deltaRow = Integer.signum(endRow - startRow);
        int col = startCol + deltaCol;
        int row = startRow + deltaRow;

        // Gehe alle Felder zwischen Start- und Endposition durch
        while (col != endCol || row != endRow) {
            if (board[row][col] != null) {
                return true;  // Ein Feld ist besetzt
            }
            col += deltaCol;
            row += deltaRow;
        }
        return false;
    }
}
