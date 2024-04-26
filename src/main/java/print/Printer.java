package print;

public class Printer {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_PURPLE = "\u001B[35m";

    public static String formatBlue(String text) {return ANSI_BLUE + text + ANSI_RESET;}

    public static String formatYellow(String text) {
        return ANSI_YELLOW + text + ANSI_RESET;
    }

    public static String formatRed(String text) {
        return ANSI_RED + text + ANSI_RESET;
    }

    public static String formatGreen(String text) {
        return ANSI_GREEN + text + ANSI_RESET;
    }

    public static String formatPurple(String text) {
        return ANSI_PURPLE + text + ANSI_RESET;
    }

}
