package models;

import java.util.concurrent.atomic.AtomicInteger;
import recipes.Recipe;

public class Order {
    private int orderId;
    private Recipe recipe;
    private int quantity;
    private AtomicInteger completed;
    private int totalSuborders;
    private int suborderNumber;

    public Order(int orderId, Recipe recipe, int quantity, int totalSuborders, int suborderNumber) {
        this.orderId = orderId;
        this.recipe = recipe;
        this.quantity = quantity;
        this.completed = new AtomicInteger(0);
        this.totalSuborders = totalSuborders;
        this.suborderNumber = suborderNumber;
    }

    public int getOrderId() {
        return orderId;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public int getQuantity() {
        return quantity;
    }

    public void incrementCompleted() {
        completed.incrementAndGet();
    }

    public int getCompleted() {
        return completed.get();
    }

    public boolean isCompleted() {
        return completed.get() >= quantity;
    }

    public int getTotalSuborders() {
        return totalSuborders;
    }

    public int getSuborderNumber() {
        return suborderNumber;
    }

    public String getProgressInfo() {
        if (totalSuborders > 1) {
            return "Subpedido " + suborderNumber + "/" + totalSuborders;
        }
        return "";
    }

    @Override
    public String toString() {
        return "Pedido #" + orderId + ": " + recipe.getName() + 
               (totalSuborders > 1 ? " (" + getProgressInfo() + ")" : "");
    }
} 