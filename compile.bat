@echo off

REM Compiles client and server, and optionally generates a new database file.
REM Parameters:
REM - Name of binary directory
REM - (Optional) Database file name (e.g. database.json).
REM - (Optional) Man-in-the-Middle password file name (e.g. mitm.txt).
REM - (Optional) Client configuration file name (e.g. config.json).
REM - (Optional) Credentials file name (e.g. credentials.txt).

set binDir=%1

cd BankingServer
mkdir %binDir%
javac -d %binDir% -sourcepath src/ -cp lib/*;. src/ServerMain.java
cd ..

cd BankingClient
mkdir %binDir%
javac -d %binDir% -sourcepath src/;../BankingServer/src/ -cp lib/*;. src/ClientMain.java
cd ..

IF [%2] == [] GOTO end
cd BankingServer/%binDir%/
java -cp ../lib/*;. ServerMain generate %2 %3 %4 %5

:end
@echo on