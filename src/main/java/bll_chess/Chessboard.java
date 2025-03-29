package main.java.bll_chess;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import main.java.bll_chess.piece.*;

/**
 * Die Klasse Chessboard repräsentiert das Schachbrett und verwaltet
 * alle Spiellogiken wie Zugvalidierung, Zobrist-Hashing, Zugrücknahme,
 * Rochade, Bauernumwandlung, Wiederholungsprüfung, 50-Züge-Regel, etc.
 *
 * Neu: Das interne 2D-Array board wird so strukturiert, dass:
 * - board[0][*] die unterste (visuelle) Reihe (Reihe 1, wo Weiß beginnt) darstellt,
 * - board[7][*] die oberste (visuelle) Reihe (Reihe 8, wo Schwarz steht).
 *
 * Die GUI-Darstellung bleibt unverändert (Weiß unten, Schwarz oben).
 */
public class Chessboard implements Serializable {
    public static final int MAX_COL = 8;         // Anzahl der Spalten
    public static final int MAX_ROW = 8;         // Anzahl der Reihen
    public static final int SQUARE_SIZE = 100;   // Größe eines Feldes in Pixeln
    public static final int MARGIN = 40;         // Abstand um das Schachbrett

    // Das Schachbrett als 2D-Array von Figuren.
    // board[0] entspricht der untersten Zeile (visuelle Reihe 1) und board[7] der obersten (visuelle Reihe 8).
    private Piece[][] board;
    // Aktueller Spieler (0 = Weiß, 1 = Schwarz)
    private int currentPlayer = 0;
    // Flag, ob der aktuelle Spieler im Schach steht
    private boolean isCheck = false;
    // Historie der Züge für Rückgängig-Funktionalität
    private Stack<ChessMove> moveHistory = new Stack<>();
    // Flag, ob eine Bauernumwandlung (Promotion) aussteht
    private boolean promotionPending = false;

    // Für Zobrist-Hashing:
    // Dimensionen: [Farbe (0/1)][Figurentyp (0:Pawn,1:Knight,2:Bishop,3:Rook,4:Queen,5:King)][Feld (0-63)]
    private long[][][] zobristTable;
    // Aktueller Zobrist-Hash der Stellung
    private long currentZobristHash = 0;
    // Für Wiederholungsprüfung: Historie der Stellungen (Hashes)
    private ArrayList<Long> positionHistory = new ArrayList<>();
    // Halbzugzähler für die 50-Züge-Regel (wird bei Bauernzug oder Schlag zurückgesetzt)
    private int halfMoveClock = 0;

    /**
     * Konstruktor: Initialisiert das Schachbrett, die Zobrist-Tabelle und speichert
     * die Ausgangsstellung.
     */
    public Chessboard() {
        initializeBoard();
        initializeZobrist();
        // Speichere die Ausgangsstellung (für Wiederholungsprüfung)
        positionHistory.add(currentZobristHash);
    }

    /**
     * Initialisiert das Schachbrett und platziert alle Figuren.
     * 
     * Neu: Die interne Darstellung entspricht der visuellen Anordnung:
     * - Weiße Hauptfiguren in board[0] (Reihe 1) und weiße Bauern in board[1].
     * - Schwarze Bauern in board[6] und schwarze Hauptfiguren in board[7].
     */
    private void initializeBoard() {
        board = new Piece[MAX_ROW][MAX_COL];
        // Platziere die Figuren an den Startpositionen:
        // Weiße Figuren (unten): Hauptfiguren in Reihe 0, Bauern in Reihe 1.
        // Schwarze Figuren (oben): Bauern in Reihe 6, Hauptfiguren in Reihe 7.
        placePieces(0, 1, 7, 6);
        // Setze den Halbzugzähler zurück
        halfMoveClock = 0;
    }

