#!/bin/bash

# Obtener el directorio actual
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Compilar el proyecto primero
./compile.sh

# Función para detectar el terminal disponible
get_terminal_command() {
    if command -v gnome-terminal &> /dev/null; then
        echo "gnome-terminal --"
    elif command -v konsole &> /dev/null; then
        echo "konsole -e"
    elif command -v xterm &> /dev/null; then
        echo "xterm -e"
    else
        echo "❌ Error: No se encontró un terminal compatible (necesitas gnome-terminal, konsole o xterm)"
        exit 1
    fi
}

# Obtener el comando del terminal
TERMINAL_CMD=$(get_terminal_command)

# Función para abrir una nueva terminal y ejecutar un comando
open_terminal_with_command() {
    $TERMINAL_CMD bash -c "cd $DIR && $1; exec bash"
}

# Abrir terminal para OrderInput (primero para que inicie el sistema)
open_terminal_with_command "java -cp out OrderInput"

# Abrir terminal para StockViewer
open_terminal_with_command "java -cp out StockViewer"

# Abrir terminal para ProcessViewer
open_terminal_with_command "java -cp out ProcessViewer"

echo "✅ Sistema de cocina iniciado:"
echo "  - OrderInput corriendo en una terminal"
echo "  - StockViewer corriendo en una terminal"
echo "  - ProcessViewer corriendo en una terminal (monitor de procesos)" 