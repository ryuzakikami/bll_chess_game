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
    private static final long TIMEOUT = 5000; // 5 seconds timeout
    
    public ArduinoConnector(String portName, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        try {
            // Find and open the serial port
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("Available ports: " + Arrays.toString(ports));
            
            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(115200);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
            
            boolean opened = serialPort.openPort();
            if (!opened) {
                System.err.println("Failed to open port " + portName);
                return;
            }
            System.out.println("Successfully connected to " + portName);
            
            // Initialize the input stream
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            
            // Add the data listener
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        try {
                            // Read available data
                            String line = input.readLine();
                            if (line == null) {
                                return;
                            }
                            
                            System.out.println("Daten empfangen: " + line);
                            
                            // Append to buffer and check for complete message
                            dataBuffer.append(line);
                            
                            // If buffer contains a complete message (has START and END markers)
                            if (dataBuffer.indexOf("START,") >= 0 && dataBuffer.indexOf(",END") >= 0) {
                                int startIndex = dataBuffer.indexOf("START,");
                                int endIndex = dataBuffer.indexOf(",END") + 4; // include ",END"
                                
                                if (startIndex >= 0 && endIndex > startIndex) {
                                    String completeMessage = dataBuffer.substring(startIndex, endIndex);
                                    processCompleteSensorData(completeMessage);
                                    
                                    // Clear processed part from buffer
                                    dataBuffer.delete(0, endIndex);
                                }
                            }
                            
                            // If buffer has become too large without finding a valid message, clear it
                            if (dataBuffer.length() > 1000) {
                                System.out.println("Buffer overflow, clearing");
                                dataBuffer.setLength(0);
                            }
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            // Send a request for data to initialize communication
            sendInitialRequest();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends an initial request to the Arduino to start sending data
     */
    private void sendInitialRequest() {
        try {
            Thread.sleep(2000); // Give Arduino time to initialize
            OutputStream output = serialPort.getOutputStream();
            output.write("READY\n".getBytes());
            output.flush();
            System.out.println("Sent READY signal to Arduino");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Process a complete sensor data message
     */
    private void processCompleteSensorData(String data) {
        if (data.startsWith("START,") && data.endsWith(",END")) {
            // Pass the complete message to the game panel
            gamePanel.processSensorData(data);
        } else {
            System.out.println("Unerwartetes Datenformat: " + data);
        }
    }
    
    /**
     * Close the serial connection
     */
    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Closed serial port");
        }
    }
}