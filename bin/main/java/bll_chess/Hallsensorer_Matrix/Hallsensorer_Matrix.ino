int muxPins[4][5] = {
    {50, 51, 52, 53, 35}, // 1–4, E–H
    {46, 47, 48, 49, 34}, // 1–4, A–D
    {42, 43, 44, 45, 33}, // 5–8, E–H
    {38, 39, 40, 41, 32}  // 5–8, A–D
};

int previousSensorValues[64] = {0};
unsigned long lastMillis = 0;
const unsigned long interval = 500; // Alle 500 ms Auswertung

bool DEBUG_MODE = false;       // Gibt gesamte Brett-Matrix aus
bool DEBUG_MUX_PINS[4] = {     // Gibt pro MUX jeden Kanal und Select-Pin-Zustand aus
    false, false, false, false
};

void setup() {
    Serial.begin(115200);
    delay(1000);
    for (int mux = 0; mux < 4; mux++) {
        for (int i = 0; i < 4; i++) {
            pinMode(muxPins[mux][i], OUTPUT);
            digitalWrite(muxPins[mux][i], LOW);
        }
        pinMode(muxPins[mux][4], INPUT);
    }
}

void loop() {
    unsigned long now = millis();
    if (now - lastMillis < interval) return;
    lastMillis = now;

    int sensorValues[64];

    // 1) Alle 64 Sensoren auslesen
    for (int mux = 0; mux < 4; mux++) {
        for (int channel = 0; channel < 16; channel++) {
            // Select-Pins setzen
            for (int b = 0; b < 4; b++) {
                bool bitVal = bitRead(channel, 3 - b);
                digitalWrite(muxPins[mux][b], bitVal ? HIGH : LOW);
            }
            delayMicroseconds(5);
            int val = digitalRead(muxPins[mux][4]);
            sensorValues[mux * 16 + channel] = (val == HIGH ? 0 : 1);

            // Debug: Jeden Kanal und Select-Pin-Zustand ausgeben
            if (DEBUG_MUX_PINS[mux]) {
                Serial.print("MUX ");
                Serial.print(mux);
                Serial.print(" Channel ");
                Serial.print(channel);
                Serial.print(" Selects [");
                for (int b = 0; b < 4; b++) {
                    int pinState = digitalRead(muxPins[mux][b]);
                    Serial.print(pinState);
                    if (b < 3) Serial.print(" ");
                }
                Serial.print("]  ->  Val=");
                Serial.println(val);
            }
        }
    }

    // 2) Debug: gesamte Sensor-Matrix ausgeben
    if (DEBUG_MODE) {
        Serial.println("=== Sensor-Matrix (Brett) ===");
        for (int row = 7; row >= 0; row--) {
            Serial.print(row + 1);
            Serial.print(" | ");
            for (int col = 0; col < 8; col++) {
                int index = getSensorIndex(row, col);
                char colChar = 'A' + col;
                Serial.print(colChar);
                Serial.print(row + 1);
                Serial.print(":");
                Serial.print(sensorValues[index]);
                Serial.print(" ");
            }
            Serial.println();
        }
        Serial.println("   ---------------------------------------");
        Serial.println("    A   B   C   D   E   F   G   H");
        Serial.println();
    }

    // 3) Bewegungserkennung
    int diffCount = 0;
    int fromIndex = -1;
    int toIndex = -1;
    for (int i = 0; i < 64; i++) {
        if (sensorValues[i] != previousSensorValues[i]) {
            diffCount++;
            if (previousSensorValues[i] == 1 && sensorValues[i] == 0) {
                fromIndex = i;
            }
            if (previousSensorValues[i] == 0 && sensorValues[i] == 1) {
                toIndex = i;
            }
        }
    }

    // 4) Wenn genau 2 Änderungen, sende im Java-GUI-Format
    if (diffCount == 2 && fromIndex >= 0 && toIndex >= 0) {
        int fromRow, fromCol, toRow, toCol;
        getRowColFromSensorIndex(fromIndex, fromRow, fromCol);
        getRowColFromSensorIndex(toIndex,   toRow,   toCol);

        int fromRowVisual = fromRow ; // 1..8
        int toRowVisual   = toRow ;   // 1..8
        char fromColChar  = 'A' + fromCol;
        char toColChar    = 'A' + toCol;

        Serial.print("START,");
        Serial.print(fromRowVisual);
        Serial.print(",");
        Serial.print(fromColChar);
        Serial.print(",");
        Serial.print(toRowVisual);
        Serial.print(",");
        Serial.print(toColChar);
        Serial.println(",END");
    }

    // 5) Sensorwerte speichern für nächsten Vergleich
    memcpy(previousSensorValues, sensorValues, sizeof(previousSensorValues));
}

// 1) Umgekehrtes Mapping: sensorIndex (0–63) → (row 0–7, col 0–7)
void getRowColFromSensorIndex(int sensorIndex, int &row, int &col) {
    int mux        = sensorIndex / 16;   // 0..3
    int localIndex = sensorIndex % 16;   // 0..15
    int muxRow     = localIndex / 4;     // 0..3
    int muxCol     = localIndex % 4;     // 0..3

    if (mux == 1 || mux == 3) {
        // MUX 1 und 3 decken Spalten A–D ab (nicht gespiegelt)
        col = 3-muxCol;
        row = (mux == 1) ? muxRow : (muxRow + 4);
    } else {
        // MUX 0 und 2 decken Spalten E–H ab (gespiegelt)
        col = 7 - muxCol;  // Spiegelung: E→3→col=4, H→0→col=7
        row = (mux == 0) ? muxRow : (muxRow + 4);
    }
}

// 2) Mapping: (row 0–7, col 0–7) → sensorIndex (0–63)
int getSensorIndex(int row, int col) {
    int mux, localIndex;

    if (col <= 3) {
        // Spalte A–D → MUX 1/3 (keine Spiegelung)
        mux = (row <= 3) ? 1 : 3;
        int muxRow = row % 4;
        int muxCol = col;
        localIndex = muxRow * 4 + muxCol;
    } else {
        // Spalte E–H → MUX 0/2 (gespiegelt)
        mux = (row <= 3) ? 0 : 2;
        int muxRow = row % 4;
        int muxCol = 7 - col;  // Spalte E(4)→muxCol=3, H(7)→muxCol=0
        localIndex = muxRow * 4 + muxCol;
    }

    return mux * 16 + localIndex;
}

