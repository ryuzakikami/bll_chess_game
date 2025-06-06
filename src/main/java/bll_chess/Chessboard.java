package main.java.bll_chess;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import main.java.bll_chess.piece.*;

public class Chessboard implements Serializable {
    public static final int MAX_COL = 8;         // Anzahl der Spalten
    public static final int MAX_ROW = 8;         // Anzahl der Reihen
    public static final int SQUARE_SIZE = 100;   // Groesse eines Feldes in Pixeln
    public static final int MARGIN = 40;         // Abstand um das Schachbrett

    // Das Schachbrett als 2D-Array von Figuren.
    // board[0] entspricht der untersten Zeile (visuelle Reihe 1) und board[7] der obersten (visuelle Reihe 8).
    private Piece[][] board;
    // Aktueller Spieler (1 = Weiss, 0 = Schwarz)
    private int currentPlayer = 1;
    // Flag, ob der aktuelle Spieler im Schach steht
    private boolean isCheck = false;
    // Historie der Zuege fuer Rueckgaengig-Funktionalitaet
    private Stack<ChessMove> moveHistory = new Stack<>();
    // Flag, ob eine Bauernumwandlung (Promotion) aussteht
    private boolean promotionPending = false;
    
    // Fuer Zobrist-Hashing:
    // Dimensionen: [Farbe (0/1)][Figurentyp (0:Pawn,1:Knight,2:Bishop,3:Rook,4:Queen,5:King)][Feld (0-63)]
    private long[][][] zobristTable;
    // Aktueller Zobrist-Hash der Stellung
    private long currentZobristHash = 0;
    // Fuer Wiederholungspruefung: Historie der Stellungen (Hashes)
    private ArrayList<Long> positionHistory = new ArrayList<>();
    // Halbzugzaehler fuer die 50-Zuege-Regel (wird bei Bauernzug oder Schlag zurueckgesetzt)
    private int halfMoveClock = 0;
    
    /**
     * Konstruktor: Initialisiert das Schachbrett, die Zobrist-Tabelle und speichert
     * die Ausgangsstellung.
     */
    public Chessboard() {
        initializeBoard();
        initializeZobrist();
        // Speichere die Ausgangsstellung (fuer Wiederholungspruefung)
        positionHistory.add(currentZobristHash);
    }
    
    private void initializeBoard() {
        board = new Piece[MAX_ROW][MAX_COL];
        placePieces(0, 1, 7, 6);
        // Setze den Halbzugzaehler zurueck
        halfMoveClock = 0;
    }
    
    /**
     * Platziert die Figuren auf dem Brett.
     *
     * @param whiteRow     Zeile fuer weisse Hauptfiguren (interner Index)
     * @param whitePawnRow Zeile fuer weisse Bauern (interner Index)
     * @param blackRow     Zeile fuer schwarze Hauptfiguren (interner Index)
     * @param blackPawnRow Zeile fuer schwarze Bauern (interner Index)
     */
    private void placePieces(int whiteRow, int whitePawnRow, int blackRow, int blackPawnRow) {
        // Weisse Figuren (untere Reihe, board[0])
        board[whiteRow][0] = new Rook(1, 0, whiteRow);
        board[whiteRow][1] = new Knight(1, 1, whiteRow);
        board[whiteRow][2] = new Bishop(1, 2, whiteRow);
        board[whiteRow][3] = new Queen(1, 3, whiteRow);
        board[whiteRow][4] = new King(1, 4, whiteRow);
        board[whiteRow][5] = new Bishop(1, 5, whiteRow);
        board[whiteRow][6] = new Knight(1, 6, whiteRow);
        board[whiteRow][7] = new Rook(1, 7, whiteRow);
        // Weisse Bauern in board[1]
        for (int col = 0; col < MAX_COL; col++) {
            board[whitePawnRow][col] = new Pawn(1, col, whitePawnRow);
        }
        
        // Schwarze Figuren (obere Reihe, board[7])
        board[blackRow][0] = new Rook(0, 0, blackRow);
        board[blackRow][1] = new Knight(0, 1, blackRow);
        board[blackRow][2] = new Bishop(0, 2, blackRow);
        board[blackRow][3] = new Queen(0, 3, blackRow);
        board[blackRow][4] = new King(0, 4, blackRow);
        board[blackRow][5] = new Bishop(0, 5, blackRow);
        board[blackRow][6] = new Knight(0, 6, blackRow);
        board[blackRow][7] = new Rook(0, 7, blackRow);
        // Schwarze Bauern in board[6]
        for (int col = 0; col < MAX_COL; col++) {
            board[blackPawnRow][col] = new Pawn(0, col, blackPawnRow);
        }
    }
    
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
     * Aktualisiert den Zobrist-Hash fuer eine bestimmte Position.
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
     * Fuehrt einen Zug aus und aktualisiert alle relevanten Zustande (Zobrist-Hash, Zug-Historie,
     * En Passant, Bauernumwandlung, Rochade, Halbzugzaehler, etc.).
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
        
