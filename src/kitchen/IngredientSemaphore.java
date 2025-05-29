package kitchen;

import java.util.concurrent.Semaphore;

public class IngredientSemaphore {
    private String name;
    private Semaphore semaphore;
    private int maxPermits;
    private int restockThreshold;

    public IngredientSemaphore(String name, int initialPermits) {
        this.name = name;
        this.maxPermits = initialPermits;
        this.restockThreshold = maxPermits / 2;
        this.semaphore = new Semaphore(initialPermits, true);
    }

    public void acquire(int permits) throws InterruptedException {
        semaphore.acquire(permits);
    }

    public void release(int permits) {
        semaphore.release(permits);
    }

    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }

    public String getName() {
        return name;
    }

    public boolean needsRestock() {
        return getAvailablePermits() <= restockThreshold;
    }

    public int getRestockThreshold() {
        return restockThreshold;
    }

    public void restock() {
        int currentPermits = getAvailablePermits();
        int permitsToAdd = maxPermits - currentPermits;
        if (permitsToAdd > 0) {
            semaphore.release(permitsToAdd);
            System.out.println("   • Reponiendo " + permitsToAdd + " unidades de " + name.toLowerCase());
        }
    }
} 