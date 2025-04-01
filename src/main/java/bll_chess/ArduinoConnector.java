package main.java.bll_chess;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class ArduinoConnector {
    private SerialPort serialPort;
    private BufferedReader input;
    private GamePanel gamePanel;
    private StringBuilder dataBuffer = new StringBuilder();
    private static final long TIMEOUT = 5000; //5000ms = 5s Timeout
    
    public ArduinoConnector(String portName, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        try {
            // Verfuegbare serielle Ports suchen und oeffnen
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("Verfuegbare Ports: " + Arrays.toString(ports));
            
            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(115200);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
            
            boolean opened = serialPort.openPort();
            if (!opened) {
                System.err.println("Fehler beim Oeffnen des Ports " + portName);
                return;
            }
            System.out.println("Erfolgreich verbunden mit " + portName);
            
            // Eingabestream initialisieren
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            
            // Daten-Listener hinzufuegen
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        try {
                            // Verfuegbare Daten lesen
                            String line = input.readLine();
                            if (line == null) {
                                return;
                            }
                            
                            System.out.println("Daten empfangen: " + line);
                            
                            // Daten zum Puffer hinzufuegen und auf vollstaendige Nachricht pruefen
                            dataBuffer.append(line);
                            
                            // Falls der Puffer eine vollstaendige Nachricht enthaelt (mit START- und END-Markern)
                            if (dataBuffer.indexOf("START,") >= 0 && dataBuffer.indexOf(",END") >= 0) {
                                int startIndex = dataBuffer.indexOf("START,");
                                int endIndex = dataBuffer.indexOf(",END") + 4; 
                                
                                if (startIndex >= 0 && endIndex > startIndex) {
                                    String completeMessage = dataBuffer.substring(startIndex, endIndex);
                                    processCompleteSensorData(completeMessage);
                                    
                                    // Verarbeiteten Teil aus dem Puffer entfernen
                                    dataBuffer.delete(0, endIndex);
                                }
                            }
                            
                            // Falls der Puffer zu gross wird, ohne eine gueltige Nachricht zu enthalten, leeren
                            if (dataBuffer.length() > 1000) {
                                System.out.println("Pufferueberlauf, wird geleert");
                                dataBuffer.setLength(0);
                            }
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            // Sende ein Startsignal an das Arduino, um die Kommunikation zu beginnen
            sendInitialRequest();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sendet ein initiales Signal an das Arduino, um Datenanforderung zu starten
     */
    private void sendInitialRequest() {
        try {
            Thread.sleep(2000); // Dem Arduino Zeit geben, um zu starten
            OutputStream output = serialPort.getOutputStream();
            output.write("READY\n".getBytes());
            output.flush();
            System.out.println("READY-Signal an Arduino gesendet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Verarbeitung einer vollstaendigen Sensordaten-Nachricht
     */
    private void processCompleteSensorData(String data) {
        if (data.startsWith("START,") && data.endsWith(",END")) {
            // Uebergabe der vollstaendigen Nachricht an das GamePanel
            gamePanel.processSensorData(data);
        } else {
            System.out.println("Unerwartetes Datenformat: " + data);
        }
    }
    
    /**
     * Schliesst die serielle Verbindung
     */
    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Serieller Port geschlossen");
        }
    }
}
