import java.util.concurrent.atomic.AtomicInteger;

public class Order {
    private int orderId;
    private Recipe recipe;
    private int quantity;
    private AtomicInteger completed;

    public Order(int orderId, Recipe recipe, int quantity) {
        this.orderId = orderId;
        this.recipe = recipe;
        this.quantity = quantity;
        this.completed = new AtomicInteger(0);
    }

    public int getOrderId() {
        return orderId;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public int getQuantity() {
        return quantity;
    }

    public void incrementCompleted() {
        completed.incrementAndGet();
    }

    public boolean isCompleted() {
        return completed.get() >= quantity;
    }

    @Override
    public String toString() {
        return "Pedido #" + orderId + ": " + recipe.getName();
    }
} 