package utils;

import model.Buffer;
import model.Data;
import print.Printer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileWriter {

    public static byte HEADER_SIZE = 8;

    private final int blockSize;

    private long duration;

    private int recordsWritten;

    public FileWriter(int blockSize) {
        this.blockSize = blockSize;
    }

    private void writeBufferToFile(RandomAccessFile file, Buffer buffer, int blockIndex) throws IOException {
        buffer.getBuffer().flip();
        file.seek(HEADER_SIZE + (long) blockIndex * blockSize);
        // Blokový zápis
        file.getChannel().write(buffer.getBuffer());
        buffer.getBuffer().clear();
        buffer.notAccessingFile();
    }

    public void write(String fileName, Set<Data> dataList, boolean useSecondBuffer) {
        try {
            List<Data> data = dataList.stream().toList();
            RandomAccessFile outputFile = new RandomAccessFile(fileName, "rw");

            // Hlavička
            int numBlocks = (int) Math.ceil((double) dataList.size() * Data.SIZE / blockSize);
            outputFile.writeInt(blockSize);
            outputFile.writeInt(numBlocks);

            Buffer buffer1 = new Buffer(ByteBuffer.allocate(blockSize));
            Buffer buffer2 = new Buffer(ByteBuffer.allocate(blockSize));

            long startTime = System.currentTimeMillis();
            AtomicLong endTime = new AtomicLong();

            AtomicInteger blockIndex = new AtomicInteger(-1);
            AtomicInteger recordIndex = new AtomicInteger(0);

            Thread thread = null;
            if (useSecondBuffer) {
                thread = new Thread(() -> {
                    try {
                        while (recordIndex.get() < data.size()) {
                            if (buffer1.isNotLoadingData()) {
                                buffer2.loadingData();
                                while (recordIndex.get() < dataList.size()) {
                                    ByteBuffer dataBuffer = data.get(recordIndex.get()).toByteBuffer();
                                    // Pokud je buffer plný, zapiš data do souboru
                                    if (buffer2.getBuffer().remaining() < dataBuffer.limit()) {
                                        synchronized (blockIndex) {
                                            buffer2.notLoadingData();
                                            while (true) {
                                                if (buffer1.isNotAccessingFile()) {
                                                    buffer2.accessingFile();
                                                    blockIndex.set(blockIndex.get() + 1);
                                                    writeBufferToFile(outputFile, buffer2, blockIndex.get());
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    } else if (buffer2.getBuffer().remaining() >= dataBuffer.limit()) {
                                        buffer2.getBuffer().put(dataBuffer);
                                        recordIndex.set(recordIndex.get() + 1);
                                    }
                                }
                            }
                        }

                        if (buffer2.getBuffer().position() > 0) {
                            blockIndex.set(blockIndex.get() + 1);
                            writeBufferToFile(outputFile, buffer2, blockIndex.get());
                        }
                        endTime.set(System.currentTimeMillis());

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            if (useSecondBuffer) thread.start();


            try {
                while (recordIndex.get() < data.size()) {
                    if (buffer2.isNotLoadingData()) {
                        buffer1.loadingData();
                        while (recordIndex.get() < dataList.size()) {
                            ByteBuffer dataBuffer = data.get(recordIndex.get()).toByteBuffer();
                            // Pokud je buffer plný, zapiš data do souboru
                            if (buffer1.getBuffer().remaining() < dataBuffer.limit()) {
                                synchronized (blockIndex) {
                                    buffer1.notLoadingData();
                                    while (true) {
                                        if (buffer2.isNotAccessingFile()) {
                                            buffer1.accessingFile();
                                            blockIndex.set(blockIndex.get() + 1);
                                            writeBufferToFile(outputFile, buffer1, blockIndex.get());
                                            break;
                                        }
                                    }
                                    break;
                                }
                            } else if (buffer1.getBuffer().remaining() >= dataBuffer.limit()) {
                                buffer1.getBuffer().put(dataBuffer);
                                recordIndex.set(recordIndex.get() + 1);
                            }
                        }
                    }
                }

                if ((buffer1.getBuffer().position() > 0)) {
                    blockIndex.set(blockIndex.get() + 1);
                    writeBufferToFile(outputFile, buffer1, blockIndex.get());
                }
                endTime.set(System.currentTimeMillis());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (useSecondBuffer) thread.join();

            this.recordsWritten = recordIndex.get();
            this.duration = endTime.get() - startTime;
            System.out.println("Zapisování dat trvalo: " + Printer.formatYellow(duration + " ms"));

            outputFile.close();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public long getDuration() {
        return duration;
    }

    public int getRecordsWritten() {
        return recordsWritten;
    }
}
