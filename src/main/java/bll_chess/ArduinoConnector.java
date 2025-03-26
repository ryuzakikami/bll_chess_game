package main.java.bll_chess;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Diese Klasse stellt die Verbindung zum Arduino Mega 2560 her.
 * Sie öffnet den seriellen Port (mit Baudrate 115200) und benachrichtigt einen
 * registrierten DataListener, sobald neue Daten eintreffen.
 */
public class ArduinoConnector {
    private SerialPort serialPort;
    private DataListener dataListener;

    /**
     * Konstruktor: Öffnet den seriellen Port.
     * 
     * @param portName Name des Ports (z. B. "COM3" unter Windows oder "/dev/ttyUSB0" unter Linux)
     */
    public ArduinoConnector(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(115200);  // Baudrate 115200
        if (serialPort.openPort()) {
            System.out.println("Port geöffnet: " + portName);
        } else {
            System.out.println("Fehler beim Öffnen des Ports: " + portName);
        }
        // Füge einen DataListener hinzu, der neue Daten an unseren DataListener weitergibt
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                byte[] newData = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(newData, newData.length);
                String data = new String(newData).trim();
                System.out.println("Daten empfangen: " + data);
                if (dataListener != null) {
                    dataListener.onDataReceived(data);
                }
            }
        });
    }

    /**
     * Registriert einen DataListener, der benachrichtigt wird, wenn neue serielle Daten eintreffen.
     *
     * @param listener der zu registrierende Listener
     */
    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    /**
     * Sendet Daten an den Arduino.
     *
     * @param data Der zu sendende String
     */
    public void sendData(String data) {
        if (serialPort.isOpen()) {
            serialPort.writeBytes(data.getBytes(), data.length());
            System.out.println("Daten gesendet: " + data);
        } else {
            System.out.println("Port ist nicht geöffnet.");
        }
    }

    /**
     * Schließt den seriellen Port.
     */
    public void close() {
        if (serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Port geschlossen.");
        }
    }
    
    /**
     * Schnittstelle für einen Listener, der serielle Daten empfängt.
     */
    public interface DataListener {
        void onDataReceived(String data);
    }
}
