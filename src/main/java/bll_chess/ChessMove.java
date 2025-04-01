package main.java.bll_chess;

import main.java.bll_chess.piece.*;

/**
 * Die Klasse ChessMove repraesentiert einen einzelnen Zug im Schach.
 * Sie speichert Informationen ueber die bewegte Figur, die Ausgangs- und Zielposition
 * sowie eine eventuell geschlagene Figur.
 */
public class ChessMove {
    // Die Figur, die den Zug ausfuehrt
    private Piece piece;
    // Ausgangsposition (Reihe und Spalte)
    private int fromRow, fromCol;
    // Zielposition (Reihe und Spalte)
    private int toRow, toCol;
    // Falls vorhanden, die Figur, die im Zug geschlagen wurde
    private Piece capturedPiece;

    /**
     * Konstruktor fuer einen Schachzug.
     *
     * @param piece       Die Figur, die gezogen wird
     * @param fromRow     Die Ausgangsreihe
     * @param fromCol     Die Ausgangsspalte
     * @param toRow       Die Zielreihe
     * @param toCol       Die Zielspalte
     * @param captured    Die geschlagene Figur (falls vorhanden, sonst null)
     */
    public ChessMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, Piece captured) {
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = captured;
    }
    
    /**
     * Gibt die geschlagene Figur zurueck.
     *
     * @return Die geschlagene Figur oder null, falls keine Figur geschlagen wurde.
     */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    /**
     * Gibt die Ausgangsspalte zurueck.
     *
     * @return Die Spalte, von der die Figur gezogen wurde.
     */
    public int getFromCol() {
        return fromCol;
    }

    /**
     * Gibt die Ausgangsreihe zurueck.
     *
     * @return Die Reihe, von der die Figur gezogen wurde.
     */
    public int getFromRow() {
        return fromRow;
    }

    /**
     * Gibt die bewegte Figur zurueck.
     *
     * @return Das Piece, das den Zug ausgefuehrt hat.
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Gibt die Zielspalte zurueck.
     *
     * @return Die Spalte, in die die Figur gezogen wurde.
     */
    public int getToCol() {
        return toCol;
    }

    /**
     * Gibt die Zielreihe zurueck.
     *
     * @return Die Reihe, in die die Figur gezogen wurde.
     */
    public int getToRow() {
        return toRow;
    }
}
