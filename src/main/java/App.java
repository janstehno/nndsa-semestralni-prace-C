import utils.FileCreator;
import model.Data;
import print.Printer;
import utils.FileReader;
import utils.FileWriter;

import java.util.Scanner;
import java.util.Set;

public class App {
    private final static String INPUT_FILE = "./src/main/resources/heap_file_read.bin";
    private final static String OUTPUT_FILE = "./src/main/resources/heap_file_write.bin";

    private final static int BLOCK_SIZE = 1000 * Data.SIZE;
    private final static int BLOCK_NUM = 1000;

    public static void main(String[] args) {
        System.out.print("Vypisovat obsahy bufferů [Y/*]: ");
        String print = new Scanner(System.in).nextLine();

        System.out.println("Vytváření souboru pro čtení (velikost záznamu: " + Printer.formatGreen(Data.SIZE + "B") + ", záznamů v bloku: " + Printer.formatGreen(String.valueOf(
                BLOCK_SIZE / Data.SIZE)) + ", počet bloků: " + Printer.formatGreen(String.valueOf(BLOCK_NUM)) + ")...");
        FileCreator fileCreator = new FileCreator(BLOCK_SIZE, BLOCK_NUM);
        fileCreator.create(INPUT_FILE);

        System.out.println();

        Set<Data> dataset = read(print);
        write(dataset);
    }

    private static Set<Data> read(String print) {
        FileReader fileReader = new FileReader();

        System.out.println("Čtení souboru s " + Printer.formatBlue("jedním") + " bufferem...");
        Set<Data> dataset = fileReader.read(INPUT_FILE, false, print.toUpperCase().startsWith("Y"));
        long oneBufferDuration = fileReader.getDuration();

        System.out.println("Přečteno " + Printer.formatPurple(String.valueOf(dataset.size())) + " záznamů");

        System.out.println("Čtení souboru se " + Printer.formatBlue("dvěma") + " buffery...");
        dataset = fileReader.read(INPUT_FILE, true, print.toUpperCase().startsWith("Y"));
        long twoBufferDuration = fileReader.getDuration();

        System.out.println("Přečteno " + Printer.formatPurple(String.valueOf(dataset.size())) + " záznamů");

        double ratio = (double) oneBufferDuration / twoBufferDuration;
        System.out.println("Čtení se dvěma buffery bylo " + Printer.formatPurple(String.format("%.2f",
                                                                                               ratio) + "x") + " rychlejší než čtení s jedním bufferem\n");

        return dataset;
    }

    private static void write(Set<Data> dataset) {
        FileWriter fileWriter = new FileWriter(BLOCK_SIZE);

        System.out.println("Zápis do souboru s " + Printer.formatBlue("jedním") + " bufferem...");
        fileWriter.write(OUTPUT_FILE, dataset, false);
        long oneBufferDuration = fileWriter.getDuration();

        System.out.println("Zapsáno " + Printer.formatPurple(String.valueOf(fileWriter.getRecordsWritten())) + " záznamů");

        System.out.println("Zápis do souboru se " + Printer.formatBlue("dvěma") + " buffery...");
        fileWriter.write(OUTPUT_FILE, dataset, true);
        long twoBufferDuration = fileWriter.getDuration();

        System.out.println("Zapsáno " + Printer.formatPurple(String.valueOf(fileWriter.getRecordsWritten())) + " záznamů");

        double ratio = (double) oneBufferDuration / twoBufferDuration;
        System.out.println("Zápis se dvěma buffery byl " + Printer.formatPurple(String.format("%.2f",
                                                                                              ratio) + "x") + " rychlejší než zápis s jedním bufferem\n");
    }
}
