package recipes;

public class Taco extends Recipe {
    public Taco() {
        super("taco");
    }

    @Override
    protected void initializeIngredients() {
        ingredients.put("Tortilla", 2);
        ingredients.put("Carne", 1);
        ingredients.put("Frijoles", 1);
        ingredients.put("Aguacate", 1);
    }
} 