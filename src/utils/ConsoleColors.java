package utils;

public class ConsoleColors {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private ConsoleColors() {
        // Utility class, no instantiation
    }

    public static String getColorForCook(String cookName) {
        switch (cookName) {
            case "Juan":
                return ANSI_GREEN;
            case "María":
                return ANSI_YELLOW;
            case "Pedro":
                return ANSI_BLUE;
            case "Ana":
                return ANSI_CYAN;
            default:
                return ANSI_RESET;
        }
    }
}