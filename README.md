# Mensch-Maschine-Schnittstelle: Analoge Spielzüge digitalisieren und verarbeiten am Beispiel von Schach

**Besondere Lernleistung (BLL)** | Jahrgangsstufe 13 (Abitur 2026)
**Autor:** Youcef Rezgui
**Betreuer:** Sebastian Wilhelm
**Schule:** Gustav-Heinemann-Schule Oberstufengymnasium Rüsselsheim 

---

## 📌 Worum geht's in dem Projekt?

In diesem Projekt habe ich ein intelligentes Schachbrett gebaut, das analoge Spielzüge auf einem echten Holzbrett in Echtzeit erkennt und digitalisiert. 

Unter dem Schachbrett sitzt eine selbst gebaute Sensormatrix aus 64 Hallsensoren. In den Böden der Schachfiguren stecken kleine Magnete. Sobald eine Figur bewegt wird, erkennt der Sensor darunter die Änderung. Ein Arduino Mega 2560 liest die Sensoren über vier Multiplexer aus und schickt die Züge an eine Java-Anwendung (GUI). Die Anwendung zeigt das Spielfeld am PC an, prüft die Züge auf Regelkonformität..

> 📄 **Hinweis zur Hausarbeit:**  
> Da das Projekt im Rahmen meiner BLL entstanden ist, habe ich den genauen Aufbau, die Schaltpläne, die Software-Logik und alle Tests ausführlich in meiner schriftlichen Hausarbeit dokumentiert[cite: 3]. Die PDF dazu findest du im Ordner [`docs/HausarbeitBLL/`](./docs/HausarbeitBLL/).

---

## 📂 Repository-Überblick

Hier eine kurze Übersicht, wo was im Projekt liegt :

* **[`docs/HausarbeitBLL/`](./docs/HausarbeitBLL/)**: Die komplette schriftliche Ausarbeitung als PDF (sowie LaTeX-Quellcode und Bilder). **Hier wird alles im Detail erklärt!**
* **[`docs/schachbrett_model/`](./docs/schachbrett_model/)**: Die 3D-Druck-Dateien (`.stl` und `.step`) für das Schachbrett und die Figuren.
* **[`src/main/java/bll_chess/`](./src/main/java/bll_chess/)**: Der Java-Code für das Spielfeld, die Regeln, Sounds und die Anbindung an den Arduino.
* **[`src/main/java/bll_chess/Hallsensorer_Matrix/`](./src/main/java/bll_chess/Hallsensorer_Matrix/)**: Der C++ Code (`.ino`) für den Arduino Mega.
* **[`src/main/resources/`](./src/main/resources/)**: Bilder der Figuren und die Soundeffekte.
* **[`lib/`](./lib/)**: Externe Java-Bibliotheken (`jSerialComm` für die serielle Verbindung & `jlayer` für Sound).

---

## 🛠️ Verwendete Technik

* **Hardware:** Arduino Mega 2560, 64x digitale Hallsensoren (3144), 4x CD74HC4067 Multiplexer, Neodym-Magnete, 3D-Druckteile.
* **Software:** Java (Swing/AWT) für die GUI, C++ für den Arduino.
* **Libraries:** `jSerialComm`, `jlayer`.
