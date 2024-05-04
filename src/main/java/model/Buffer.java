package model;

import java.nio.ByteBuffer;

public class Buffer {
    private final ByteBuffer buffer;
    private boolean reading;

    public Buffer(ByteBuffer buffer) {
        this.buffer = buffer;
        this.reading = false;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean isNotReading() {return !reading;}

    public void reading() {
        this.reading = true;
    }

    public void notReading() {
        this.reading = false;
    }
}
