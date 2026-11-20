@echo off
chcp 65001 >nul
title DebloaToast - Desconectar
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Desconectar-Windows.ps1"
echo.
pause