    /**
     * Platziert die Figuren auf dem Brett.
     *
     * @param whiteRow     Zeile für weiße Hauptfiguren (interner Index)
     * @param whitePawnRow Zeile für weiße Bauern (interner Index)
     * @param blackRow     Zeile für schwarze Hauptfiguren (interner Index)
     * @param blackPawnRow Zeile für schwarze Bauern (interner Index)
     */
    private void placePieces(int whiteRow, int whitePawnRow, int blackRow, int blackPawnRow) {
        // Weiße Figuren (untere Reihe, board[0])
        board[whiteRow][0] = new Rook(0, 0, whiteRow);
        board[whiteRow][1] = new Knight(0, 1, whiteRow);
        board[whiteRow][2] = new Bishop(0, 2, whiteRow);
        board[whiteRow][3] = new Queen(0, 3, whiteRow);
        board[whiteRow][4] = new King(0, 4, whiteRow);
        board[whiteRow][5] = new Bishop(0, 5, whiteRow);
        board[whiteRow][6] = new Knight(0, 6, whiteRow);
        board[whiteRow][7] = new Rook(0, 7, whiteRow);
        // Weiße Bauern in board[1]
        for (int col = 0; col < MAX_COL; col++) {
            board[whitePawnRow][col] = new Pawn(0, col, whitePawnRow);
        }

        // Schwarze Figuren (obere Reihe, board[7])
        board[blackRow][0] = new Rook(1, 0, blackRow);
        board[blackRow][1] = new Knight(1, 1, blackRow);
        board[blackRow][2] = new Bishop(1, 2, blackRow);
        board[blackRow][3] = new Queen(1, 3, blackRow);
        board[blackRow][4] = new King(1, 4, blackRow);
        board[blackRow][5] = new Bishop(1, 5, blackRow);
        board[blackRow][6] = new Knight(1, 6, blackRow);
        board[blackRow][7] = new Rook(1, 7, blackRow);
        // Schwarze Bauern in board[6]
        for (int col = 0; col < MAX_COL; col++) {
            board[blackPawnRow][col] = new Pawn(1, col, blackPawnRow);
        }
    }

