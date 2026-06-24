@echo off
title Pet Simulator 3D
chcp 65001 >nul

set JAVAFX_PATH=D:\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1
set MYSQL_JAR=lib\mysql-connector-j-9.7.0.jar
set SRC=src\pet\*.java
set BIN=bin
set MODULES=javafx.controls,javafx.graphics,javafx.media,javafx.fxml,javafx.base

echo ^===================================^
echo ^|   Pet Simulator 3D - Compiler   ^|
echo ^===================================^
echo.

if not exist "%JAVAFX_PATH%\lib\javafx.controls.jar" (
    echo [!] JavaFX SDK tidak ditemukan di: %JAVAFX_PATH%
    echo [!] Edit run.bat dan sesuaikan JAVAFX_PATH
    pause
    exit /b 1
)

if not exist "%MYSQL_JAR%" (
    echo [!] MySQL Connector tidak ditemukan di: %MYSQL_JAR%
    echo [!] Edit run.bat dan sesuaikan MYSQL_JAR
    pause
    exit /b 1
)

echo [*] Compiling...
javac --module-path "%JAVAFX_PATH%\lib" --add-modules %MODULES% -cp "%MYSQL_JAR%" -d "%BIN%" %SRC%
if %errorlevel% neq 0 (
    echo [!] Compile error! Perbaiki kode di atas.
    pause
    exit /b %errorlevel%
)

echo [*] Compile berhasil! Menjalankan game...
echo.
java --enable-native-access=javafx.graphics --module-path "%JAVAFX_PATH%\lib" --add-modules %MODULES% -cp "%BIN%;%MYSQL_JAR%" pet.Main

pause
