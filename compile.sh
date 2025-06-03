#!/bin/bash

# Crear directorio out si no existe
mkdir -p out

# Limpiar directorio out
rm -rf out/*

# Limpiar archivos .class en src
find src -name "*.class" -type f -delete

# Compilar todos los archivos .java a out
find src -name "*.java" -print | xargs javac -d out

echo "✅ Compilación completada. Los archivos .class están en el directorio 'out'" 