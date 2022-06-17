@echo off

REM Starts the server application.
REM Parameters:
REM - Name of binary directory
REM - Database file name (e.g. database.json).
REM - IP address or host name (e.g. 127.0.0.1).
REM - Port (e.g. 12000).

set binDir=%1

REM Run server
cd BankingServer/%binDir%/
java -cp ../lib/*;. ServerMain run %2 %3 %4
cd ../../

@echo on