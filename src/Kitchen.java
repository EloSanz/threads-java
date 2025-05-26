import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Kitchen {
    private Map<String, IngredientSemaphore> ingredients;
    private ReentrantLock lock;

    public Kitchen() {
        this.ingredients = new HashMap<>();
        this.lock = new ReentrantLock();
        initializeIngredients();
    }

    private void initializeIngredients() {
        ingredients.put("Pan", new IngredientSemaphore("Pan", 10));
        ingredients.put("Carne", new IngredientSemaphore("Carne", 10));
        ingredients.put("Lechuga", new IngredientSemaphore("Lechuga", 10));
        ingredients.put("Tomate", new IngredientSemaphore("Tomate", 10));
        ingredients.put("Queso", new IngredientSemaphore("Queso", 10));
    }

    public boolean acquireIngredients(Recipe recipe) {
        lock.lock();
        try {
            Map<String, Integer> needed = recipe.getIngredients();
            for (Map.Entry<String, Integer> entry : needed.entrySet()) {
                IngredientSemaphore sem = ingredients.get(entry.getKey());
                if (sem.getAvailablePermits() < entry.getValue()) {
                    return false;
                }
            }

            for (Map.Entry<String, Integer> entry : needed.entrySet()) {
                try {
                    ingredients.get(entry.getKey()).acquire(entry.getValue());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void checkAndRestockIngredients() {
        lock.lock();
        try {
            boolean restocked = false;
            for (IngredientSemaphore ingredient : ingredients.values()) {
                if (ingredient.needsRestock()) {
                    ingredient.restock();
                    restocked = true;
                }
            }
            if (restocked) {
                printStock("📦 Stock después de reposición");
            }
        } finally {
            lock.unlock();
        }
    }

    public void printStock(String message) {
        lock.lock();
        try {
            System.out.println("\n📊 " + message);
            System.out.println("------------------------");
            for (IngredientSemaphore ingredient : ingredients.values()) {
                int available = ingredient.getAvailablePermits();
                String status = available < 5 ? "⚠️ Bajo" : "✅ OK";
                System.out.printf("   • %-10s: %3d disponibles  %s%n", 
                    ingredient.getName().toLowerCase(), 
                    available,
                    status);
            }
            System.out.println("------------------------");
        } finally {
            lock.unlock();
        }
    }
} 