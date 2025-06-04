#!/bin/bash

# Dar permisos de ejecución al script
chmod +x monitor.sh

while true; do
    clear
    echo "=== Monitor de Recursos del Sistema de Cocina ==="
    echo "Presiona Ctrl+C para salir"
    echo
    
    echo "=== Memoria Compartida ==="
    ipcs -m
    echo
    
    echo "=== Semáforos ==="
    ipcs -s
    echo
    
    echo "=== Procesos ==="
    ps aux | grep kitchen_system | grep -v grep
    
    sleep 2
done 