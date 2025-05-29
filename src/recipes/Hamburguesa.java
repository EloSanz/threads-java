package recipes;

public class Hamburguesa extends Recipe {
    public Hamburguesa() {
        super("hamburguesa");
    }

    @Override
    protected void initializeIngredients() {
        ingredients.put("Pan", 2);
        ingredients.put("Carne", 1);
        ingredients.put("Lechuga", 1);
        ingredients.put("Tomate", 1);
        ingredients.put("Queso", 1);
    }
} 