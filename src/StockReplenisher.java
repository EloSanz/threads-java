public class StockReplenisher extends Thread {
    private Kitchen kitchen;
    private volatile boolean running;
    private static final long CHECK_INTERVAL = 2000; // 2 segundos
    
    public StockReplenisher(Kitchen kitchen) {
        this.kitchen = kitchen;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(CHECK_INTERVAL);
                kitchen.checkAndRestockIngredients();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopReplenishing() {
        running = false;
        interrupt();
    }
} 