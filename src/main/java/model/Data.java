package model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Data {
    public static final int SIZE = 64;

    int id;
    byte[] data;

    public Data(int id, String data) {
        this.id = id;
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        this.data = new byte[SIZE - 4];
        System.arraycopy(dataBytes, 0, this.data, 0, Math.min(dataBytes.length, this.data.length));
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.putInt(id);
        buffer.put(data);
        buffer.position(0); // Reset position to beginning for reading
        return buffer;
    }
}
