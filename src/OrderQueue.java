import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderQueue {
    private ConcurrentLinkedQueue<Order> orders;
    private AtomicInteger orderIdCounter;
    private volatile boolean accepting;
    private Kitchen kitchen;

    public OrderQueue(Kitchen kitchen) {
        this.orders = new ConcurrentLinkedQueue<>();
        this.orderIdCounter = new AtomicInteger(1);
        this.accepting = true;
        this.kitchen = kitchen;
    }

    public void addOrder(Recipe recipe, int quantity) {
        if (!accepting) {
            System.out.println("❌ Lo siento, no se están aceptando más pedidos");
            return;
        }

        int orderId = orderIdCounter.getAndIncrement();
        
        System.out.println("\n📝 Nuevo pedido registrado:");
        System.out.println("   • Pedido #" + orderId);
        System.out.println("   • Plato: " + recipe.getName());
        System.out.println("   • Cantidad: " + quantity);
        kitchen.printStock("Stock antes de comenzar Pedido #" + orderId);

        // Dividir el pedido en subpedidos si la cantidad es mayor a 1
        if (quantity > 1) {
            System.out.println("\n🔄 Dividiendo pedido para procesamiento paralelo:");
            for (int i = 0; i < quantity; i++) {
                Order subOrder = new Order(orderId, recipe, 1, quantity, i + 1);
                orders.offer(subOrder);
                System.out.println("   • Subpedido " + (i + 1) + "/" + quantity + " creado");
            }
        } else {
            Order order = new Order(orderId, recipe, 1, 1, 1);
            orders.offer(order);
        }
    }

    public Order getNextOrder() {
        return orders.poll();
    }

    public boolean hasOrders() {
        return !orders.isEmpty();
    }

    public void stopAccepting() {
        accepting = false;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public int getPendingOrdersCount() {
        return orders.size();
    }
} 