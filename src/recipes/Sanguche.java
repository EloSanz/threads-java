package recipes;

public class Sanguche extends Recipe {
    public Sanguche() {
        super("sanguche");
    }

    @Override
    protected void initializeIngredients() {
        ingredients.put("Pan", 2);
        ingredients.put("Queso", 1);
        ingredients.put("Tomate", 1);
        ingredients.put("Lechuga", 1);
    }
} 