@echo off
setlocal enabledelayedexpansion

set NUM_CLIENTS=3
set SERVER_WAIT=3

echo [1/3] Сборка проекта...
call gradlew.bat runScomp compileJava --quiet
if errorlevel 1 (
    echo [ERROR] Сборка завершилась с ошибкой
    pause
    exit /b 1
)
echo [OK] Сборка успешна

echo [2/3] Запуск сервера...
start "Server" cmd /k "gradlew.bat runServer"
echo Сервер запущен. Ожидание %SERVER_WAIT% сек...
timeout /t %SERVER_WAIT% /nobreak >nul

echo [3/3] Запуск %NUM_CLIENTS% клиентов...
for /l %%i in (1,1,%NUM_CLIENTS%) do (
    start "Client-%%i" cmd /k "gradlew.bat runClient"
    echo Клиент %%i запущен
    timeout /t 1 /nobreak >nul
)

echo.
echo Все процессы запущены.
echo Введите в окне клиента: -m Ваше сообщение
echo Для выхода из клиента: -h
echo.
pause