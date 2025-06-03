import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class OrderInput {
    private static final Map<String, String> MENU = new HashMap<String, String>() {{
        put("1", "hamburguesa");
        put("2", "ensalada");
        put("3", "taco");
        put("4", "sanguche");
    }};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean systemRunning = true;

        while (systemRunning) {
            // Limpiar la pantalla
            System.out.print("\033[H\033[2J");
            System.out.flush();
            
            System.out.println("\n=== Sistema de Pedidos de Cocina ===\n");
            Map<String, Integer> orders = new HashMap<>();
            
            boolean orderInProgress = true;
            while (orderInProgress) {
                System.out.println("\nMenú disponible:");
                System.out.println("1. Hamburguesa");
                System.out.println("2. Ensalada");
                System.out.println("3. Taco");
                System.out.println("4. Sanguche");
                System.out.println("5. Finalizar y enviar a cocina");
                System.out.println("6. Salir del sistema");
                
                if (!orders.isEmpty()) {
                    System.out.println("\nPedido actual:");
                    orders.forEach((plato, cantidad) -> 
                        System.out.println("• " + plato + ": " + cantidad));
                }

                System.out.print("\nSeleccione una opción: ");
                String option = scanner.nextLine().trim();

                if (option.equals("6")) {
                    System.out.println("\n👋 ¡Gracias por usar el Sistema de Cocina!");
                    systemRunning = false;
                    orderInProgress = false;
                    continue;
                }

                if (option.equals("5")) {
                    if (orders.isEmpty()) {
                        System.out.println("\n⚠️ No hay platos en el pedido");
                        continue;
                    }
                    orderInProgress = false;
                    
                    // Iniciar los procesos de cocina con las órdenes
                    try {
                        ProcessBuilder pb = new ProcessBuilder(
                            "java", "-cp", "out", "KitchenSystem",
                            orders.entrySet().stream()
                                .map(e -> e.getKey() + ":" + e.getValue())
                                .reduce((a, b) -> a + "," + b)
                                .get()
                        );
                        pb.inheritIO();
                        Process process = pb.start();
                        process.waitFor();
                        
                        System.out.println("\n✅ Pedido completado!");
                        System.out.println("\nPresione ENTER para continuar...");
                        scanner.nextLine();
                        continue;
                    } catch (Exception e) {
                        System.err.println("Error al iniciar la cocina: " + e.getMessage());
                        System.exit(1);
                    }
                }

                if (!MENU.containsKey(option)) {
                    System.out.println("\n⚠️ Opción no válida");
                    continue;
                }

                System.out.print("Cantidad de " + MENU.get(option) + "s: ");
                int cantidad;
                try {
                    cantidad = Integer.parseInt(scanner.nextLine().trim());
                    if (cantidad <= 0) {
                        System.out.println("\n⚠️ La cantidad debe ser mayor a 0");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\n⚠️ Por favor ingrese un número válido");
                    continue;
                }

                String plato = MENU.get(option);
                orders.put(plato, orders.getOrDefault(plato, 0) + cantidad);
                System.out.println("\n✅ Agregado al pedido: " + cantidad + " " + plato + "(s)");
            }
        }
        scanner.close();
    }
} 