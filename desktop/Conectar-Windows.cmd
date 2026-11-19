@echo off
chcp 65001 >nul
title Android Optimizer - Conectar
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Conectar-Windows.ps1"
echo.
echo Pressione qualquer tecla para fechar.
pause >nul
