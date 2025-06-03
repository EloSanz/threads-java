import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

public class KitchenSystem {
    private static final Map<String, Integer> INITIAL_STOCK = new HashMap<String, Integer>() {{
        put("Pan", 20);
        put("Carne", 15);
        put("Lechuga", 30);
        put("Tomate", 30);
        put("Queso", 20);
        put("Tortilla", 20);
        put("Salsa", 15);
        put("Zanahoria", 20);
    }};

    public static void main(String[] args) {
        if (args.length != 1) {// checked ✅
            System.err.println("Uso: KitchenSystem <ordenes>");
            System.err.println("Formato: plato1:cantidad1,plato2:cantidad2,...");
            System.exit(1);
        }

        // Parsear órdenes del argumento (formato: plato:cantidad,plato:cantidad,...)
        Map<String, Integer> orders = new HashMap<>();
        for (String orderStr : args[0].split(",")) {
            String[] parts = orderStr.split(":");
            orders.put(parts[0], Integer.parseInt(parts[1]));
        }

        System.out.println("\n=== Sistema de Cocina Iniciado ===\n");
        
        try {
            // Inicializar memoria compartida
            SharedMemory sharedMemory = new SharedMemory(); // checked ✅
            
            // Inicializar stock en memoria compartida
            for (Map.Entry<String, Integer> entry : INITIAL_STOCK.entrySet()) {
                sharedMemory.setStock(entry.getKey(), entry.getValue()); // checked ✅
            }

            // Imprimir stock inicial
            System.out.println("Stock inicial:");
            INITIAL_STOCK.forEach((ingredient, quantity) -> 
                System.out.println("• " + ingredient + ": " + quantity)); // checked ✅

            // Imprimir órdenes recibidas
            System.out.println("\nÓrdenes recibidas:");
            orders.forEach((plato, cantidad) -> 
                System.out.println("• " + cantidad + " " + plato + "(s)")); // checked ✅

            // Lista para mantener referencia a los procesos
            List<Process> processes = new ArrayList<>(); // acá guardo a los cocineros y al reponedor

            // Iniciar el proceso reponedor
            Process replenisher = null;
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "java",
                     "-cp", "out", "StockReplenisher"
                );
                pb.inheritIO(); // Heredar la entrada/salida estándar del proceso padre
                replenisher = pb.start(); // Iniciar el proceso
                processes.add(replenisher); // Añadir el proceso a una lista de procesos
                System.out.println("\n✅ Proceso reponedor creado e iniciado");
            } catch (IOException e) {
                System.err.println("Error al crear proceso reponedor: " + e.getMessage());
                shutdownProcesses(processes);
                System.exit(1);
            }

            // Crear los procesos cocineros según las órdenes
            int cookId = 1;
            for (Map.Entry<String, Integer> order : orders.entrySet()) {
                String plato = order.getKey();
                int cantidad = order.getValue();
                
                // Crear un cocinero por cada plato ordenado
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        "java", "-cp", "out", "CookProcess",
                        "Cocinero" + cookId,  // nombre del cocinero
                        plato,                // tipo de receta
                        String.valueOf(cantidad)  // cantidad a preparar
                    );
                    pb.inheritIO();
                    Process process = pb.start();
                    processes.add(process);
                    System.out.println("✅ Proceso cocinero creado: Cocinero" + cookId + 
                                     " - Especialidad: " + plato + 
                                     " - Cantidad: " + cantidad);
                    cookId++; // Incrementar el ID del cocinero
                } catch (IOException e) {
                    System.err.println("Error al crear proceso para " + plato + ": " + e.getMessage());
                    shutdownProcesses(processes);
                    System.exit(1);
                }
            }

            // Esperar a que terminen los cocineros (todos menos el reponedor)
            for (Process process : processes) {
                if (process != replenisher) {
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // Terminar el reponedor
            if (replenisher != null) {
                replenisher.destroy();
            }

            // Mostrar resultados finales
            System.out.println("\n=== Resumen Final de la Cocina ===");
            System.out.println("\nStock final:");
            INITIAL_STOCK.keySet().forEach(ingredient -> 
                System.out.println("• " + ingredient + ": " + sharedMemory.getStock(ingredient)));

            System.out.println("\nPedidos completados:");
            orders.forEach((plato, cantidad) -> 
                System.out.println("✅ " + cantidad + " " + plato + "(s)"));

            System.out.println("\n¡Gracias por usar el Sistema de Cocina!");
            System.exit(0);

        } catch (IOException e) {
            System.err.println("Error al inicializar la memoria compartida: " + e.getMessage());
            System.exit(1);
        }
    }
    private static void shutdownProcesses(List<Process> processes) {
        System.out.println("\n🔄 Cerrando procesos...");
        for (Process process : processes) {
            try {
                if (process != null && process.isAlive()) {
                    // Intenta cerrar suavemente
                    process.destroy();
                    
                    // Espera hasta 1 segundo para que termine
                    if (!process.waitFor(1, TimeUnit.SECONDS)) {
                        System.out.println("⚠️ Forzando cierre del proceso...");
                        process.destroyForcibly();
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Error al esperar cierre del proceso: " + e.getMessage());
                process.destroyForcibly();
            }
        }
        processes.clear();
        System.out.println("✅ Todos los procesos cerrados correctamente");
        // TODO: liberar memoria compartida
    }
    
} 