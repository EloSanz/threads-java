package recipes;

public class Ensalada extends Recipe {
    public Ensalada() {
        super("ensalada");
    }

    @Override
    protected void initializeIngredients() {
        ingredients.put("Lechuga", 2);
        ingredients.put("Tomate", 2);
        ingredients.put("Queso", 1);
    }
} 