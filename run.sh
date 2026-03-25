#!/usr/bin/env bash
set -euo pipefail

NUM_CLIENTS=3
SERVER_WAIT=3

open_terminal() {
    local title="$1"
    local cmd="$2"

    if command -v gnome-terminal &>/dev/null; then
        gnome-terminal --title="$title" -- bash -c "$cmd; exec bash"
    elif command -v xterm &>/dev/null; then
        xterm -title "$title" -e bash -c "$cmd; exec bash" &
    elif command -v konsole &>/dev/null; then
        konsole --new-tab -p tabtitle="$title" -e bash -c "$cmd; exec bash" &
    else
        echo "[WARN] Терминал не найден. Запускаем '$title' в фоне, лог: logs/$title.log"
        mkdir -p logs
        bash -c "$cmd" > "logs/$title.log" 2>&1 &
    fi
}

echo "[1/3] Сборка..."
./gradlew runScomp compileJava --quiet
echo "Сборка завершена"

echo "[2/3] Запуск сервера..."
open_terminal "Server" "cd $(pwd) && ./gradlew runServer"
echo "Сервер запущен. Ожидание ${SERVER_WAIT} сек..."
sleep "$SERVER_WAIT"

echo "[3/3] Запуск ${NUM_CLIENTS} клиентов..."
for i in $(seq 1 "$NUM_CLIENTS"); do
    open_terminal "Client-$i" "cd $(pwd) && ./gradlew runClient"
    echo "Клиент $i запущен."
    sleep 1
done

echo ""
echo "Все процессы запущены."
echo "Введите в окне клиента: -m Ваше сообщение"
echo "Для выхода из клиента: -h"