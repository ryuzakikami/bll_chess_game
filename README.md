# Mensch-Maschine-Schnittstelle: Analoge Spielzüge digitalisieren und verarbeiten am Beispiel von Schach

**Besondere Lernleistung (BLL)** | Jahrgangsstufe 13 (Abitur 2026)[cite: 3]  
**Autor:** Youcef Rezgui[cite: 3]  
**Betreuer:** Sebastian Wilhelm[cite: 3]  
**Schule:** Gustav-Heinemann-Schule Oberstufengymnasium Rüsselsheim[cite: 3]  

---

## 📌 Worum geht's in dem Projekt?

In diesem Projekt habe ich ein intelligentes Schachbrett gebaut, das analoge Spielzüge auf einem echten Holzbrett in Echtzeit erkennt und digitalisiert[cite: 3]. 

Unter dem Schachbrett sitzt eine selbst gebaute Sensormatrix aus 64 Hallsensoren[cite: 3]. In den Böden der Schachfiguren stecken kleine Magnete[cite: 3]. Sobald eine Figur bewegt wird, erkennt der Sensor darunter die Änderung. Ein Arduino Mega 2560 liest die Sensoren über vier Multiplexer aus und schickt die Züge an eine Java-Anwendung (GUI)[cite: 3]. Die Anwendung zeigt das Spielfeld am PC an, prüft die Züge auf Regelkonformität.[cite: 3].

> 📄 **Hinweis zur Hausarbeit:**  
> Da das Projekt im Rahmen meiner BLL entstanden ist, habe ich den genauen Aufbau, die Schaltpläne, die Software-Logik und alle Tests ausführlich in meiner schriftlichen Hausarbeit dokumentiert[cite: 3]. Die PDF dazu findest du im Ordner [`docs/HausarbeitBLL/`](./docs/HausarbeitBLL/)[cite: 3].

---

## 📂 Repository-Überblick

Hier eine kurze Übersicht, wo was im Projekt liegt:

* **[`docs/HausarbeitBLL/`](./docs/HausarbeitBLL/)**: Die komplette schriftliche Ausarbeitung als PDF (sowie LaTeX-Quellcode und Bilder)[cite: 3]. **Hier wird alles im Detail erklärt!**[cite: 3]
* **[`docs/schachbrett_model/`](./docs/schachbrett_model/)**: Die 3D-Druck-Dateien (`.stl` und `.step`) für das Schachbrett und die Figuren[cite: 3].
* **[`src/main/java/bll_chess/`](./src/main/java/bll_chess/)**: Der Java-Code für das Spielfeld, die Regeln, Sounds und die Anbindung an den Arduino[cite: 3].
* **[`src/main/java/bll_chess/Hallsensorer_Matrix/`](./src/main/java/bll_chess/Hallsensorer_Matrix/)**: Der C++ Code (`.ino`) für den Arduino Mega[cite: 3].
* **[`src/main/resources/`](./src/main/resources/)**: Bilder der Figuren und die Soundeffekte[cite: 3].
* **[`lib/`](./lib/)**: Externe Java-Bibliotheken (`jSerialComm` für die serielle Verbindung & `jlayer` für Sound)[cite: 3].

---

## 🛠️ Verwendete Technik

* **Hardware:** Arduino Mega 2560, 64x digitale Hallsensoren (3144), 4x CD74HC4067 Multiplexer, Neodym-Magnete, 3D-Druckteile[cite: 3].
* **Software:** Java (Swing/AWT) für die GUI, C++ für den Arduino[cite: 3].
* **Libraries:** `jSerialComm`, `jlayer`[cite: 3].

---

## 🚀 Wie man das Projekt startet

1. Den Arduino Mega anschließen und den Sketch `Hallsensorer_Matrix.ino` hochladen[cite: 3].
2. Das Java-Projekt in einer IDE (z. B. Eclipse oder IntelliJ) öffnen[cite: 3].
3. Die `.jar`-Dateien aus dem `lib/`-Ordner zum Build Path / zu den Abhängigkeiten hinzufügen[cite: 3].
4. `ChessApp.java` ausführen[cite: 3].