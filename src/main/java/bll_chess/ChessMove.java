package main.java.bll_chess;

import main.java.bll_chess.piece.*;

/**
 * Die Klasse ChessMove repräsentiert einen einzelnen Zug im Schach.
 * Sie speichert Informationen über die bewegte Figur, die Ausgangs- und Zielposition
 * sowie eine eventuell geschlagene Figur.
 */
public class ChessMove {
    // Die Figur, die den Zug ausführt
    private Piece piece;
    // Ausgangsposition (Reihe und Spalte)
    private int fromRow, fromCol;
    // Zielposition (Reihe und Spalte)
    private int toRow, toCol;
    // Falls vorhanden, die Figur, die im Zug geschlagen wurde
    private Piece capturedPiece;

    /**
     * Konstruktor für einen Schachzug.
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
    
    // Getter-Methoden

    /**
     * Gibt die geschlagene Figur zurück.
     *
     * @return Die geschlagene Figur oder null, falls keine Figur geschlagen wurde.
     */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    /**
     * Gibt die Ausgangsspalte zurück.
     *
     * @return Die Spalte, von der die Figur gezogen wurde.
     */
    public int getFromCol() {
        return fromCol;
    }

    /**
     * Gibt die Ausgangsreihe zurück.
     *
     * @return Die Reihe, von der die Figur gezogen wurde.
     */
    public int getFromRow() {
        return fromRow;
    }

    /**
     * Gibt die bewegte Figur zurück.
     *
     * @return Das Piece, das den Zug ausgeführt hat.
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Gibt die Zielspalte zurück.
     *
     * @return Die Spalte, in die die Figur gezogen wurde.
     */
    public int getToCol() {
        return toCol;
    }

    /**
     * Gibt die Zielreihe zurück.
     *
     * @return Die Reihe, in die die Figur gezogen wurde.
     */
    public int getToRow() {
        return toRow;
    }
}
