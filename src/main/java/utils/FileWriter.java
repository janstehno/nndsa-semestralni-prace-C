package utils;

import model.Data;
import print.Printer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Set;

public record FileWriter(int blockSize) {

    public static byte HEADER_SIZE = 8;

    public FileWriter(int blockSize) {
        this.blockSize = blockSize * Data.SIZE;
    }

    private void writeBufferToFile(RandomAccessFile file, ByteBuffer buffer, int blockIndex) throws IOException {
        buffer.flip();
        file.seek(HEADER_SIZE + (long) blockIndex * blockSize);
        // Blokový zápis
        file.getChannel().write(buffer);
    }

    public void write(String fileName, Set<Data> dataList) {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");

            // Hlavička
            int blockNum = (int) Math.ceil((double) dataList.size() * Data.SIZE / blockSize);
            file.writeInt(blockSize);
            file.writeInt(blockNum);

            ByteBuffer buffer = ByteBuffer.allocate(blockSize);
            int recordsWritten = 0;
            int currentBlock = 0;
            for (Data data : dataList) {
                ByteBuffer dataBuffer = data.toByteBuffer();
                if (buffer.remaining() < dataBuffer.limit()) {
                    // Pokud je buffer plný, zapiš data do souboru
                    writeBufferToFile(file, buffer, currentBlock);
                    buffer.clear();
                    currentBlock++;
                }
                buffer.put(dataBuffer);
                recordsWritten++;
            }

            if (buffer.position() > 0) {
                writeBufferToFile(file, buffer, currentBlock);
            }

            file.close();
            System.out.println("Zapsáno " + Printer.formatPurple(String.valueOf(recordsWritten)) + " záznamů");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
