# Sistema de Cocina - Simulador de Cocina con Procesos Concurrentes

Este sistema simula una cocina profesional con múltiples cocineros especializados, un reponedor automático de ingredientes y monitores de inventario en tiempo real. Utiliza procesos concurrentes en C con memoria compartida, semáforos y un sistema de reposición eficiente basado en eventos.

## Características Principales

✨ **Sistema de Reposición Inteligente**: Reponedor event-driven que solo actúa cuando es necesario
🍳 **Cocineros Especializados**: Cada cocinero se especializa en una receta específica
📊 **Monitoreo en Tiempo Real**: Múltiples monitores para diferentes aspectos del sistema
🔄 **Gestión Automática de Procesos**: Supervisión y recuperación automática de procesos
🛡️ **Manejo Robusto de Señales**: Terminación limpia y manejo de procesos hijos

## Requisitos

- Sistema operativo Unix/Linux/macOS
- Compilador GCC
- Make
- Permisos para crear recursos IPC (memoria compartida y semáforos)

## Estructura del Proyecto

```
.
├── include/
│   └── kitchen.h              # Definiciones principales y estructuras
├── src/
│   ├── kitchen.c              # Programa principal de la cocina
│   ├── kitchen_utils.c        # Funciones de utilidad y manejo de semáforos
│   ├── kitchen_monitor.c      # Monitor visual del inventario
│   └── process_monitor.c      # Monitor de procesos del sistema
├── test/                      # Directorio de pruebas
├── .vscode/                   # Configuración de VS Code
├── Makefile                   # Script de compilación
├── monitor.sh                 # Script de monitoreo de recursos IPC
├── README.md                  # Este archivo
├── REPOSICION_SEMAFOROS.md    # Documentación detallada del sistema de reposición
└── ARCHITECTURE.md            # Documentación de la arquitectura del sistema
```

## Compilación

```bash
make clean && make
```

Esto generará tres ejecutables:
- `kitchen`: Programa principal con cocineros y reponedor
- `kitchen_monitor`: Monitor visual del inventario con barras de progreso
- `process_monitor`: Monitor de procesos y recursos IPC del sistema

## Modos de Ejecución

### Modo Interactivo (Recomendado)

Para una experiencia completa, abrir **4 terminales diferentes**:

#### Terminal 1 - Programa Principal:
```bash
./kitchen 5 3 2 4  # Prepara 5 hamburguesas, 3 tacos, 2 ensaladas, 4 sopas
```

#### Terminal 2 - Monitor Visual de Inventario:
```bash
./kitchen_monitor
```
Muestra una interfaz visual con:
- 📊 Barras de progreso por ingrediente
- 🎨 Código de colores (Verde: OK, Amarillo: Medio, Rojo: Bajo)
- 📈 Estadísticas de recetas completadas
- ⚡ Actualizaciones en tiempo real

#### Terminal 3 - Monitor de Procesos:
```bash
./process_monitor
```
Muestra información del sistema:
- 🔍 Procesos activos del kitchen system
- 🧠 IDs de memoria compartida activos
- 🔐 IDs de semáforos en uso
- 📊 Estadísticas de recursos

#### Terminal 4 - Monitor de Recursos IPC:
```bash
./monitor.sh
```
Script que muestra cada 2 segundos:
- 💾 Estado de memoria compartida (ipcs -m)
- 🔒 Estado de semáforos (ipcs -s)
- ⚙️ Procesos relacionados con kitchen

### Modo Simple

Para ejecución básica sin monitores:
```bash
./kitchen 10 5 3 7  # Ejemplo con diferentes cantidades por receta
```

### Modo de Prueba Rápida

```bash
make run  # Ejecuta todos los componentes automáticamente
```

## Sistema de Reposición Inteligente

### Características del Reponedor Event-Driven

**Antes (Polling):**
- ❌ Revisaba inventario cada segundo (desperdicio de CPU)
- ❌ Latencia hasta 1 segundo para detectar faltantes
- ❌ Consumo constante de recursos

**Ahora (Event-Driven):**
- ✅ Se activa **solo cuando es necesario**
- ✅ Reposición **inmediata** al detectar stock bajo
- ✅ **Cero desperdicio** de CPU cuando no hay que reponer
- ✅ Sincronización perfecta con cocineros

### Funcionamiento

