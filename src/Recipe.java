import java.util.HashMap;
import java.util.Map;

public abstract class Recipe {
    protected String name;
    protected Map<String, Integer> ingredients;

    public Recipe(String name) {
        this.name = name;
        this.ingredients = new HashMap<>();
        initializeIngredients();
    }

    protected abstract void initializeIngredients();

    public String getName() {
        return name;
    }

    public Map<String, Integer> getIngredients() {
        return ingredients;
    }

    @Override
    public String toString() {
        return name;
    }
} 