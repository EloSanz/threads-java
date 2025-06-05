# Reposición Automática con Semáforos

## Diseño Actual vs. Semáforos

### ❌ **Método Actual (Polling)**
```c
// Reponedor actual - desperdicio de recursos
while (1) {
    sem_lock(sem_id);
    // Revisar TODOS los ingredientes cada segundo
    for (int i = 0; i < NUM_INGREDIENTS; i++) {
        if (memory->stock[i] < MIN_STOCK) {
            // Reponer...
        }
    }
    sem_unlock(sem_id);
    sleep(1); // ⚠️ Espera innecesaria
}
```

### ✅ **Método con Semáforos (Event-driven)**

## Enfoque 1: Semáforo de Notificación Simple

### Estructura Modificada:
```c
// En kitchen.h - agregar
typedef struct {
    int stock[NUM_INGREDIENTS];
    int should_terminate;
    // ... campos existentes ...
    int replenish_needed;  // ✨ Nueva bandera
} SharedMemory;

// Variables globales adicionales
int g_sem_mutex;      // Semáforo mutex existente
int g_sem_replenish;  // ✨ Nuevo semáforo para notificación
```

### Inicialización:
```c
// En main() después de crear semáforo mutex
g_sem_replenish = semget(key, 2, IPC_CREAT | 0666); // 2 semáforos
if (g_sem_replenish == -1) {
    perror("semget replenish failed");
    exit(1);
}

// Inicializar semáforos
semctl(g_sem_replenish, 0, SETVAL, 1); // mutex (índice 0)
semctl(g_sem_replenish, 1, SETVAL, 0); // notificación (índice 1)
```

### Cocineros Modificados:
```c
void consume_ingredients_for_recipe(SharedMemory* memory, int recipe_index) {
    int need_replenish = 0;
    
    // Consumir ingredientes
    for (int i = 0; i < NUM_INGREDIENTS; i++) {
        memory->stock[i] -= RECIPES[recipe_index].ingredients[i];
        
        // ✨ Verificar si algún ingrediente quedó bajo
        if (memory->stock[i] < MIN_STOCK) {
            need_replenish = 1;
        }
    }
    memory->cooking_in_progress[recipe_index]++;
    
    // ✨ Notificar al reponedor si es necesario
    if (need_replenish && !memory->replenish_needed) {
        memory->replenish_needed = 1;
        
        // Señalar al reponedor (incrementar semáforo de notificación)
        struct sembuf sb = {1, 1, 0}; // semáforo índice 1, +1
        semop(g_sem_replenish, &sb, 1);
        
        printf("🔔 Cocinero notificó necesidad de reposición\n");
    }
}
```

### Reponedor Event-driven:
```c
void replenisher_process(int shm_id, int sem_mutex, int sem_replenish) {
    SharedMemory* memory = shmat(shm_id, NULL, 0);
    printf("Reponedor iniciado (modo event-driven)\n");
    
    while (1) {
        // ✨ ESPERAR notificación (bloqueo hasta que se necesite reponer)
        struct sembuf sb = {1, -1, 0}; // semáforo índice 1, -1
        if (semop(sem_replenish, &sb, 1) == -1) {
            if (errno == EINTR) continue; // Interrumpido por señal
            perror("semop wait failed");
            break;
        }
        
        // Obtener acceso exclusivo
        sem_lock(sem_mutex);
        
        if (memory->should_terminate) {
            sem_unlock(sem_mutex);
            break;
        }
        
        printf("🔔 Reponedor activado por notificación\n");
        
        // Reponer ingredientes bajos
        int replenished = 0;
        for (int i = 0; i < NUM_INGREDIENTS; i++) {
            if (memory->stock[i] < MIN_STOCK) {
                int to_add = MAX_STOCK - memory->stock[i];
                memory->stock[i] = MAX_STOCK;
                printf("📦 Reponiendo %d unidades de %s (stock: %d)\n", 
                       to_add, INGREDIENT_NAMES[i], memory->stock[i]);
                replenished = 1;
            }
        }
        
        // Resetear bandera
        memory->replenish_needed = 0;
        
        sem_unlock(sem_mutex);
        
        if (replenished) {
            printf("✅ Reposición completada\n");
        }
    }
}
```