    /**
     * Initialisiert die Zobrist-Tabelle mit zufälligen Long-Werten.
     * Diese Tabelle wird verwendet, um schnell einen eindeutigen Hash für jede Stellung zu berechnen.
     */
    private void initializeZobrist() {
        Random rand = new Random();
        zobristTable = new long[2][6][MAX_ROW * MAX_COL];
        for (int color = 0; color < 2; color++) {
            for (int type = 0; type < 6; type++) {
                for (int pos = 0; pos < MAX_ROW * MAX_COL; pos++) {
                    zobristTable[color][type][pos] = rand.nextLong();
                }
            }
        }
        // Berechne den Hash der Ausgangsstellung
        currentZobristHash = 0;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    int type = getPieceType(piece);
                    int pos = row * MAX_COL + col;
                    currentZobristHash ^= zobristTable[piece.getColor()][type][pos];
                }
            }
        }
    }

    /**
     * Liefert den Typ der Figur als int-Wert.
     *
     * @param piece Die zu überprüfende Figur
     * @return 0 für Bauer, 1 für Springer, 2 für Läufer, 3 für Turm, 4 für Dame, 5 für König; ansonsten -1
     */
    private int getPieceType(Piece piece) {
        if (piece instanceof Pawn) return 0;
        if (piece instanceof Knight) return 1;
        if (piece instanceof Bishop) return 2;
        if (piece instanceof Rook) return 3;
        if (piece instanceof Queen) return 4;
        if (piece instanceof King) return 5;
        return -1;
    }

    /**
     * Aktualisiert den Zobrist-Hash für eine bestimmte Position.
     *
     * @param piece Die betroffene Figur
     * @param row   Zeile der Position
     * @param col   Spalte der Position
     */
    private void updateZobrist(Piece piece, int row, int col) {
        if (piece == null) return;
        int type = getPieceType(piece);
        int pos = row * MAX_COL + col;
        currentZobristHash ^= zobristTable[piece.getColor()][type][pos];
    }

    /**
     * Führt einen Zug aus und aktualisiert alle relevanten Zustände (Zobrist-Hash, Zug-Historie,
     * En Passant, Bauernumwandlung, Rochade, Halbzugzähler, etc.).
     *
     * @param fromRow    Startreihe der Figur
     * @param fromCol    Startspalte der Figur
     * @param toRow      Zielreihe der Figur
     * @param toCol      Zielspalte der Figur
     * @param switchTurn Flag, ob der Spielerwechsel nach dem Zug erfolgen soll
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, boolean switchTurn) {
        Piece movingPiece = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];

        // Setze En Passant für alle Bauern zurück
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COL; c++) {
                if (board[r][c] instanceof Pawn) {
                    ((Pawn) board[r][c]).setEnPassantEligible(false);
                }
            }
        }

        // En Passant: Falls ein Bauer diagonal in ein leeres Feld zieht,
        // wird der gegnerische Bauer auf der gleichen Reihe (dem Ausgangsfeld) geschlagen.
        if (movingPiece instanceof Pawn && Math.abs(toCol - fromCol) == 1 && board[toRow][toCol] == null) {
            int capturedPawnRow = fromRow; // Der geschlagene Bauer steht in der Ausgangsreihe
            captured = board[capturedPawnRow][toCol];
            updateZobrist(captured, capturedPawnRow, toCol);
            board[capturedPawnRow][toCol] = null;
        }

        // Entferne die Figur von der alten Position im Hash
        updateZobrist(movingPiece, fromRow, fromCol);
        if (captured != null) {
            updateZobrist(captured, toRow, toCol);
        }

        // Speichere den Zug in der Historie
        moveHistory.push(new ChessMove(movingPiece, fromRow, fromCol, toRow, toCol, captured));

        // Führe den Zug auf dem Brett aus
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;

        // Aktualisiere den Hash: Füge die Figur an der neuen Position hinzu
        updateZobrist(movingPiece, toRow, toCol);

        if (movingPiece != null) {
            // Aktualisiere die Position der Figur
            movingPiece.setRow(toRow);
            movingPiece.setCol(toCol);

            // Bauernumwandlung (Promotion):
            // Weiß fördert beim Erreichen von board[7], Schwarz beim Erreichen von board[0].
            if (movingPiece instanceof Pawn) {
                handlePawnPromotion(movingPiece, toRow, toCol);
            }

            // Wenn ein Bauer zwei Felder vorwärts geht, setze En Passant
            int rowDifference = toRow - fromRow;
            if (movingPiece instanceof Pawn && Math.abs(rowDifference) == 2) {
                ((Pawn) movingPiece).setEnPassantEligible(true);
            }

            // Rochade
            if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
                handleCastling(fromRow, fromCol, toRow, toCol);
                ((King) movingPiece).markAsMoved();
            } else if (movingPiece instanceof King) {
                ((King) movingPiece).markAsMoved();
            }
        }

        // Aktualisiere den Halbzugzähler
        if (movingPiece instanceof Pawn || captured != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        // Speichere die aktuelle Stellungshash für Wiederholungsprüfung
        positionHistory.add(currentZobristHash);

        // Prüfe, ob der aktuelle Spieler im Schach steht
        isCheck = isKingInCheck(currentPlayer);
        if (switchTurn) {
            switchPlayer();
        }
    }

    /**
     * Behandelt die Bauernumwandlung (Promotion).
     *
     * @param pawn  Der Bauer, der umgewandelt werden soll
     * @param toRow Zielreihe
     * @param toCol Zielspalte
     */
    private void handlePawnPromotion(Piece pawn, int toRow, int toCol) {
        // Bei der neuen Logik:
        // Weißer Bauer wird befördert, wenn er board[7] erreicht, schwarzer, wenn er board[0] erreicht.
        if ((pawn.getColor() == 0 && toRow == 7) || (pawn.getColor() == 1 && toRow == 0)) {
            promotionPending = true;
            board[toRow][toCol] = null;
            String colorPrefix = (pawn.getColor() == 0) ? "white" : "black";
            ImageIcon queenIcon = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Queen.png");
            ImageIcon rookIcon = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Rook.png");
            ImageIcon bishopIcon = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Bishop.png");
            ImageIcon knightIcon = new ImageIcon("src/main/resources/pieces/" + colorPrefix + "Knight.png");
            Object[] promotionOptions = {queenIcon, rookIcon, bishopIcon, knightIcon};

            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Wähle die Figur, in die der Bauer umgewandelt werden soll:",
                    "Beförderung",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    promotionOptions,
                    promotionOptions[0]
            );

            Piece promotedPiece;
            switch (choice) {
                case 0:
                    promotedPiece = new Queen(pawn.getColor(), toCol, toRow);
                    break;
                case 1:
                    promotedPiece = new Rook(pawn.getColor(), toCol, toRow);
                    break;
                case 2:
                    promotedPiece = new Bishop(pawn.getColor(), toCol, toRow);
                    break;
                case 3:
                    promotedPiece = new Knight(pawn.getColor(), toCol, toRow);
                    break;
                default:
                    promotedPiece = new Queen(pawn.getColor(), toCol, toRow);
                    break;
            }
            board[toRow][toCol] = promotedPiece;
            updateZobrist(promotedPiece, toRow, toCol);
            promotionPending = false;
        }
    }

    /**
     * Behandelt die Rochade.
     *
     * @param fromRow Startreihe des Königs
     * @param fromCol Startspalte des Königs
     * @param toRow   Zielreihe des Königs
     * @param toCol   Zielspalte des Königs
     */
    private void handleCastling(int fromRow, int fromCol, int toRow, int toCol) {
        if (!(board[fromRow][fromCol] instanceof King)) {
            return;
        }

        King king = (King) board[fromRow][fromCol];
        Rook rook = null;
        int rookCol = (toCol > fromCol) ? 7 : 0;

        if (king.hasMoved() || (rook = (Rook) board[fromRow][rookCol]) == null || rook.hasMoved()) {
            return;
        }

        int step = (toCol > fromCol) ? 1 : -1;
        for (int col = fromCol + step; col != toCol; col += step) {
            if (board[fromRow][col] != null) {
                return;
            }
        }

        if (isKingInCheck(currentPlayer) || isMoveLeavingKingInCheck(fromRow, fromCol, toRow, toCol)) {
            return;
        }

        board[toRow][toCol] = king;
        board[fromRow][fromCol] = null;
        king.setCol(toCol);
        king.markAsMoved();

        board[toRow][toCol - step] = rook;
        board[fromRow][rookCol] = null;
        rook.setCol(toCol - step);
        rook.markAsMoved();

        updateZobrist(king, toRow, toCol);
        updateZobrist(rook, toRow, toCol - step);
    }

    /**
     * Macht den letzten Zug rückgängig.
     */
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            ChessMove lastMove = moveHistory.pop();
            updateZobrist(lastMove.getPiece(), lastMove.getToRow(), lastMove.getToCol());
            board[lastMove.getFromRow()][lastMove.getFromCol()] = lastMove.getPiece();
            board[lastMove.getToRow()][lastMove.getToCol()] = lastMove.getCapturedPiece();
            updateZobrist(lastMove.getPiece(), lastMove.getFromRow(), lastMove.getFromCol());
            if (lastMove.getCapturedPiece() != null) {
                updateZobrist(lastMove.getCapturedPiece(), lastMove.getToRow(), lastMove.getToCol());
            }
            lastMove.getPiece().setRow(lastMove.getFromRow());
            lastMove.getPiece().setCol(lastMove.getFromCol());

            if (lastMove.getPiece() instanceof King && Math.abs(lastMove.getToCol() - lastMove.getFromCol()) == 2) {
                resetRookPosition(lastMove);
            }

            switchPlayer();
        }
    }

    /**
     * Setzt die Position des Turms nach einer Rückgängigmachung einer Rochade zurück.
     *
     * @param move Der Zug, der rückgängig gemacht wurde
     */
    private void resetRookPosition(ChessMove move) {
        int rookCol = (move.getToCol() > move.getFromCol()) ? 7 : 0;
        int newRookCol = (move.getToCol() > move.getFromCol()) ? move.getToCol() - 1 : move.getToCol() + 1;

        Rook rook = (Rook) board[move.getToRow()][newRookCol];
        updateZobrist(rook, move.getToRow(), newRookCol);
        board[move.getToRow()][newRookCol] = null;
        board[move.getToRow()][rookCol] = rook;
        updateZobrist(rook, move.getToRow(), rookCol);

        if (rook != null) {
            rook.setCol(rookCol);
        }
    }

    /**
     * Gibt das aktuelle Schachbrett (2D-Array) zurück.
     *
     * @return Das Schachbrett
     */
    public Piece[][] getBoard() {
        return board;
    }

    /**
     * Gibt den aktuellen Spieler zurück.
     *
     * @return 0 für Weiß, 1 für Schwarz
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gibt zurück, ob eine Bauernumwandlung (Promotion) noch aussteht.
     *
     * @return true, wenn Promotion aussteht, sonst false
     */
    public boolean isPromotionPending() {
        return promotionPending;
    }

    /**
     * Wechselt den aktuellen Spieler.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == 0) ? 1 : 0;
    }

    /**
     * Überprüft, ob der übergebene Zug vom richtigen Spieler ausgeführt wird.
     *
     * @param piece Die Figur, die gezogen werden soll
     * @return true, wenn die Figur dem aktuellen Spieler gehört, sonst false
     */
    public boolean isValidTurn(Piece piece) {
        if (piece == null) {
            System.out.println("isValidTurn: Piece is null");
            return false;
        }
        System.out.println("isValidTurn: Piece color = " + piece.getColor() + ", currentPlayer = " + currentPlayer);
        if (piece.getColor() != currentPlayer) {
            System.out.println("isValidTurn: Falscher Spieler am Zug!");
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob der König des angegebenen Spielers im Schach steht.
     *
     * @param player Der Spieler (0 = Weiß, 1 = Schwarz)
     * @return true, wenn der König im Schach steht, sonst false
     */
    public boolean isKingInCheck(int player) {
        King king = null;
        // Finde den König des angegebenen Spielers
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece instanceof King && piece.getColor() == player) {
                    king = (King) piece;
                    break;
                }
            }
            if (king != null) break;
        }
        if (king == null) return false;

        int opponentColor = 1 - player;
        // Prüfe alle gegnerischen Figuren, ob eine den König angreift
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == opponentColor) {
                    if (piece.isValidMove(king.getCol(), king.getRow(), board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Prüft, ob der angegebene Spieler schachmatt ist.
     *
     * @param player Der Spieler (0 = Weiß, 1 = Schwarz)
     * @return true, wenn kein gültiger Zug den König aus dem Schach befreien kann, sonst false
     */
    public boolean isCheckmate(int player) {
        if (!isKingInCheck(player)) {
            return false;
        }

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == player) {
                    for (int newRow = 0; newRow < MAX_ROW; newRow++) {
                        for (int newCol = 0; newCol < MAX_COL; newCol++) {
                            if (piece.isValidMove(newCol, newRow, board)) {
                                if (!isMoveLeavingKingInCheck(row, col, newRow, newCol)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob der angegebene Spieler remis ist (Patt).
     *
     * @param player Der Spieler (0 = Weiß, 1 = Schwarz)
     * @return true, wenn der Spieler keinen gültigen Zug hat und der König nicht im Schach steht, sonst false
     */
    public boolean isStalemate(int player) {
        if (isKingInCheck(player)) {
            return false;
        }

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == player) {
                    for (int newRow = 0; newRow < MAX_ROW; newRow++) {
                        for (int newCol = 0; newCol < MAX_COL; newCol++) {
                            if (piece.isValidMove(newCol, newRow, board)) {
                                if (!isMoveLeavingKingInCheck(row, col, newRow, newCol)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob die aktuelle Stellung mindestens dreifach wiederholt wurde.
     *
     * @return true, wenn dieselbe Stellung drei oder mehr Mal erreicht wurde, sonst false
     */
    public boolean isThreefoldRepetition() {
        int count = 0;
        for (Long hash : positionHistory) {
            if (hash == currentZobristHash) {
                count++;
            }
        }
        return count >= 3;
    }

    /**
     * Prüft die 50-Züge-Regel.
     *
     * @return true, wenn seit dem letzten Bauernzug oder Schlag 100 Halbzüge vergangen sind, sonst false
     */
    public boolean isFiftyMoveRule() {
        return halfMoveClock >= 100;
    }

    /**
     * Prüft, ob ein geplanter Zug den König im Schach belässt.
     * Temporär wird der Zug durchgeführt, die Stellung geprüft und dann wieder zurückgesetzt.
     *
     * @param fromRow Startreihe
     * @param fromCol Startspalte
     * @param toRow   Zielreihe
     * @param toCol   Zielspalte
     * @return true, wenn der Zug den König im Schach lässt, sonst false
     */
    boolean isMoveLeavingKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece tempPiece = board[toRow][toCol];
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;

        boolean isInCheck = isKingInCheck(currentPlayer);

        // Stelle den ursprünglichen Zustand wieder her
        board[fromRow][fromCol] = board[toRow][toCol];
        board[toRow][toCol] = tempPiece;

        return isInCheck;
    }

    /**
     * Zeichnet das Schachbrett samt Feldern, Figuren, Rahmen und Beschriftung.
     * 
     * Neu: Da board[0] die unterste Reihe darstellt, wird beim Zeichnen umgerechnet, 
     * sodass die visuelle Darstellung (Weiß unten, Schwarz oben, Reihen 1 bis 8 von unten nach oben) erhalten bleibt.
     *
     * @param g2 Graphics2D-Objekt, das zum Zeichnen verwendet wird
     */
    public void draw(Graphics2D g2) {
        int margin = MARGIN;
        int boardSize = MAX_COL * SQUARE_SIZE;

        // Zeichne die Felder: interne Zeile 0 entspricht visual bottom (Reihe 1)
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Color fieldColor = ((row + col) % 2 == 0)
                        ? new Color(210, 165, 125)
                        : new Color(175, 115, 70);
                int x = margin + col * SQUARE_SIZE;
                // Umrechnung: board[0] soll unten erscheinen
                int y = margin + (MAX_ROW - 1 - row) * SQUARE_SIZE;
                g2.setColor(fieldColor);
                g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
            }
        }

        // Zeichne die Figuren entsprechend ihrer internen Position
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    int x = margin + col * SQUARE_SIZE;
                    int y = margin + (MAX_ROW - 1 - row) * SQUARE_SIZE;
                    g2.drawImage(piece.getImage(), x, y, SQUARE_SIZE, SQUARE_SIZE, null);
                }
            }
        }

        // Zeichne einen schwarzen Rahmen um das Brett
        g2.setColor(Color.BLACK);
        g2.drawRect(margin, margin, boardSize, boardSize);

        // Zeichne die Beschriftung der Spalten (a-h) und Reihen (1-8)
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        for (int col = 0; col < MAX_COL; col++) {
            String label = String.valueOf((char) ('a' + col));
            int x = margin + col * SQUARE_SIZE + SQUARE_SIZE / 2 - 5;
            int yBottom = margin + boardSize + 20;
            int yTop = margin - 10;
            g2.drawString(label, x, yBottom);
            g2.drawString(label, x, yTop);
        }
        // Reihenbeschriftung: Visual von unten (1) bis oben (8)
        for (int row = 0; row < MAX_ROW; row++) {
            // Da board[0] unten ist, entspricht row 0 -> 1, row 7 -> 8
            String label = String.valueOf(row + 1);
            int y = margin + (MAX_ROW - 1 - row) * SQUARE_SIZE + SQUARE_SIZE / 2 + 5;
            int xRight = margin + boardSize + 5;
            int xLeft = margin - 20;
            g2.drawString(label, xRight, y);
            g2.drawString(label, xLeft, y);
        }
    }
}
