@echo off
cd /d "%~dp0"
tasklist /FI "IMAGENAME eq mariadbd.exe" 2>NUL | find /I /N "mariadbd.exe">NUL
if "%ERRORLEVEL%"=="1" (
    start "" /B "C:\Program Files\MariaDB 13.0\bin\mariadbd.exe" --defaults-file="C:\Program Files\MariaDB 13.0\data\my.ini"
    timeout /t 1 /nobreak >nul
)
start "" "C:\Program Files\BellSoft\LibericaJDK-21-Full\bin\java.exe" -cp "bin;lib/*;Images;." application.Candy01
