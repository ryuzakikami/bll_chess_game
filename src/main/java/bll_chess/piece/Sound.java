package main.java.bll_chess.piece;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Sound {

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

        public String getFilename() {
            return filename;
        }
    }

    public static void play(SoundType soundType) {
        String filePath = "src/main/resources/sounds/" + soundType.getFilename();
        
        try {
            // Open the stream and keep it open during playback
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
            AdvancedPlayer player = new AdvancedPlayer(bis);
            
            // Play the sound on a separate thread
            new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bis.close();  // Ensure the stream is closed after the sound finishes
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
