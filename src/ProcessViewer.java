

public class ProcessViewer {
    private static final int CHECK_INTERVAL = 1000; // 1 segundo

    public static void main(String[] args) {
        try {
            while (true) {
                // Limpiar la pantalla (compatible con Unix/Windows)
                System.out.print("\033[H\033[2J");
                System.out.flush();

                System.out.println("🔍 Monitor de Procesos de Cocina");
                System.out.println("================================");
                
                // Contador de procesos encontrados
                final int[] count = {0};
                
                // Buscar procesos de cocineros y reponedor
                ProcessHandle.allProcesses()
                    .filter(process -> {
                        String cmd = process.info().command().orElse("");
                        String[] processArgs = process.info().arguments().orElse(new String[0]);
                        String argsStr = String.join(" ", processArgs);
                        return cmd.contains("java") && 
                               (argsStr.contains("CookProcess") || 
                                argsStr.contains("StockReplenisher"));
                    })
                    .forEach(process -> {
                        count[0]++;
                        String[] processArgs = process.info().arguments().orElse(new String[0]);
                        String argsStr = String.join(" ", processArgs);
                        
                        if (argsStr.contains("CookProcess")) {
                            System.out.println("👨‍🍳 Cocinero (PID: " + process.pid() + ")");
                            System.out.printf("  Estado: %s%n", process.isAlive() ? "🟢 Activo" : "🔴 Inactivo");
                            
                            // Buscar los argumentos reales del cocinero después de "CookProcess"
                            int cookIndex = -1;
                            for (int i = 0; i < processArgs.length; i++) {
                                if (processArgs[i].contains("CookProcess")) {
                                    cookIndex = i;
                                    break;
                                }
                            }
                            
                            // Si encontramos CookProcess y hay suficientes argumentos después
                            if (cookIndex >= 0 && cookIndex + 2 < processArgs.length) {
                                System.out.printf("  Nombre: %s%n", processArgs[cookIndex + 1]);
                                System.out.printf("  Especialidad: %s%n", processArgs[cookIndex + 2]);
                            }
                        } else {
                            System.out.println("📦 Reponedor (PID: " + process.pid() + ")");
                            System.out.printf("  Estado: %s%n", process.isAlive() ? "🟢 Activo" : "🔴 Inactivo");
                        }
                        System.out.println();
                    });
                
                if (count[0] == 0) {
                    System.out.println("\nEsperando cocineros o reponedor...");
                }

                Thread.sleep(CHECK_INTERVAL);
            }
        } catch (InterruptedException e) {
            System.out.println("Monitor de procesos finalizado.");
        }
    }
} 