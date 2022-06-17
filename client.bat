@echo off

REM Starts the client application.
REM Parameters:
REM - Name of binary directory
REM - Client configuration file
REM - Server IP address
REM - Server port
REM - Directory for storing device codes

set binDir=%1

REM Run client
cd BankingClient/%binDir%/
java -cp ../lib/*;. ClientMain %2 %3 %4 %5
cd ../../

@echo on