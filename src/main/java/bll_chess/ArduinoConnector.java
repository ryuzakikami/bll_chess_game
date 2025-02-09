package main.java.bll_chess;
import com.fazecast.jSerialComm.SerialPort;

public class ArduinoConnector {
    private SerialPort serialPort;

    public ArduinoConnector(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(9600);
        if (serialPort.openPort()) {
            System.out.println("Port geöffnet: " + portName);
        } else {
            System.out.println("Fehler beim Öffnen des Ports: " + portName);
        }
    }

    public void sendData(String data) {
        if (serialPort.isOpen()) {
            serialPort.writeBytes(data.getBytes(), data.length());
            System.out.println("Daten gesendet: " + data);
        } else {
            System.out.println("Port ist nicht geöffnet.");
        }
    }

    public void close() {
        if (serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Port geschlossen.");
        }
    }
}
