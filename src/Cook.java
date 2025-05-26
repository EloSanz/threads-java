public class Cook extends Thread {
    private String name;
    private Kitchen kitchen;
    private OrderQueue orderQueue;
    private int dishesCooked;
    private boolean running;
    private String color;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public Cook(String name, Kitchen kitchen, OrderQueue orderQueue) {
        this.name = name;
        this.kitchen = kitchen;
        this.orderQueue = orderQueue;
        this.dishesCooked = 0;
        this.running = true;
        
        switch (name) {
            case "Juan":
                this.color = ANSI_GREEN;
                break;
            case "María":
                this.color = ANSI_YELLOW;
                break;
            case "Pedro":
                this.color = ANSI_BLUE;
                break;
            default:
                this.color = ANSI_RESET;
                break;
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Order order = orderQueue.getNextOrder();
                if (order == null) {
                    if (!orderQueue.isAccepting() && !orderQueue.hasOrders()) {
                        System.out.println(color + "👨‍🍳 " + name + " ha terminado su turno - No hay más pedidos pendientes." + ANSI_RESET);
                        running = false;
                        break;
                    }
                    Thread.sleep(1000);
                    continue;
                }

                System.out.println("\n" + color + "👨‍🍳 " + name + " comienza a preparar " + order.getRecipe().getName() + 
                                 (order.getTotalSuborders() > 1 ? " (" + order.getProgressInfo() + ")" : "") +
                                 " [Pedido #" + order.getOrderId() + "]" + ANSI_RESET);

                if (kitchen.acquireIngredients(order.getRecipe())) {
                    Thread.sleep(2000);
                    
                    dishesCooked++;
                    order.incrementCompleted();
                    System.out.println(color + "✨ " + name + " ha completado " + order.getRecipe().getName() + 
                                     (order.getTotalSuborders() > 1 ? " (" + order.getProgressInfo() + ")" : "") +
                                     " [Pedido #" + order.getOrderId() + "]" + ANSI_RESET);

                    // Solo mostrar el mensaje de completado cuando es el último subpedido
                    if (order.getSuborderNumber() == order.getTotalSuborders()) {
                        System.out.println(color + "🎉 ¡PEDIDO #" + order.getOrderId() + " COMPLETADO!" + ANSI_RESET);
                        System.out.println(color + "   • Plato: " + order.getRecipe().getName() + ANSI_RESET);
                        System.out.println(color + "   • Cantidad total: " + order.getTotalSuborders() + ANSI_RESET);
                        System.out.println(color + "   • Cocineros participantes: " + name + 
                                         (order.getTotalSuborders() > 1 ? " y otros" : "") + ANSI_RESET);
                        System.out.println(color + "   • Tiempo de preparación: " + 
                                         (order.getTotalSuborders() * 2) + " segundos" + ANSI_RESET);
                        kitchen.printStock("Stock después de completar Pedido #" + order.getOrderId());
                    }
                } else {
                    System.out.println(color + "⚠️ " + name + " no pudo obtener los ingredientes para " + 
                                     order.getRecipe().getName() +
                                     (order.getTotalSuborders() > 1 ? " (" + order.getProgressInfo() + ")" : "") +
                                     " [Pedido #" + order.getOrderId() + "] - Reintentando más tarde" + ANSI_RESET);
                    // Reencolar el subpedido
                    orderQueue.addOrder(order.getRecipe(), 1);
                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stopCooking() {
        running = false;
        interrupt();
    }

    public int getDishesCooked() {
        return dishesCooked;
    }

    public String getCookName() {
        return name;
    }
} 