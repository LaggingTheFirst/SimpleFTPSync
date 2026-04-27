package org.cptgum.simpleftpsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class SyncStateStore {
    private final File stateFile;
    private final Properties properties = new Properties();
    private boolean dirty;

    public SyncStateStore(File dataFolder) throws IOException {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create plugin data folder: " + dataFolder.getPath());
        }

        this.stateFile = new File(dataFolder, "sync-state.properties");
        load();
    }

    public boolean isUpToDate(String key, File file, ChangeDetectionMode mode) throws IOException {
        return fingerprint(file, mode).equals(properties.getProperty(key));
    }

    public void markSynced(String key, File file, ChangeDetectionMode mode) throws IOException {
        String fingerprint = fingerprint(file, mode);
        if (!fingerprint.equals(properties.getProperty(key))) {
            properties.setProperty(key, fingerprint);
            dirty = true;
        }
    }

    public void save() throws IOException {
        if (!dirty) {
            return;
        }

        FileOutputStream outputStream = new FileOutputStream(stateFile);
        try {
            properties.store(outputStream, "SimpleFTPSync upload state");
            dirty = false;
        } finally {
            outputStream.close();
        }
    }

    private void load() throws IOException {
        if (!stateFile.exists()) {
            return;
        }

        FileInputStream inputStream = new FileInputStream(stateFile);
        try {
            properties.load(inputStream);
        } finally {
            inputStream.close();
        }
    }

    private String fingerprint(File file, ChangeDetectionMode mode) throws IOException {
        switch (mode) {
            case METADATA:
                return file.length() + ":" + file.lastModified();
            case CHECKSUM:
                return file.length() + ":" + sha256(file);
            default:
                throw new IllegalStateException("Unsupported change detection mode: " + mode);
        }
    }

    private String sha256(File file) throws IOException {
        MessageDigest digest = createDigest();
        byte[] buffer = new byte[8192];

        FileInputStream inputStream = new FileInputStream(file);
        try {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }

        byte[] hash = digest.digest();
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            hex.append(String.format("%02x", Integer.valueOf(value & 0xff)));
        }
        return hex.toString();
    }

    private MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this Java runtime.", e);
        }
    }
}
