import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class SharedMemory {
    private static final String SHARED_FILE = "kitchen_stock.dat";
    private static final int BUFFER_SIZE = 1024;
    private MappedByteBuffer buffer;
    private Map<String, Integer> stockOffsets;

    public SharedMemory() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(SHARED_FILE, "rw")) {
            buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, BUFFER_SIZE);
        }
        stockOffsets = new HashMap<>();
        initializeOffsets();
    }

    private void initializeOffsets() {
        // Definir offsets para cada ingrediente
        int offset = 0;
        String[] ingredients = {
            "Pan", "Carne", "Lechuga", "Tomate", "Queso",
            "Tortilla", "Frijoles", "Arroz", "Aguacate"
        };
        
        for (String ingredient : ingredients) {
            stockOffsets.put(ingredient, offset);
            offset += 4; // 4 bytes por ingrediente (int)
        }
    }

    public void setStock(String ingredient, int quantity) {
        Integer offset = stockOffsets.get(ingredient);
        if (offset != null) {
            buffer.position(offset);
            buffer.putInt(quantity);
        }
    }

    public int getStock(String ingredient) {
        Integer offset = stockOffsets.get(ingredient);
        if (offset != null) {
            buffer.position(offset);
            return buffer.getInt();
        }
        return 0;
    }

    public boolean checkAndDecrementIngredients(Map<String, Integer> needed) {
        // Verificar si hay suficientes ingredientes
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            if (getStock(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        // Decrementar ingredientes
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            int currentStock = getStock(entry.getKey());
            setStock(entry.getKey(), currentStock - entry.getValue());
        }
        return true;
    }
} 