## Enfoque 2: Semáforos por Ingrediente (Más Avanzado)

### Estructura:
```c
// Un semáforo por ingrediente (8 semáforos)
int g_sem_ingredients[NUM_INGREDIENTS];

// Inicialización
for (int i = 0; i < NUM_INGREDIENTS; i++) {
    g_sem_ingredients[i] = semget(IPC_PRIVATE, 1, IPC_CREAT | 0666);
    semctl(g_sem_ingredients[i], 0, SETVAL, MAX_STOCK); // Inicializar con stock máximo
}
```

### Consumo:
```c
void consume_ingredients_atomic(SharedMemory* memory, int recipe_index) {
    // ✨ Decrementar semáforos atómicamente
    for (int i = 0; i < NUM_INGREDIENTS; i++) {
        int needed = RECIPES[recipe_index].ingredients[i];
        for (int j = 0; j < needed; j++) {
            struct sembuf sb = {0, -1, 0};
            semop(g_sem_ingredients[i], &sb, 1); // Bloquea si no hay stock
        }
        memory->stock[i] -= needed;
    }
}
```

### Reposición:
```c
void replenisher_per_ingredient() {
    while (1) {
        for (int i = 0; i < NUM_INGREDIENTS; i++) {
            // Verificar valor del semáforo
            int current_val = semctl(g_sem_ingredients[i], 0, GETVAL);
            
            if (current_val < MIN_STOCK) {
                // Reponer incrementando semáforo
                int to_add = MAX_STOCK - current_val;
                struct sembuf sb = {0, to_add, 0};
                semop(g_sem_ingredients[i], &sb, 1);
                
                memory->stock[i] = MAX_STOCK;
                printf("📦 Reponiendo %s a %d unidades\n", 
                       INGREDIENT_NAMES[i], MAX_STOCK);
            }
        }
        sleep(1); // Revisar menos frecuentemente
    }
}
```

## Enfoque 3: Semáforo de Umbral (Híbrido)

### Concepto:
```c
typedef struct {
    int stock[NUM_INGREDIENTS];
    int low_stock_flags[NUM_INGREDIENTS]; // ✨ Banderas por ingrediente
    // ...
} SharedMemory;

void check_and_signal_low_stock(SharedMemory* memory, int ingredient_index) {
    if (memory->stock[ingredient_index] < MIN_STOCK && 
        !memory->low_stock_flags[ingredient_index]) {
        
        memory->low_stock_flags[ingredient_index] = 1;
        
        // Notificar reponedor específicamente para este ingrediente
        struct sembuf sb = {ingredient_index, 1, 0};
        semop(g_sem_replenish_signals, &sb, 1);
        
        printf("🚨 Ingrediente %s requiere reposición urgente\n", 
               INGREDIENT_NAMES[ingredient_index]);
    }
}
```

## Comparación de Enfoques

| Enfoque | Ventajas | Desventajas |
|---------|----------|-------------|
| **Polling Actual** | Simple, fácil debug | Desperdicio CPU, latencia |
| **Notificación Simple** | Eficiente, inmediato | Un poco más complejo |
| **Semáforos por Ingrediente** | Control granular, bloqueo automático | Muy complejo, muchos semáforos |
| **Umbral Híbrido** | Balance eficiencia/simplicidad | Complejidad media |

## Recomendación

**El Enfoque 1 (Notificación Simple)** es el más práctico:
- ✅ Elimina polling innecesario
- ✅ Reposición inmediata
- ✅ Complejidad moderada
- ✅ Fácil de implementar y debuguear

## Ventajas del Diseño con Semáforos

1. **Eficiencia**: Reponedor solo actúa cuando es necesario
2. **Inmediatez**: Reposición en tiempo real
3. **Escalabilidad**: Mejor rendimiento con más procesos
4. **Recursos**: Menor uso de CPU
5. **Sincronización**: Coordinación perfecta entre procesos 