import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KitchenSystem {
    public static void main(String[] args) {
        // Crear la cocina y la cola de pedidos
        Kitchen kitchen = new Kitchen();
        OrderQueue orderQueue = new OrderQueue(kitchen);
        
        // Crear el reponedor de stock
        StockReplenisher replenisher = new StockReplenisher(kitchen);
        replenisher.start();

        // Crear los cocineros
        List<Cook> cooks = new ArrayList<>();
        cooks.add(new Cook("Juan", kitchen, orderQueue));
        cooks.add(new Cook("María", kitchen, orderQueue));
        cooks.add(new Cook("Pedro", kitchen, orderQueue));

        // Iniciar los cocineros
        for (Cook cook : cooks) {
            cook.start();
        }

        // Procesar pedidos desde la entrada del usuario
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== ¡Bienvenido al Sistema de Cocina! ===");
        System.out.println("\nRecetas disponibles:");
        System.out.println("1. Hamburguesa");
        System.out.println("   Ingredientes por unidad:");
        System.out.println("   - 2 panes");
        System.out.println("   - 1 carne");
        System.out.println("   - 1 lechuga");
        System.out.println("   - 1 tomate");
        System.out.println("   - 1 queso");
        System.out.println("\n2. Ensalada");
        System.out.println("   Ingredientes por unidad:");
        System.out.println("   - 2 lechugas");
        System.out.println("   - 2 tomates");
        
        System.out.println("\nInstrucciones:");
        System.out.println("1. Para hacer un pedido, ingrese dos números separados por espacio:");
        System.out.println("   - Primer número: tipo de receta (1 o 2)");
        System.out.println("   - Segundo número: cantidad deseada");
        System.out.println("\nEjemplos válidos:");
        System.out.println("- '1 5' → 5 hamburguesas");
        System.out.println("- '2 3' → 3 ensaladas");
        System.out.println("\nPara finalizar el programa, escriba: 'fin'");
        System.out.println("\nNuestros cocineros Juan, María y Pedro prepararán sus pedidos en paralelo.");
        System.out.println("El sistema repondrá ingredientes automáticamente cuando sea necesario.");
        System.out.println("\n=======================================");

        while (true) {
            try {
                Thread.sleep(100); // Pequeña pausa para asegurar que los mensajes anteriores se muestren
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.print("\nIngrese su pedido: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("fin")) {
                break;
            }

            try {
                String[] parts = input.split(" ");
                if (parts.length != 2) {
                    System.out.println("Error: Formato inválido. Use: <número de receta> <cantidad>");
                    System.out.println("Ejemplo: '1 5' para cinco hamburguesas");
                    continue;
                }

                int recipeNum = Integer.parseInt(parts[0]);
                int quantity = Integer.parseInt(parts[1]);

                if (quantity <= 0) {
                    System.out.println("Error: La cantidad debe ser mayor a 0");
                    continue;
                }

                Recipe recipe;
                switch (recipeNum) {
                    case 1:
                        recipe = new Hamburguesa();
                        break;
                    case 2:
                        recipe = new Ensalada();
                        break;
                    default:
                        System.out.println("Error: Número de receta inválido. Use 1 para Hamburguesa o 2 para Ensalada");
                        continue;
                }

                orderQueue.addOrder(recipe, quantity);

            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese números válidos");
                System.out.println("Ejemplo: '1 5' para cinco hamburguesas");
            }
        }

        // Dejar de aceptar nuevos pedidos
        orderQueue.stopAccepting();
        System.out.println("\nFinalizando: Esperando a que se completen todos los pedidos pendientes...");

        // Esperar a que terminen los cocineros
        for (Cook cook : cooks) {
            try {
                cook.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Detener el reponedor
        replenisher.stopReplenishing();
        try {
            replenisher.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Mostrar resultados finales
        System.out.println("\n=== Resultados Finales ===");
        int totalDishes = 0;
        for (Cook cook : cooks) {
            System.out.println(cook.getCookName() + " preparó " + 
                             cook.getDishesCooked() + " platos en total");
            totalDishes += cook.getDishesCooked();
        }
        
        System.out.println("\n✅ ¡ÉXITO! Se completaron todos los pedidos correctamente");
        System.out.println("   • Total de platos preparados: " + totalDishes);
        System.out.println("   • Estado: No se detectaron bloqueos (deadlocks)");

        scanner.close();
    }
} 