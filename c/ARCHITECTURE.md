# Arquitectura de Procesos - Sistema de Cocina

## Resumen Ejecutivo

El sistema de cocina utiliza una arquitectura basada en **procesos concurrentes** que se comunican a través de **memoria compartida** y **semáforos**. El número total de procesos varía dinámicamente según las recetas solicitadas.

## Tipos de Procesos

### 1. Proceso Principal (Main Process)
- **Función**: Coordinador y supervisor del sistema
- **Responsabilidades**:
  - Crear y configurar memoria compartida y semáforos
  - Lanzar procesos hijos según necesidades
  - Monitorear progreso general
  - Gestionar terminación del sistema
  - Limpiar recursos al finalizar

### 2. Cocineros Especializados (Specialized Cook Processes)
- **Función**: Preparar un tipo específico de receta
- **Características**:
  - **Dinámicos**: Solo se crean si hay pedidos de su receta
  - **Especializados**: Cada cocinero trabaja exclusivamente en un tipo de plato
  - **Máximo**: 4 cocineros (uno por cada tipo de receta disponible)

#### Tipos de Cocineros Especializados:
1. **Cocinero de Hamburguesas**: Solo prepara hamburguesas
2. **Cocinero de Tacos**: Solo prepara tacos  
3. **Cocinero de Sandwiches**: Solo prepara sandwiches vegetales
4. **Cocinero de Quesadillas**: Solo prepara quesadillas

### 3. Proceso Reponedor (Replenisher Process)
- **Función**: Gestión automática de inventario
- **Responsabilidades**:
  - Monitorear niveles de stock de todos los ingredientes
  - Reabastecer cuando el stock < MIN_STOCK (5 unidades)
  - Mantener stock en MAX_STOCK (15 unidades)
  - **Siempre presente**: Se crea independientemente de las recetas

### 4. Proceso Monitor (Monitor Process)  
- **Función**: Visualización del estado del sistema
- **Responsabilidades**:
  - Mostrar progreso de recetas cada 2 segundos
  - Reportar estado del inventario
  - Alertar sobre ingredientes bajos
  - **Siempre presente**: Se crea independientemente de las recetas

## Fórmula de Cálculo de Procesos

```
Total de Procesos = 1 (Main) + N (Cocineros) + 1 (Reponedor) + 1 (Monitor)

Donde N = Número de recetas con cantidad > 0
```

## Ejemplos Prácticos

### Ejemplo 1: `./kitchen 100 0 0 0`
```
Recetas solicitadas: Solo hamburguesas
Procesos creados:
├── 1 Proceso Principal
├── 1 Cocinero especializado en Hamburguesas
├── 1 Proceso Reponedor  
└── 1 Proceso Monitor
TOTAL: 4 procesos
```

### Ejemplo 2: `./kitchen 100 100 0 0`
```
Recetas solicitadas: Hamburguesas y Tacos
Procesos creados:
├── 1 Proceso Principal
├── 1 Cocinero especializado en Hamburguesas
├── 1 Cocinero especializado en Tacos
├── 1 Proceso Reponedor
└── 1 Proceso Monitor  
TOTAL: 5 procesos
```

### Ejemplo 3: `./kitchen 100 100 100 100`
```
Recetas solicitadas: Todas las recetas
Procesos creados:
├── 1 Proceso Principal
├── 1 Cocinero especializado en Hamburguesas
├── 1 Cocinero especializado en Tacos
├── 1 Cocinero especializado en Sandwiches
├── 1 Cocinero especializado en Quesadillas
├── 1 Proceso Reponedor
└── 1 Proceso Monitor
TOTAL: 7 procesos
```

### Ejemplo 4: `./kitchen 0 0 0 500`
```
Recetas solicitadas: Solo quesadillas
Procesos creados:
├── 1 Proceso Principal
├── 1 Cocinero especializado en Quesadillas
├── 1 Proceso Reponedor
└── 1 Proceso Monitor
TOTAL: 4 procesos
```

## Configuración del Sistema

### Constantes Definidas
```c
#define MAX_CHILDREN 6      // Máximo de procesos hijos
#define NUM_RECIPES 4       // Número de recetas disponibles
#define NUM_INGREDIENTS 8   // Número de ingredientes
#define MAX_STOCK 15        // Stock máximo por ingrediente
#define MIN_STOCK 5         // Stock mínimo para reabastecimiento
```

### Rango de Procesos Posibles
- **Mínimo**: 4 procesos (cuando solo se solicita 1 tipo de receta)
- **Máximo**: 7 procesos (cuando se solicitan las 4 recetas)

## Comunicación Entre Procesos

### Memoria Compartida
- **Estructura**: `SharedMemory`
- **Contenido**:
  - Stock de ingredientes
  - Progreso de recetas (completadas, en preparación, pendientes)
  - Señales de terminación
  - Contadores totales

### Sincronización
- **Mecanismo**: Semáforos POSIX
- **Propósito**: Acceso exclusivo a memoria compartida
- **Funciones**: `sem_lock()` y `sem_unlock()`

## Terminación del Sistema

### Terminación Automática
- Ocurre cuando todas las recetas solicitadas han sido completadas
- Los cocineros especializados terminan automáticamente al completar sus asignaciones

### Terminación Manual
- El usuario puede presionar Enter en cualquier momento
- Se activa la bandera `should_terminate` en memoria compartida
- Todos los procesos verifican esta bandera periódicamente

### Limpieza de Recursos
1. Desconexión de memoria compartida (`shmdt`)
2. Eliminación de memoria compartida (`shmctl`)
3. Eliminación de semáforos (`semctl`)
4. Espera de terminación de procesos hijos (`waitpid`)

## Ventajas de esta Arquitectura

1. **Eficiencia**: Solo se crean los procesos necesarios
2. **Especialización**: Cada cocinero es experto en su receta
3. **Escalabilidad**: Fácil agregar nuevas recetas o cocineros
4. **Robustez**: Manejo de señales y limpieza de recursos
5. **Monitoreo**: Visibilidad completa del estado del sistema
6. **Concurrencia**: Preparación paralela de diferentes recetas

## Notas Técnicas

- Los procesos hijos configuran manejadores de señales por defecto (`SIG_DFL`)
- El proceso principal maneja señales `SIGTERM` y `SIGINT` para limpieza
- Se utiliza `ftok(".", 'K')` para generar claves consistentes
- Timeout en `select()` permite verificación periódica de terminación manual 