package org.cptgum.simpleftpsync;

import java.io.File;

public class SyncFile {
    private final File localFile;
    private final String remoteFilePath;
    private final String stateKey;

    public SyncFile(File localFile, String remoteFilePath, String stateKey) {
        this.localFile = localFile;
        this.remoteFilePath = remoteFilePath;
        this.stateKey = stateKey;
    }

    public File getLocalFile() {
        return localFile;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public String getStateKey() {
        return stateKey;
    }
}
