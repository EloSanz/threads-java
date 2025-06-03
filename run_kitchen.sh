#!/bin/bash

# Obtener el directorio actual
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Compilar el proyecto primero
./compile.sh

# Función para abrir una nueva terminal y ejecutar un comando
open_terminal_with_command() {
    osascript -e "tell app \"Terminal\"
        do script \"cd $DIR && $1\"
    end tell"
}

# Abrir terminal para OrderInput (primero para que inicie el sistema)
open_terminal_with_command "java -cp out OrderInput"

# Abrir terminal para StockViewer
open_terminal_with_command "java -cp out StockViewer"

# Abrir terminal para ProcessViewer (último para que vea todos los procesos)
open_terminal_with_command "java -cp out ProcessViewer"

echo "✅ Sistema de cocina iniciado:"
echo "  - OrderInput corriendo en una terminal"
echo "  - StockViewer corriendo en una terminal"
echo "  - ProcessViewer corriendo en una terminal (monitor de procesos)" 