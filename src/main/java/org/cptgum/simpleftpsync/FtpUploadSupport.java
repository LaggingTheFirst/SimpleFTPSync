package org.cptgum.simpleftpsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;

final class FtpUploadSupport {
    private FtpUploadSupport() {
    }

    static void uploadPath(FTPClient client, File localPath, String remotePath) throws IOException {
        if (!localPath.exists()) {
            throw new IOException("Local path does not exist: " + localPath.getPath());
        }

        String normalizedRemotePath = normalizeRemotePath(remotePath);
        if (localPath.isDirectory()) {
            ensureDirectoryExists(client, normalizedRemotePath);

            File[] children = localPath.listFiles();
            if (children == null) {
                throw new IOException("Could not read local directory: " + localPath.getPath());
            }

            for (File child : children) {
                uploadPath(client, child, appendRemotePath(normalizedRemotePath, child.getName()));
            }
            return;
        }

        ensureParentDirectoryExists(client, normalizedRemotePath);
        try (FileInputStream inputStream = new FileInputStream(localPath)) {
            if (!client.storeFile(normalizedRemotePath, inputStream)) {
                throw new IOException(
                        "Failed to upload " + localPath.getPath() + " to " + normalizedRemotePath
                                + ": " + client.getReplyString());
            }
        }
    }

    private static void ensureParentDirectoryExists(FTPClient client, String remoteFilePath) throws IOException {
        int lastSlashIndex = remoteFilePath.lastIndexOf('/');
        if (lastSlashIndex <= 0) {
            return;
        }

        ensureDirectoryExists(client, remoteFilePath.substring(0, lastSlashIndex));
    }

    private static void ensureDirectoryExists(FTPClient client, String remoteDirectory) throws IOException {
        if (remoteDirectory == null || remoteDirectory.isEmpty() || "/".equals(remoteDirectory)) {
            return;
        }

        String[] parts = remoteDirectory.split("/");
        String currentPath = remoteDirectory.startsWith("/") ? "/" : "";

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            currentPath = currentPath.isEmpty() || "/".equals(currentPath)
                    ? currentPath + part
                    : currentPath + "/" + part;

            if (!client.changeWorkingDirectory(currentPath)
                    && !client.makeDirectory(currentPath)
                    && !client.changeWorkingDirectory(currentPath)) {
                throw new IOException("Failed to create remote directory " + currentPath + ": " + client.getReplyString());
            }
        }
    }

    private static String normalizeRemotePath(String remotePath) {
        String normalized = remotePath == null ? "" : remotePath.replace('\\', '/').trim();
        if (normalized.isEmpty()) {
            return "/";
        }

        return normalized;
    }

    private static String appendRemotePath(String parent, String child) {
        if (parent == null || parent.isEmpty() || "/".equals(parent)) {
            return "/" + child;
        }

        if (parent.endsWith("/")) {
            return parent + child;
        }

        return parent + "/" + child;
    }
}
