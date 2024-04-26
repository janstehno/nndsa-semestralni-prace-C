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

    private final static int BLOCK_SIZE = 1000;
    private final static int BLOCK_NUM = 1000;

    public static void main(String[] args) {
        System.out.print("Vypisovat obsahy bufferů [Y/*]: ");
        String print = new Scanner(System.in).nextLine();

        System.out.println("Vytváření souboru (velikost záznamu: " + Printer.formatGreen(Data.SIZE + "B") + ", záznamů v bloku: " + Printer.formatGreen(String.valueOf(
                BLOCK_SIZE)) + ", počet bloků: " + Printer.formatGreen(String.valueOf(BLOCK_NUM)) + ")...");
        FileCreator fileCreator = new FileCreator(BLOCK_SIZE, BLOCK_NUM);
        fileCreator.create(INPUT_FILE);

        System.out.println();

        System.out.println("Čtení souboru s " + Printer.formatBlue("jedním") + " bufferem...");
        FileReader fileReaderOneBuffer = new FileReader();
        fileReaderOneBuffer.read(INPUT_FILE, false, false);

        System.out.println("Čtení souboru se " + Printer.formatBlue("dvěma") + " buffery...");
        FileReader fileReaderTwoBuffers = new FileReader();
        Set<Data> dataset = fileReaderTwoBuffers.read(INPUT_FILE, true, print.toUpperCase().startsWith("Y"));

        System.out.println("Přečteno " + Printer.formatPurple(String.valueOf(dataset.size())) + " záznamů\n");

        System.out.println("Zápis do souboru (velikost záznamu: " + Printer.formatGreen(Data.SIZE + "B") + ", záznamů v bloku: " + Printer.formatGreen(String.valueOf(
                BLOCK_SIZE)) + ")...");
        FileWriter fileWriter = new FileWriter(BLOCK_SIZE);
        fileWriter.write(OUTPUT_FILE, dataset);

        System.out.println("Čtení zapsaného souboru se " + Printer.formatBlue("dvěma") + " buffery...");
        FileReader fileReaderTwoBuffersAfterWrite = new FileReader();
        Set<Data> datasetAfterWrite = fileReaderTwoBuffersAfterWrite.read(OUTPUT_FILE, true, false);

        System.out.println("Přečteno " + Printer.formatPurple(String.valueOf(datasetAfterWrite.size())) + " záznamů");
    }
}
