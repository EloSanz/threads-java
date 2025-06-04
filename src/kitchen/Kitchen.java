
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import recipes.Recipe;

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
        ingredients.put("Tortilla", new IngredientSemaphore("Tortilla", 10));
        ingredients.put("Salsa", new IngredientSemaphore("Salsa", 10));
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
                int available = ingredient.getAvailablePermits();
                if (ingredient.needsRestock()) {
                    if (!restocked) {
                        System.out.println("\n📦 Iniciando reposición de ingredientes:");
                    }
                    System.out.println("\n⚠️ " + ingredient.getName() + " bajo mínimo:");
                    System.out.println("   • Disponible: " + available);
                    System.out.println("   • Mínimo requerido: " + ingredient.getRestockThreshold());
                    ingredient.restock();
                    restocked = true;
                }
            }
            
            if (restocked) {
                System.out.println("\n📊 Estado final de la cocina:");
                printStock("Inventario después de reposición");
            }
        } finally {
            lock.unlock();
        }
    }

    public void printStock(String message) {
        lock.lock();
        try {
            if (message.startsWith("Stock después de completar Pedido")) {
                String[] parts = message.split("#");
                if (parts.length > 1) {
                    System.out.println("\n🍽️ Resumen del pedido #" + parts[1] + ":");
                }
            }
            
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