1. **Cocineros**: Al consumir ingredientes, verifican si alguno queda bajo el mínimo
2. **Notificación**: Si detectan faltante, activan bandera y señalizan al reponedor
3. **Reponedor**: Despierta inmediatamente, repone ingredientes necesarios
4. **Eficiencia**: Reponedor duerme hasta próxima notificación

Ver `REPOSICION_SEMAFOROS.md` para detalles técnicos completos.

## Recetas Disponibles

| Receta | Ingredientes Necesarios |
|--------|------------------------|
| 🍔 **Hamburguesa** | Pan(2) + Carne(1) + Lechuga(1) + Tomate(1) + Queso(1) |
| 🌮 **Taco** | Tortilla(2) + Carne(1) + Lechuga(1) + Salsa(1) |
| 🥗 **Ensalada** | Lechuga(2) + Tomate(2) + Zanahoria(1) |
| 🍲 **Sopa** | Zanahoria(2) + Tomate(1) |

## Gestión de Procesos

El sistema incluye supervisión automática de procesos:

### Procesos Principales
- **Proceso Padre**: Coordina todo el sistema
- **Cocineros**: Un proceso por receta activa (máximo 4)
- **Reponedor**: Proceso dedicado a mantener inventario
- **Monitor**: Proceso opcional para visualización

### Manejo de Fallos
- 🔄 **Detección automática** de procesos hijos que fallan
- 🛠️ **Recuperación automática** de procesos críticos
- 🛡️ **Limpieza automática** de recursos al terminar

## Comandos de Monitoreo

### Ver recursos IPC activos:
```bash
make monitor-ipcs
```

### Ver procesos del sistema:
```bash
make monitor-ps
```

### Limpiar recursos manualmente:
```bash
make clean
```

## Terminación del Sistema

### Terminación Normal
- **Programa principal**: Presiona `Enter` o `Ctrl+C`
- **Monitores**: `Ctrl+C`
- **Script monitor.sh**: `Ctrl+C`

### Terminación de Emergencia
Si el sistema no responde:
```bash
# Limpieza completa de recursos
make clean

# O manualmente:
ipcs -m | grep $(whoami) | awk '{print $2}' | xargs -n1 ipcrm -m
ipcs -s | grep $(whoami) | awk '{print $2}' | xargs -n1 ipcrm -s
```

## Configuración Avanzada

### Parámetros del Sistema (kitchen.h)
```c
#define MAX_STOCK 15        // Stock máximo por ingrediente
#define MIN_STOCK 5         // Umbral de reposición
#define NUM_INGREDIENTS 8   // Total de ingredientes
#define NUM_RECIPES 4       // Total de recetas disponibles
```

### Variables de Entorno
```bash
export KITCHEN_DEBUG=1      # Activa modo debug verboso
export KITCHEN_FAST=1       # Reduce tiempos de cocción para pruebas
```

## Resolución de Problemas

### Error: "Permission denied" al crear semáforos
```bash
# Verificar límites del sistema
ipcs -l

# Limpiar recursos previos
make clean
```

### Error: "No space left on device"
```bash
# Limpiar todos los recursos IPC del usuario
ipcs -m | grep $(whoami) | awk '{print $2}' | xargs -n1 ipcrm -m
ipcs -s | grep $(whoami) | awk '{print $2}' | xargs -n1 ipcrm -s
```

### Los monitores no muestran datos
1. Verificar que el programa principal esté ejecutándose
2. Verificar permisos de recursos IPC
3. Usar `./monitor.sh` para diagnóstico

## Señales Manejadas

- **SIGTERM/SIGINT**: Terminación limpia del sistema completo
- **SIGCHLD**: Supervisión automática de procesos hijos
- **SIGUSR1**: Modo debug (si está compilado)

## Desarrollo y Debugging

### Modo Verbose
```bash
KITCHEN_DEBUG=1 ./kitchen 2 2 2 2
```

### Análisis de Rendimiento
```bash
# Usar con valgrind para detectar memory leaks
valgrind --leak-check=full ./kitchen 1 1 1 1

# Usar con strace para analizar llamadas al sistema
strace -o trace.log ./kitchen 1 1 1 1
```

## Documentación Adicional

- `ARCHITECTURE.md`: Diagrama y explicación de la arquitectura
- `REPOSICION_SEMAFOROS.md`: Detalles técnicos del sistema de reposición
- Comentarios en código fuente para detalles de implementación

---

**Desarrollado para el curso de Sistemas Operativos**  
Sistema de procesos concurrentes con IPC en C 