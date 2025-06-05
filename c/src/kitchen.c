#include "../include/kitchen.h"

// Proceso cocinero especializado - solo prepara una receta específica
void specialized_cook_process(int shm_id, int sem_id, int cook_id, int recipe_index) {
    SharedMemory* memory = shmat(shm_id, NULL, 0);
    if (memory == (void *)-1) {
        perror("shmat failed in cook");
        exit(1);
    }

    // Configurar manejador de señales para el proceso hijo
    signal(SIGTERM, SIG_DFL);
    signal(SIGINT, SIG_DFL);

    printf("🧑‍🍳 Cocinero %d ESPECIALIZADO en %s iniciado\n", cook_id, RECIPES[recipe_index].name);
    
    while (1) {
        sem_lock(sem_id);
        
        if (memory->should_terminate) {
            sem_unlock(sem_id);
            break;
        }

        // Verificar si ya se completaron todos los pedidos de esta receta
        if (memory->recipes_completed[recipe_index] >= memory->recipes_to_prepare[recipe_index]) {
            sem_unlock(sem_id);
            printf("✅ Cocinero %d completó todos los %s (%d/%d)\n", 
                   cook_id, RECIPES[recipe_index].name,
                   memory->recipes_completed[recipe_index], 
                   memory->recipes_to_prepare[recipe_index]);
            break;
        }

        // Verificar si puede cocinar esta receta específica
        if (can_cook_recipe(memory, recipe_index)) {
            printf("🔥 Cocinero %d va a preparar: %s (%d/%d)\n", 
                   cook_id, RECIPES[recipe_index].name,
                   memory->recipes_completed[recipe_index] + memory->cooking_in_progress[recipe_index] + 1,
                   memory->recipes_to_prepare[recipe_index]);
            
            // Consumir ingredientes
            consume_ingredients_for_recipe(memory, recipe_index);
            
            printf("📦 Cocinero %d consiguió ingredientes para %s\n", cook_id, RECIPES[recipe_index].name);
            printf("   Ingredientes usados: ");
            int first = 1;
            for (int i = 0; i < NUM_INGREDIENTS; i++) {
                if (RECIPES[recipe_index].ingredients[i] > 0) {
                    if (!first) printf(", ");
                    printf("%s(%d)", INGREDIENT_NAMES[i], RECIPES[recipe_index].ingredients[i]);
                    first = 0;
                }
            }
            printf("\n");
            
            sem_unlock(sem_id);
            
            // Simular tiempo de preparación (fuera del semáforo)
            printf("⏱️  Cocinero %d cocinando %s... (%d segundos)\n", 
                   cook_id, RECIPES[recipe_index].name, RECIPES[recipe_index].prep_time);
            sleep(RECIPES[recipe_index].prep_time);
            
            // Marcar plato como completado
            sem_lock(sem_id);
            memory->recipes_completed[recipe_index]++;
            memory->cooking_in_progress[recipe_index]--;
            memory->total_completed++;
            printf("🍽️  Cocinero %d completó %s! (%d/%d de esta receta, %d/%d total)\n", 
                   cook_id, RECIPES[recipe_index].name,
                   memory->recipes_completed[recipe_index], memory->recipes_to_prepare[recipe_index],
                   memory->total_completed, memory->total_to_prepare);
            sem_unlock(sem_id);
        } else {
            printf("⏳ Cocinero %d esperando ingredientes para %s\n", cook_id, RECIPES[recipe_index].name);
            sem_unlock(sem_id);
            sleep(1); // Esperar antes de intentar de nuevo
        }
    }

    if (shmdt(memory) == -1) {
        perror("shmdt failed in cook");
    }
    printf("👋 Cocinero %d especializado en %s terminando\n", cook_id, RECIPES[recipe_index].name);
    exit(0);
}

