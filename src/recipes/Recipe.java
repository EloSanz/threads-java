package recipes;

import java.util.HashMap;
import java.util.Map;

public abstract class Recipe {
    protected final String name;
    protected final Map<String, Integer> ingredients;

    protected Recipe(String name) {
        this.name = name;
        this.ingredients = new HashMap<>();
        initializeIngredients();
    }

    // Método abstracto que cada receta debe implementar para definir sus ingredientes
    protected abstract void initializeIngredients();

    public String getName() {
        return name;
    }

    public Map<String, Integer> getIngredients() {
        return new HashMap<>(ingredients); // Retorna una copia para evitar modificaciones externas
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (Ingredientes: ");
        ingredients.forEach((ingredient, quantity) -> 
            sb.append(ingredient).append("x").append(quantity).append(", "));
        sb.setLength(sb.length() - 2); // Remover última coma y espacio
        sb.append(")");
        return sb.toString();
    }
} 