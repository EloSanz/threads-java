import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.util.Map;
import recipes.*;

public class CookProcess {
    private static final String SHARED_MEMORY_FILE = "kitchen_stock.dat";
    private static final String SEMAPHORE_FILE = "kitchen_semaphore.lock";
    private static final int COOKING_TIME = 2000; // 2 segundos para cocinar
    private static final int WAITING_TIME = 1000; // 1 segundo de espera si no hay ingredientes

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Uso: CookProcess <nombre_cocinero> <tipo_receta> <cantidad>");
            System.exit(1);
        }

        String cookName = args[0];
        String recipeType = args[1];
        int dishesToCook = Integer.parseInt(args[2]);
        
        try {
            try (
                RandomAccessFile sharedMemoryFile = new RandomAccessFile(SHARED_MEMORY_FILE, "rw");
                RandomAccessFile lockFile = new RandomAccessFile(SEMAPHORE_FILE, "rw");
                FileChannel lockChannel = lockFile.getChannel()
            ) {
                // Acceder a la memoria compartida
                MappedByteBuffer sharedMemory = sharedMemoryFile.getChannel().map(
                    FileChannel.MapMode.READ_WRITE, 0, 1024);

                Recipe recipe;
                switch (recipeType.toLowerCase()) {
                    case "hamburguesa":
                        recipe = new Hamburguesa();
                        break;
                    case "ensalada":
                        recipe = new Ensalada();
                        break;
                    case "taco":
                        recipe = new Taco();
                        break;
                    case "sanguche":
                        recipe = new Sanguche();
                        break;
                    default:
                        System.err.println("Receta no válida: " + recipeType);
                        System.exit(1);
                        return;
                }

                System.out.println("🧑‍🍳 Proceso cocinero " + cookName + " iniciado - Especialidad: " + recipe.getName() +
                                 " - Objetivo: " + dishesToCook + " platos");
                
                int platosPreparados = 0;
                boolean running = true;

                while (running && platosPreparados < dishesToCook) {
                    System.out.println("\n👨‍🍳 " + cookName + " intenta preparar " + recipe.getName() + 
                                     " (" + (platosPreparados + 1) + "/" + dishesToCook + ")");
                    
                    try {
                        // Obtener lock exclusivo para el archivo
                        var lock = lockChannel.lock();
                        try {
                            // Verificar ingredientes en memoria compartida
                            Map<String, Integer> ingredientes = recipe.getIngredients();
                            boolean tieneIngredientes = true;
                            
                            // Leer y verificar stock
                            for (Map.Entry<String, Integer> entry : ingredientes.entrySet()) {
                                int offset = getIngredientOffset(entry.getKey());
                                sharedMemory.position(offset);
                                int disponible = sharedMemory.getInt();
                                if (disponible < entry.getValue()) {
                                    tieneIngredientes = false;
                                    break;
                                }
                            }

                            if (tieneIngredientes) {
                                // Decrementar ingredientes en memoria compartida
                                for (Map.Entry<String, Integer> entry : ingredientes.entrySet()) {
                                    int offset = getIngredientOffset(entry.getKey());
                                    sharedMemory.position(offset);
                                    int disponible = sharedMemory.getInt();
                                    sharedMemory.position(offset);
                                    sharedMemory.putInt(disponible - entry.getValue());
                                }
                                
                                // Simular tiempo de cocción
                                System.out.println("🔥 " + cookName + " está cocinando " + recipe.getName());
                                Thread.sleep(COOKING_TIME);
                                
                                platosPreparados++;
                                System.out.println("✅ " + cookName + " completó " + recipe.getName() + " #" + platosPreparados + 
                                                 " de " + dishesToCook);
                            } else {
                                System.out.println("⏳ " + cookName + " espera por ingredientes para " + recipe.getName());
                                Thread.sleep(WAITING_TIME);
                            }
                        } finally {
                            lock.release();
                        }

                    } catch (InterruptedException e) {
                        running = false;
                    }
                }

                System.out.println("\n👋 " + cookName + " termina - Preparó " + platosPreparados + "/" + 
                                 dishesToCook + " " + recipe.getName());
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