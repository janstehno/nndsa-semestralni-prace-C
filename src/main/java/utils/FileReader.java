package utils;

import model.Buffer;
import model.Data;
import print.Printer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileReader {

    private final Set<Data> dataset = new HashSet<>();

    private long duration;

    private int[] readHeader(RandomAccessFile file) throws IOException {
        file.seek(0);
        int[] header = new int[2];
        header[0] = file.readInt(); // Velikost bloku
        header[1] = file.readInt(); // Počet bloků
        return header;
    }

    private void readBlockToBuffer(Buffer buffer, RandomAccessFile file, int blockIndex, int blockSize) throws IOException {
        buffer.getBuffer().clear();
        file.seek(FileCreator.HEADER_SIZE + (long) blockIndex * blockSize);

        // Blokové čtení
        file.getChannel().read(buffer.getBuffer());
        buffer.notAccessingFile();
    }

    private void processBlock(Buffer buffer, int bufferNum, boolean printBuffer) {
        try {
            int recordNum = 0;

            // Zpracování dat
            buffer.getBuffer().flip();

            while (buffer.getBuffer().hasRemaining()) {
                int id = buffer.getBuffer().getInt();
                byte[] dataBytes = new byte[Data.SIZE - 4];
                buffer.getBuffer().get(dataBytes);
                String data = new String(dataBytes, StandardCharsets.UTF_8).trim();
                synchronized (dataset) {
                    dataset.add(new Data(id, data));
                }
                recordNum++;
            }

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

            Buffer buffer1 = new Buffer(ByteBuffer.allocate(blockSize));
            Buffer buffer2 = new Buffer(ByteBuffer.allocate(blockSize));

            long startTime = System.currentTimeMillis();
            AtomicLong endTime = new AtomicLong();

            AtomicInteger blockIndex = new AtomicInteger(-1);

            Thread thread = null;
            if (useSecondBuffer) {
                thread = new Thread(() -> {
                    try {
                        while (true) {
                            if (blockIndex.get() + 1 >= numBlocks) {
                                endTime.set(System.currentTimeMillis());
                                break;
                            } else if (buffer1.isNotAccessingFile()) {
                                synchronized (blockIndex) {
                                    buffer2.accessingFile();
                                }
                                blockIndex.set(blockIndex.get() + 1);
                                readBlockToBuffer(buffer2, inputFile, blockIndex.get(), blockSize);
                                processBlock(buffer2, 2, printBuffers);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            if (useSecondBuffer) thread.start();

            try {
                while (true) {
                    if (blockIndex.get() + 1 >= numBlocks) {
                        endTime.set(System.currentTimeMillis());
                        break;
                    } else if (!useSecondBuffer || (buffer2.isNotAccessingFile())) {
                        synchronized (blockIndex) {
                            buffer1.accessingFile();
                        }
                        blockIndex.set(blockIndex.get() + 1);
                        readBlockToBuffer(buffer1, inputFile, blockIndex.get(), blockSize);
                        processBlock(buffer1, 1, printBuffers);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (useSecondBuffer) thread.join();

            this.duration = endTime.get() - startTime;
            System.out.println("Čtení dat trvalo: " + Printer.formatYellow(duration + " ms"));

            inputFile.close();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return dataset;
    }

    public long getDuration() {
        return duration;
    }
}