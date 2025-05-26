import java.util.concurrent.Semaphore;

public class IngredientSemaphore {
    private String name;
    private Semaphore semaphore;
    private int maxPermits;
    private static final int RESTOCK_THRESHOLD = 5;
    private static final int RESTOCK_AMOUNT = 5;

    public IngredientSemaphore(String name, int initialPermits) {
        this.name = name;
        this.maxPermits = initialPermits;
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
        return semaphore.availablePermits() <= RESTOCK_THRESHOLD;
    }

    public void restock() {
        int currentPermits = semaphore.availablePermits();
        int toAdd = Math.min(RESTOCK_AMOUNT, maxPermits - currentPermits);
        if (toAdd > 0) {
            semaphore.release(toAdd);
        }
    }
} 