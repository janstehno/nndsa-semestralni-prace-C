package utils;

import model.Data;
import print.Printer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class FileReader {

    private final Set<Data> dataset = new HashSet<>();

    private int[] readHeader(RandomAccessFile file) throws IOException {
        file.seek(0);
        int[] header = new int[2];
        header[0] = file.readInt(); // Velikost bloku
        header[1] = file.readInt(); // Počet bloků
        return header;
    }

    private void readBlockToBuffer(ByteBuffer buffer, RandomAccessFile file, int blockIndex, int blockSize) throws IOException {
        buffer.clear();
        file.seek(FileCreator.HEADER_SIZE + (long) blockIndex * blockSize);
        // Blokové čtení
        file.getChannel().read(buffer);
        buffer.flip();
    }

    private void processBlock(ByteBuffer buffer, int bufferNum, boolean printBuffer) {
        try {
            int recordNum = 0;

            // Zpracování dat
            while (buffer.hasRemaining()) {
                int id = buffer.getInt();
                byte[] dataBytes = new byte[Data.SIZE - 4];
                buffer.get(dataBytes);
                String data = new String(dataBytes, StandardCharsets.UTF_8).trim();
                synchronized (dataset) {
                    dataset.add(new Data(id, data));
                }
                recordNum++;
            }

            // Thread.sleep(1);

            // Výpis do konzole
            if (printBuffer) System.out.format(Printer.formatRed(String.format("B%s: %d\n", bufferNum, recordNum)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Set<Data> read(String fileName, boolean useSecondBuffer, boolean printBuffers) {
        dataset.clear();
        try {
            RandomAccessFile inputFile = new RandomAccessFile(fileName, "r");

            int[] header = readHeader(inputFile);
            int blockSize = header[0];
            int numBlocks = header[1];

            ByteBuffer buffer1 = ByteBuffer.allocate(blockSize);
            ByteBuffer buffer2 = ByteBuffer.allocate(blockSize);

            Thread thread1 = new Thread(() -> {
                try {
                    int index = 0;
                    while (index < numBlocks) {
                        readBlockToBuffer(buffer1, inputFile, index, blockSize);
                        processBlock(buffer1, 1, printBuffers);
                        if (useSecondBuffer) {
                            index += 2;
                        } else {
                            index += 1;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Thread thread2 = null;
            if (useSecondBuffer) {
                thread2 = new Thread(() -> {
                    try {
                        int index = 1;
                        while (index < numBlocks) {
                            readBlockToBuffer(buffer2, inputFile, index, blockSize);
                            processBlock(buffer2, 2, printBuffers);
                            index += 2;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            long startTime = System.currentTimeMillis();

            thread1.start();
            if (useSecondBuffer) thread2.start();

            thread1.join();
            if (useSecondBuffer) thread2.join();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Čtení dat trvalo: " + Printer.formatYellow(duration + " ms"));

            inputFile.close();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return dataset;
    }
}