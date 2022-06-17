#!/bin/bash

# Compiles client and server, and optionally generates a new database file.
# Parameters:
# - Name of binary directory
# - (Optional) Database file name (e.g. database.json).
# - (Optional) Man-in-the-Middle password file name (e.g. mitm.txt).
# - (Optional) Client configuration file name (e.g. config.json).
# - (Optional) Credentials file name (e.g. credentials.txt).

binDir=$1

# Server
cd BankingServer
mkdir -p $binDir/
javac -d $binDir -sourcepath src/ -cp lib/*:. src/ServerMain.java
cd ..

# Client
cd BankingClient
mkdir -p $binDir/
javac -d $binDir -sourcepath src/:../BankingServer/src/ -cp lib/*:. src/ClientMain.java
cd ..

# Generate new database, if desired
if [ "$#" -lt 5 ]; then
	exit
fi
cd BankingServer/$binDir/
exec java -cp ../lib/*:. ServerMain generate $2 $3 $4 $5