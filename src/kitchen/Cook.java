

import recipes.Recipe;
import utils.ConsoleColors;

public class Cook extends Thread {
    private String name;
    private Kitchen kitchen;
    private Recipe fixedRecipe;  // Receta fija para este cocinero
    private int dishesCooked;
    private boolean running;
    private String color;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_WAIT_TIME = 2000; // 2 segundos

    public Cook(String name, Kitchen kitchen, Recipe fixedRecipe) {
        this.name = name;
        this.kitchen = kitchen;
        this.fixedRecipe = fixedRecipe;
        this.dishesCooked = 0;
        this.running = true;
        this.color = ConsoleColors.getColorForCook(name);
    }

    @Override
    public void run() {
        while (running) {
            try {
                System.out.println("\n" + color + "👨‍🍳 " + name + " intenta preparar " + fixedRecipe.getName() + ConsoleColors.ANSI_RESET);

                boolean acquired = false;
                int retryCount = 0;

                while (!acquired && retryCount < MAX_RETRIES && running) {
                    acquired = kitchen.acquireIngredients(fixedRecipe);
                    if (acquired) {
                        Thread.sleep(2000); // Tiempo de cocción
                        dishesCooked++;
                        System.out.println(color + "✨ " + name + " ha completado " + fixedRecipe.getName() + ConsoleColors.ANSI_RESET);
                        kitchen.printStock("Stock después de que " + name + " preparó " + fixedRecipe.getName());
                    } else {
                        retryCount++;
                        System.out.println(color + "⚠️ " + name + " no pudo obtener ingredientes para " + 
                                         fixedRecipe.getName() + " (intento " + retryCount + "/" + MAX_RETRIES + 
                                         ") - Esperando..." + ConsoleColors.ANSI_RESET);
                        Thread.sleep(RETRY_WAIT_TIME);
                    }
                }

                if (!acquired) {
                    System.out.println(color + "❌ " + name + " no pudo completar " + fixedRecipe.getName() + 
                                     " después de " + MAX_RETRIES + " intentos - Finalizando" + ConsoleColors.ANSI_RESET);
                    running = false;
                }

            } catch (InterruptedException e) {
                running = false;
            }
        }

        System.out.println(color + "👋 " + name + " termina su turno - Preparó " + dishesCooked + 
                         " " + fixedRecipe.getName() + "(s)" + ConsoleColors.ANSI_RESET);
    }

    public void stopCooking() {
        running = false;
        interrupt();
    }

    public int getDishesCooked() {
        return dishesCooked;
    }

    public String getCookName() {
        return name;
    }

    public String getRecipeName() {
        return fixedRecipe.getName();
    }
} 