// Proceso reponedor event-driven
void replenisher_process(int shm_id, int sem_id) {
    SharedMemory* memory = shmat(shm_id, NULL, 0);
    if (memory == (void *)-1) {
        perror("shmat failed in replenisher");
        exit(1);
    }

    // Configurar manejador de señales para el proceso hijo
    signal(SIGTERM, SIG_DFL);
    signal(SIGINT, SIG_DFL);

    printf("Reponedor iniciado (modo event-driven)\n");
    
    while (1) {
        // ✨ ESPERAR notificación (bloqueo hasta que se necesite reponer)
        struct sembuf sb = {0, -1, 0}; // semáforo índice 0, -1
        if (semop(g_sem_replenish, &sb, 1) == -1) {
            if (errno == EINTR) continue; // Interrumpido por señal
            perror("semop wait for replenish failed");
            break;
        }
        
        // Obtener acceso exclusivo
        sem_lock(sem_id);
        
        if (memory->should_terminate) {
            sem_unlock(sem_id);
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
        
        sem_unlock(sem_id);
        
        if (replenished) {
            printf("✅ Reposición completada\n");
        }
    }

    if (shmdt(memory) == -1) {
        perror("shmdt failed in replenisher");
    }
    printf("Reponedor terminando\n");
    exit(0);
}

// Proceso monitor mejorado
void monitor_process(int shm_id, int sem_id) {
    SharedMemory* memory = shmat(shm_id, NULL, 0);
    if (memory == (void *)-1) {
        perror("shmat failed in monitor");
        exit(1);
    }

    // Configurar manejador de señales para el proceso hijo
    signal(SIGTERM, SIG_DFL);
    signal(SIGINT, SIG_DFL);

    printf("Monitor iniciado\n");
    
    while (1) {
        sem_lock(sem_id);
        
        if (memory->should_terminate) {
            sem_unlock(sem_id);
            break;
        }

        // Mostrar estado actual
        printf("\n🏪 === ESTADO DE LA COCINA ===\n");
        printf("Menús completados: %d/%d\n", memory->total_completed, memory->total_to_prepare);
        
        printf("\n📊 Progreso por receta:\n");
        for (int i = 0; i < NUM_RECIPES; i++) {
            printf("  %s: %d/%d completados", 
                   RECIPES[i].name, 
                   memory->recipes_completed[i], 
                   memory->recipes_to_prepare[i]);
            if (memory->cooking_in_progress[i] > 0) {
                printf(" (%d en preparación)", memory->cooking_in_progress[i]);
            }
            printf("\n");
        }
        
        printf("\n📦 Stock de ingredientes:\n");
        for (int i = 0; i < NUM_INGREDIENTS; i++) {
            printf("  %s: %d", INGREDIENT_NAMES[i], memory->stock[i]);
            if (memory->stock[i] < MIN_STOCK) {
                printf(" ⚠️  (BAJO)");
            }
            printf("\n");
        }
        printf("==============================\n");

        sem_unlock(sem_id);
        sleep(2); // Actualizar cada 2 segundos
    }

    if (shmdt(memory) == -1) {
        perror("shmdt failed in monitor");
    }
    printf("Monitor terminando\n");
    exit(0);
}

int main(int argc, char* argv[]) {
    int recipes_count[NUM_RECIPES];
    
    // Parsear argumentos de línea de comandos
    if (!parse_arguments(argc, argv, recipes_count)) {
        print_usage(argv[0]);
        return 1;
    }
    
    // Verificar que al menos una receta tiene cantidad > 0
    int total_recipes = 0;
    for (int i = 0; i < NUM_RECIPES; i++) {
        total_recipes += recipes_count[i];
    }
    
    if (total_recipes == 0) {
        printf("Error: Debe preparar al menos un plato\n");
        return 1;
    }
    
    // Mostrar resumen de lo que se va a preparar
    printf("\n🍳 Iniciando cocina para preparar:\n");
    for (int i = 0; i < NUM_RECIPES; i++) {
        if (recipes_count[i] > 0) {
            printf("  - %d %s\n", recipes_count[i], RECIPES[i].name);
        }
    }
    printf("Total: %d platos\n\n", total_recipes);
    
    // Contar recetas activas y mostrar cocineros que se crearán
    int active_recipes = count_active_recipes(recipes_count);
    printf("🧑‍🍳 Se crearán %d cocineros especializados:\n", active_recipes);
    for (int i = 0; i < NUM_RECIPES; i++) {
        if (recipes_count[i] > 0) {
            printf("  - Cocinero especializado en %s\n", RECIPES[i].name);
        }
    }
    printf("\n");
    
    // Crear memoria compartida
    key_t key = ftok(".", 'K');
    g_shm_id = shmget(key, sizeof(SharedMemory), IPC_CREAT | 0666);
    if (g_shm_id == -1) {
        perror("shmget failed");
        exit(1);
    }

    // Crear semáforo principal (mutex)
    g_sem_id = semget(key, 1, IPC_CREAT | 0666);
    if (g_sem_id == -1) {
        perror("semget failed");
        cleanup_resources(g_shm_id, -1);
        exit(1);
    }

    // ✨ Crear semáforo de notificación para reposición
    key_t replenish_key = ftok(".", 'R');
    g_sem_replenish = semget(replenish_key, 1, IPC_CREAT | 0666);
    if (g_sem_replenish == -1) {
        perror("semget replenish failed");
        cleanup_resources(g_shm_id, g_sem_id);
        exit(1);
    }

    // Inicializar semáforos
    if (semctl(g_sem_id, 0, SETVAL, 1) == -1) {
        perror("semctl mutex failed");
        cleanup_resources(g_shm_id, g_sem_id);
        exit(1);
    }
    
    // ✨ Inicializar semáforo de notificación en 0 (sin notificaciones pendientes)
    if (semctl(g_sem_replenish, 0, SETVAL, 0) == -1) {
        perror("semctl replenish failed");
        cleanup_resources(g_shm_id, g_sem_id);
        exit(1);
    }

    // Inicializar memoria compartida
    SharedMemory* memory = shmat(g_shm_id, NULL, 0);
    if (memory == (void *)-1) {
        perror("shmat failed");
        cleanup_resources(g_shm_id, g_sem_id);
        exit(1);
    }
    init_shared_memory(memory, recipes_count);
    
    // Configurar manejador de señales
    signal(SIGTERM, handle_sigterm);
    signal(SIGINT, handle_sigterm);

    // Crear procesos hijos
    pid_t pids[MAX_CHILDREN];
    int process_count = 0;

    // Crear cocineros especializados (uno por receta activa)
    for (int i = 0; i < NUM_RECIPES; i++) {
        if (recipes_count[i] > 0) {  // Solo crear cocinero si hay pedidos de esta receta
            pids[process_count] = fork();
            if (pids[process_count] == -1) {
                perror("fork failed");
                cleanup_resources(g_shm_id, g_sem_id);
                exit(1);
            }
            if (pids[process_count] == 0) {
                specialized_cook_process(g_shm_id, g_sem_id, process_count + 1, i);
                exit(0);
            }
            process_count++;
        }
    }

    // Crear reponedor
    pids[process_count] = fork();
    if (pids[process_count] == -1) {
        perror("fork failed");
        cleanup_resources(g_shm_id, g_sem_id);
        exit(1);
    }
    if (pids[process_count] == 0) {
        replenisher_process(g_shm_id, g_sem_id);
        exit(0);
    }
    process_count++;

    // Crear monitor
    pids[process_count] = fork();
    if (pids[process_count] == -1) {
        perror("fork failed");
        cleanup_resources(g_shm_id, g_sem_id);
        exit(1);
    }
    if (pids[process_count] == 0) {
        monitor_process(g_shm_id, g_sem_id);
        exit(0);
    }
    process_count++;

    // Esperar a que se completen todos los menús
    printf("Presiona Enter para terminar el programa manualmente, o espera a que se completen todos los menús...\n");
    
    // Monitorear el progreso
    int manual_termination = 0;
    while (1) {
        sem_lock(g_sem_id);
        if (memory->total_completed >= memory->total_to_prepare) {
            printf("\n🎉 ¡Todos los menús han sido completados!\n");
            memory->should_terminate = 1;
            sem_unlock(g_sem_id);
            break;
        }
        sem_unlock(g_sem_id);
        
        // Verificar si el usuario presionó Enter
        fd_set fds;
        struct timeval timeout;
        FD_ZERO(&fds);
        FD_SET(0, &fds); // stdin
        timeout.tv_sec = 1;
        timeout.tv_usec = 0;
        
        if (select(1, &fds, NULL, NULL, &timeout) > 0) {
            getchar(); // Leer el Enter
            manual_termination = 1;
            printf("\n⏹️  Terminación manual solicitada...\n");
            break;
        }
    }

    // Indicar a los procesos que deben terminar
    sem_lock(g_sem_id);
    memory->should_terminate = 1;
    sem_unlock(g_sem_id);

    // Dar tiempo a los procesos para que vean la señal de terminación
    printf("Esperando que los procesos terminen limpiamente...\n");
    sleep(1);

    // Enviar señal SIGTERM a todos los procesos hijo por si acaso
    for (int i = 0; i < process_count; i++) {
        if (pids[i] > 0) {
            kill(pids[i], SIGTERM);
        }
    }

    // Esperar a que todos los procesos hijo terminen con timeout
    for (int i = 0; i < process_count; i++) {
        if (pids[i] > 0) {
            int status;
            pid_t result = waitpid(pids[i], &status, WNOHANG);
            if (result == 0) {
                // El proceso aún está ejecutándose, forzar terminación
                printf("Forzando terminación del proceso %d...\n", pids[i]);
                kill(pids[i], SIGKILL);
                waitpid(pids[i], &status, 0);
            }
        }
    }

    // Mostrar estado final
    printf("\n🏁 === RESUMEN FINAL ===\n");
    if (manual_termination) {
        printf("Terminación manual - Menús completados: %d/%d\n", 
               memory->total_completed, memory->total_to_prepare);
    } else {
        printf("Menús completados: %d/%d\n", memory->total_completed, memory->total_to_prepare);
    }
    
    printf("\n📊 Completados por receta:\n");
    for (int i = 0; i < NUM_RECIPES; i++) {
        printf("  %s: %d/%d\n", 
               RECIPES[i].name, 
               memory->recipes_completed[i], 
               memory->recipes_to_prepare[i]);
    }
    
    printf("\n📦 Stock final:\n");
    for (int i = 0; i < NUM_INGREDIENTS; i++) {
        printf("  %s: %d\n", INGREDIENT_NAMES[i], memory->stock[i]);
    }
    printf("=======================\n");

    // Desconectar de la memoria compartida
    if (shmdt(memory) == -1) {
        perror("shmdt failed");
    }

    // Limpiar recursos
    printf("Liberando recursos...\n");
    cleanup_resources(g_shm_id, g_sem_id);
    printf("✅ Recursos liberados correctamente\n");
    printf("Programa terminado\n");

    return 0;
} 