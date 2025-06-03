import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;

public class StockReplenisher {
    private static final String SHARED_MEMORY_FILE = "kitchen_stock.dat";
    private static final String SEMAPHORE_FILE = "kitchen_semaphore.lock";
    private static final long CHECK_INTERVAL = 2000; // 2 segundos
    private static final int RESTOCK_AMOUNT = 10;
    private static final int RESTOCK_THRESHOLD = 5;

    public static void main(String[] args) {

        try (
            RandomAccessFile sharedMemoryFile = new RandomAccessFile(SHARED_MEMORY_FILE, "rw");
            RandomAccessFile lockFile = new RandomAccessFile(SEMAPHORE_FILE, "rw");
            FileChannel lockChannel = lockFile.getChannel()
        ) {
            MappedByteBuffer sharedMemory = sharedMemoryFile.getChannel().map(
                FileChannel.MapMode.READ_WRITE, 0, 1024);

            while (true) {
                try {
                    Thread.sleep(CHECK_INTERVAL);
                    
                    // Obtener lock exclusivo
                    var lock = lockChannel.lock();
                    try {
                        boolean restocked = false;
                        String[] ingredients = {"Pan", "Carne", "Lechuga", "Tomate", "Queso", "Tortilla", "Salsa", "Zanahoria"};

                        for (String ingredient : ingredients) {
                            int offset = getIngredientOffset(ingredient);
                            sharedMemory.position(offset);
                            int disponible = sharedMemory.getInt();

                            if (disponible <= RESTOCK_THRESHOLD) {
                                if (!restocked) {
                                    System.out.println("\n📦 Iniciando reposición de ingredientes:");
                                    restocked = true;
                                }
                                
                                int reponer = RESTOCK_AMOUNT - disponible;
                                sharedMemory.position(offset);
                                sharedMemory.putInt(RESTOCK_AMOUNT);
                                
                                System.out.println("   • Reponiendo " + reponer + " unidades de " + ingredient.toLowerCase() +
                                                 " (Antes: " + disponible + ", Después: " + RESTOCK_AMOUNT + ")");
                            }
                        }
                        
                        if (restocked) {
                            System.out.println("✅ Reposición completada");
                        }
                    } finally {
                        lock.release();
                    }

                } catch (InterruptedException e) {
                    System.out.println("⚠️ Proceso reponedor interrumpido");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error al acceder a la memoria compartida: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int getIngredientOffset(String ingredient) {
        // Usar los mismos offsets que CookProcess
        switch (ingredient.toLowerCase()) {
            case "pan": return 0;
            case "carne": return 4;
            case "lechuga": return 8;
            case "tomate": return 12;
            case "queso": return 16;
            case "tortilla": return 20;
            case "salsa": return 24;
            case "zanahoria": return 28;
            default: return 0;
        }
    }
} 