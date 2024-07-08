package manager.impl;

import java.io.Serializable;

public class ChunkInfo implements Serializable {
    private String dataServerName;
    private int chunkNumber;

    public ChunkInfo(String dataServerName, int chunkNumber) {
        this.dataServerName = dataServerName;
        this.chunkNumber = chunkNumber;
    }

    public String getDataServerName() {
        return dataServerName;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }
}