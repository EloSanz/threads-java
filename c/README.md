# Sistema de Cocina - Simulador de Cocina con Procesos Concurrentes

Este sistema simula una cocina con múltiples cocineros, un reponedor de ingredientes y monitores de inventario utilizando procesos concurrentes en C. El sistema utiliza memoria compartida y semáforos para la sincronización entre procesos.

## Requisitos

- Sistema operativo Unix/Linux/macOS
- Compilador GCC
- Make (opcional)

## Estructura del Proyecto

```
.
├── include/
│   └── kitchen.h
├── src/
│   ├── kitchen.c
│   ├── kitchen_monitor.c
│   └── process_monitor.c
├── Makefile
└── README.md
```

## Compilación

```bash
make
```

Esto generará tres ejecutables:
- `kitchen`: El programa principal de la cocina
- `kitchen_monitor`: Monitor visual del inventario
- `process_monitor`: Monitor de procesos y recursos del sistema

## Ejecución

Para una mejor visualización, se recomienda abrir tres terminales diferentes:

### Terminal 1 - Programa Principal:
```bash
./kitchen
```
Este es el programa principal que ejecuta los cocineros y el reponedor.

### Terminal 2 - Monitor de Inventario:
```bash
./kitchen_monitor
```
Muestra una interfaz visual con barras de progreso y colores que indica el estado actual del inventario:
- Verde: Stock saludable
- Amarillo: Stock medio
- Rojo: Stock bajo

### Terminal 3 - Monitor de Procesos:
```bash
./process_monitor
```
Muestra información en tiempo real sobre:
- Procesos activos del sistema
- IDs de memoria compartida
- IDs de semáforos

## Funcionamiento

El programa principal iniciará:
- 2 cocineros que prepararán platos consumiendo ingredientes
- 1 reponedor que monitoreará y repondrá ingredientes bajos

Los monitores te permitirán ver en tiempo real:
- El estado del inventario con una interfaz visual
- Los procesos activos y recursos del sistema

## Terminación

- En el programa principal: Presiona Enter
- En los monitores: Presiona Ctrl+C

El sistema limpiará automáticamente todos los recursos al terminar el programa principal.

## Detalles del Sistema

- Los cocineros intentarán cocinar si hay suficientes ingredientes disponibles
- El reponedor mantiene el stock de ingredientes entre un mínimo y máximo establecido
- Los monitores se actualizan en tiempo real
- El sistema utiliza memoria compartida y semáforos para la sincronización
- Se manejan señales para una terminación limpia del programa

## Limpieza Manual de Recursos

Si necesitas limpiar manualmente los recursos:
```bash
make clean
```

## Señales Manejadas

- SIGTERM: Termina el programa limpiamente
- SIGINT (Ctrl+C): Termina el programa limpiamente

## Limpieza de Recursos

El programa maneja automáticamente la limpieza de recursos (memoria compartida y semáforos) al terminar. 