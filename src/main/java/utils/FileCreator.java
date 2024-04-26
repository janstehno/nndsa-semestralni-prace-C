package utils;

import model.Data;
import print.Printer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public record FileCreator(int blockSize, int blockNum) {

    public static byte HEADER_SIZE = 8;

    public FileCreator(int blockSize, int blockNum) {
        this.blockSize = blockSize * Data.SIZE;
        this.blockNum = blockNum;
    }

    public void create(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) file.delete();

            try (FileOutputStream fos = new FileOutputStream(fileName); FileChannel fileChannel = fos.getChannel()) {

                ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
                header.putInt(blockSize);
                header.putInt(blockNum);
                header.flip();

                fileChannel.write(header);

                for (int i = 0; i < blockNum; i++) {
                    ByteBuffer blockData = ByteBuffer.allocate(blockSize);

                    for (int j = 0; j < blockSize / Data.SIZE; j++) {
                        Data data = new Data(i * (blockSize / Data.SIZE) + j, "ZÁZNAM " + (i * (blockSize / Data.SIZE) + j));
                        blockData.put(data.toByteBuffer());
                    }

                    blockData.flip();
                    fileChannel.write(blockData);
                }

                System.out.println("Soubor " + Printer.formatYellow("heap_file.bin") + " byl úspěšně vytvořen.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
