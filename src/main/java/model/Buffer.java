package model;

import java.nio.ByteBuffer;

public class Buffer {
    private final ByteBuffer buffer;
    private boolean accessingFile;
    private boolean loadingData;

    public Buffer(ByteBuffer buffer) {
        this.buffer = buffer;
        this.accessingFile = false;
        this.loadingData = false;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean isNotAccessingFile() {return !accessingFile;}

    public void accessingFile() {
        this.accessingFile = true;
    }

    public void notAccessingFile() {
        this.accessingFile = false;
    }

    public boolean isNotLoadingData() {return !loadingData;}

    public void loadingData() {
        this.loadingData = true;
    }

    public void notLoadingData() {
        this.loadingData = false;
    }
}
