
# Benutzungshinweise

## Kompilieren
Der folgende Befehl kompiliert Server und Client, und legt die generierten `*.class`-Dateien jeweils in einem Unterordner namens `binaries` ab.
```
$ ./compile.sh binaries
```

## Datenbank und Konfigurationsdatei erzeugen
Mit dem Befehl
```
$ ./compile.sh binaries /home/its/db.json /home/its/mitm.txt /home/its/clientconfig.json /home/its/credentials.txt
```
werden vier für das Projekt relevante Dateien erzeugt:
* `db.json`: Server-Datenbank. In dieser liegen Accountdaten für die Gruppe und die Angriffsziele.
* `mitm.txt`: Man-in-the-Middle-Passwort. Wird vom Praktikumsserver benötigt, um Szenario 1 simulieren zu können.
* `clientconfig.json`: Client-Konfiguration. In dieser Datei legt der Server aktuell nur eine Versionsnummer ab; die erzeugte Datei wird dem Client beigelegt. Die Versionsnummer (hardcoded in `Database.generate()`) hilft dem Nutzer dabei, die korrekte Implementierung zu identifizieren, und darf gern im Laufe des Projekts verändert werden. Sie hat darüber hinaus keine besondere technische Bedeutung.
* `credentials.txt`: Relevante Accountdaten. In dieser Datei finden die angreifenden Gruppen die für die einzelnen Angriffsszenarien benötigten Nutzernamen und Passwörter. Auch diese Datei wird dem Client beigelegt.

Es lohnt sich jeweils, für diese Dateien absolute Pfade zu benutzen (hier beispielsweise `/home/its/`), da diese sonst im `binaries`-Ordner des Servers landen.

## Server starten
Der Server wird mit dem Befehl
```
$ ./server.sh binaries /home/its/db.json localhost 12300
```
gestartet. Dies lädt die Server-Datenbank `/home/its/db.json` und öffnet anschließend auf dem lokalen Rechner den Port `12300` für eingehende Client-Verbindungen.

Für eine IPv6-Adresse statt eines Hostnamens sähe das folgendermaßen aus:
```
$ ./server.sh binaries /home/its/db.json ::1 12300
```
Auch dies öffnet den Port `12300` auf dem lokalen Rechner.

Das Skript nimmt außerdem einige optionale Parameter an; diese sind jedoch ausschließlich für die Einbettung in die Praktikumsumgebung relevant, und sollten beim lokalen Testen weggelassen werden.

## Client starten
Der folgende Befehl startet den Client:
```
$ ./client.sh binaries /home/its/clientconfig.json localhost 12300 /home/its/devicecodes/
```
Der Client lädt die angegebene Konfigurationsdatei und gibt die enthaltene Versionsnummer aus; anschließend baut er eine Verbindung mit dem Server unter `localhost`, Port `12300` auf. Bei der Registrierung des Clients beim Server wird ein Gerätecode erzeugt; dieser wird im Ordner `/home/its/devicecodes/` abgelegt.