        // Setze En Passant fuer alle Bauern zurueck
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
        // Fuehre den Zug auf dem Brett aus
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
        // Aktualisiere den Hash: Fuege die Figur an der neuen Position hinzu
        updateZobrist(movingPiece, toRow, toCol);
        if (movingPiece != null) {
            // Aktualisiere die Position der Figur
            movingPiece.setRow(toRow);
            movingPiece.setCol(toCol);
            // Bauernumwandlung (Promotion):
            if (movingPiece instanceof Pawn) {
                handlePawnPromotion(movingPiece, toRow, toCol);
            }
            // Wenn ein Bauer zwei Felder vorwaerts geht, setze En Passant
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
        // Aktualisiere den Halbzugzaehler
        if (movingPiece instanceof Pawn || captured != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        // Speichere die aktuelle Stellungshash fuer Wiederholungspruefung
        positionHistory.add(currentZobristHash);
        // Pruefe, ob der aktuelle Spieler im Schach steht
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
        // Weiss wird befoerdert, wenn board[7] erreicht wird, Schwarz wenn board[0] erreicht wird.
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
                    "Waehle die Figur, in die der Bauer umgewandelt werden soll:",
                    "Befoerderung",
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
     * @param fromRow Startreihe des Koenigs
     * @param fromCol Startspalte des Koenigs
     * @param toRow   Zielreihe des Koenigs
     * @param toCol   Zielspalte des Koenigs
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
     * Macht den letzten Zug rueckgaengig.
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
     * Setzt die Position des Turms nach einer Rueckgaengigmachung einer Rochade zurueck.
     *
     * @param move Der Zug, der rueckgaengig gemacht wurde
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
     * Gibt das aktuelle Schachbrett (2D-Array) zurueck.
     *
     * @return Das Schachbrett
     */
    public Piece[][] getBoard() {
        return board;
    }
    
    /**
     * Gibt den aktuellen Spieler zurueck.
     *
     * @return 0 fuer Weiss, 1 fuer Schwarz
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * Gibt zurueck, ob eine Bauernumwandlung (Promotion) noch aussteht.
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
        currentPlayer = (currentPlayer == 1) ? 0 : 1;
        System.out.println("[DEBUG] Spieler gewechselt -> Neuer Spieler: " 
            + (currentPlayer == 1 ? "Weiss" : "Schwarz"));
    }
    
    /**
     * Ueberprueft, ob der uebergebene Zug vom richtigen Spieler ausgefuehrt wird.
     *
     * @param piece Die Figur, die gezogen werden soll
     * @return true, wenn die Figur dem aktuellen Spieler gehoert, sonst false
     */
    public boolean isValidTurn(Piece piece) {
        if (piece == null) {
            System.out.println("isValidTurn: Piece ist null");
            return false;
        }
        System.out.println("isValidTurn: Piece-Farbe = " + piece.getColor() + ", aktueller Spieler = " + currentPlayer);
        if (piece.getColor() != currentPlayer) {
            System.out.println("isValidTurn: Falscher Spieler am Zug!");
            return false;
        }
        return true;
    }
    
    public boolean isKingInCheck(int player) {
        King king = null;
        // Finde den Koenig des angegebenen Spielers
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
        // Ueberpruefe alle gegnerischen Figuren, ob eine den Koenig angreift
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
     * Prueft, ob der angegebene Spieler schachmatt ist.
     *
     * @param player Der Spieler (0 = Weiss, 1 = Schwarz)
     * @return true, wenn kein gueltiger Zug den Koenig aus dem Schach befreien kann, sonst false
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
     * Prueft, ob die aktuelle Stellung mindestens dreifach wiederholt wurde.
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
     * Prueft die 50-Zuege-Regel.
     *
     * @return true, wenn seit dem letzten Bauernzug oder Schlag 100 Halbzuege vergangen sind, sonst false
     */
    public boolean isFiftyMoveRule() {
        return halfMoveClock >= 100;
    }
    
    /**
     * Prueft, ob ein geplanter Zug den Koenig im Schach belaesst.
     * Temporaer wird der Zug durchgefuehrt, die Stellung geprueft und dann wieder zurueckgesetzt.
     *
     * @param fromRow Startreihe
     * @param fromCol Startspalte
     * @param toRow   Zielreihe
     * @param toCol   Zielspalte
     * @return true, wenn der Zug den Koenig im Schach laesst, sonst false
     */
    boolean isMoveLeavingKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece tempPiece = board[toRow][toCol];
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;
        
        boolean isInCheck = isKingInCheck(currentPlayer);
        
        // Stelle den urspruenglichen Zustand wieder her
        board[fromRow][fromCol] = board[toRow][toCol];
        board[toRow][toCol] = tempPiece;
        
        return isInCheck;
    }
    
    /**
     * Zeichnet das Schachbrett samt Feldern, Figuren, Rahmen und Beschriftung.
     * @param g2 Graphics2D-Objekt, das zum Zeichnen verwendet wird
     */
    public void draw(Graphics2D g2) {
        int margin = MARGIN;
        int boardSize = MAX_COL * SQUARE_SIZE;
        
        // Zeichne die Felder
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                Color fieldColor = ((row + col) % 2 == 0)
                        ? new Color(210, 165, 125)
                        : new Color(175, 115, 70);
                int x = margin + col * SQUARE_SIZE;
                int y = margin + (MAX_ROW - 1 - row) * SQUARE_SIZE;
                g2.setColor(fieldColor);
                g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
        
        // Zeichne die Figuren
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
        // Reihenbeschriftung
        for (int row = 0; row < MAX_ROW; row++) {
            String label = String.valueOf(row + 1);
            int y = margin + (MAX_ROW - 1 - row) * SQUARE_SIZE + SQUARE_SIZE / 2 + 5;
            int xRight = margin + boardSize + 5;
            int xLeft = margin - 20;
            g2.drawString(label, xRight, y);
            g2.drawString(label, xLeft, y);
        }
    }
    // In Chessboard.java
    public void setCurrentPlayer(int player) {
    this.currentPlayer = player;
}

}
