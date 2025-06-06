package main.java.bll_chess;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.*;

public class ArduinoConnector {
    private SerialPort serialPort;
    private BufferedReader input;
    private GamePanel gamePanel;
    private StringBuilder dataBuffer = new StringBuilder();

    public ArduinoConnector(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        // Starte ein Hintergrund‐Thread, damit die GUI sofort angezeigt werden kann:
        new Thread(this::initializeSerialConnection).start();
    }

    private void initializeSerialConnection() {
        String portName = "";

        try {
            // 1) Verfügbare serielle Ports ermitteln
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("Verfügbare Ports: " + Arrays.toString(ports));

            // 2) Nach "Arduino Mega 2560" suchen und COM-Namen extrahieren
            for (SerialPort port : ports) {
                String portStr = port.toString();
                System.out.println(portStr);
                if (portStr.contains("Arduino Mega 2560")) {
                    Pattern p = Pattern.compile("\\(([^)]+)\\)$");
                    Matcher m = p.matcher(portStr);
                    if (m.find()) {
                        portName = m.group(1); // z.B. "COM3"
                        System.out.println("Gefundener COM-Port: " + portName);
                        break;
                    }
                }
            }

            // 3) Abbruch, falls kein Arduino Mega gefunden wurde
            if (portName.isEmpty()) {
                System.out.println("Kein Arduino Mega 2560 gefunden.");
                return;
            }

            // 4) Seriellen Port öffnen
            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(115200);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

            if (serialPort.openPort()) {
                System.out.println("Erfolgreich verbunden mit " + portName);
               
            }
            else{
                 System.err.println("Fehler beim Öffnen des Ports " + portName);
                return;
            }
           

            // 5) Input-Stream initialisieren
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            // 6) Listener hinzufügen
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        try {
                            String line = input.readLine();
                            if (line == null) return;

                            System.out.println("Daten empfangen: " + line);
                            dataBuffer.append(line);

                            int startIndex = dataBuffer.indexOf("START,");
                            int endIndex   = dataBuffer.indexOf(",END");
                            if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
                                String completeMessage = dataBuffer.substring(startIndex, endIndex + 4);
                                processCompleteSensorData(completeMessage);
                                dataBuffer.delete(0, endIndex + 4);
                            }

                            if (dataBuffer.length() > 1000) {
                                System.out.println("Pufferüberlauf, wird geleert");
                                dataBuffer.setLength(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // 7) READY‐Signal an Arduino senden – nur, wenn der Port noch geöffnet ist:
            Thread.sleep(2000);
            if (serialPort.isOpen()) {
                try (OutputStream output = serialPort.getOutputStream()) {
                    output.write("READY\n".getBytes());
                    output.flush();
                    System.out.println("READY-Signal an Arduino gesendet");
                } catch (Exception writeEx) {
                    System.err.println(writeEx.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verarbeitung einer vollständigen Sensordaten‐Nachricht (START,…,END).
     */
    private void processCompleteSensorData(String data) {
        if (data.startsWith("START,") && data.endsWith(",END")) {
            gamePanel.processSensorData(data);
        } else {
            System.out.println("Unerwartetes Datenformat: " + data);
        }
    }

    /**
     * Schließt die serielle Verbindung wieder.
     */
    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Serieller Port geschlossen");
        }
    }
}
