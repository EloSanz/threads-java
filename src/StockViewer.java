import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;

public class StockViewer {
    private static final String SHARED_MEMORY_FILE = "kitchen_stock.dat";
    private static final Map<String, Integer> OFFSETS = new LinkedHashMap<String, Integer>() {{
        put("Pan", 0);
        put("Carne", 4);
        put("Lechuga", 8);
        put("Tomate", 12);
        put("Queso", 16);
        put("Tortilla", 20);
        put("Salsa", 24);
        put("Zanahoria", 28);
    }};

    public static void main(String[] args) {
        System.out.println("\n=== Monitor de Stock en Tiempo Real ===\n");
        System.out.println("Presiona Ctrl+C para salir\n");

        try (RandomAccessFile file = new RandomAccessFile(SHARED_MEMORY_FILE, "r")) {
            MappedByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 1024);

            while (true) {
                // Limpiar pantalla (ANSI escape code)
                System.out.print("\033[H\033[2J");
                System.out.flush();
                
                System.out.println("=== Monitor de Stock en Tiempo Real ===");
                System.out.println("Presiona Ctrl+C para salir\n");
                System.out.println("Stock actual:");
                System.out.println("------------------------");

                for (Map.Entry<String, Integer> entry : OFFSETS.entrySet()) {
                    buffer.position(entry.getValue());
                    int cantidad = buffer.getInt();
                    String status = cantidad <= 5 ? "⚠️ BAJO" : "✅ OK ";
                    System.out.printf("%-12s: %3d  %s%n", entry.getKey(), cantidad, status);
                }

                System.out.println("------------------------");
                
                // Esperar 500ms antes de actualizar
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.err.println("Error al leer la memoria compartida: " + e.getMessage());
            System.exit(1);
        }
    }
} 