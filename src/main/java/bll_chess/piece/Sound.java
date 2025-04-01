package main.java.bll_chess.piece;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Die Klasse Sound ermoeglicht das Abspielen von Soundeffekten im Spiel.
 */
public class Sound {

    /**
     * Enum, das die verschiedenen Soundtypen und zugehoerige Dateinamen definiert.
     */
    public enum SoundType {
        BLACK_CAPTURES("Black_captures.mp3"),
        BLACK_CASTLE("Black_castle.mp3"),
        BLACK_CHECK("Black_check.mp3"),
        BLACK_MOVE("Black_move.mp3"),
        CHECKMATE("checkmate.mp3"),
        GAME_OVER_STALEMATE("game_over_stalemate.mp3"),
        GAME_OVER("game_over.mp3"),
        START_GAME("Start_game.mp3"),
        WHITE_CASTLE("Withe_castle.mp3"),
        WHITE_CHECK("Withe_check.mp3"),
        WHITE_MOVE("Withe_move.mp3"),
        WHITE_TAKES("Withe_takes.mp3");

        private final String filename;

        SoundType(String filename) {
            this.filename = filename;
        }

        /**
         * Gibt den Dateinamen des Sounds zurueck.
         * 
         * @return Dateiname als String
         */
        public String getFilename() {
            return filename;
        }
    }

    /**
     * Spielt den angegebenen Sound ab.
     * 
     * Dabei wird der Sound in einem eigenen Thread abgespielt, um den Spielablauf nicht zu blockieren.
     * 
     * @param soundType Der Typ des Sounds, der abgespielt werden soll
     */
    public static void play(SoundType soundType) {
        String filePath = "src/main/resources/sounds/" + soundType.getFilename();
        
        try {
            // Oeffne einen gepufferten InputStream zur Sounddatei
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
            AdvancedPlayer player = new AdvancedPlayer(bis);
            
            // Starte einen neuen Thread, um den Sound asynchron abzuspielen
            new Thread(() -> {
                try {
                    player.play();  // Sound abspielen
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bis.close();  // Schliesse den Stream, wenn der Sound abgespielt wurde
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException | JavaLayerException e) {
            System.err.println("Fehler beim Abspielen der Datei: " + filePath);
            e.printStackTrace();
        }
    }
}
