package org.cptgum.simpleftpsync;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.IOException;

public class SFTPUtil {
    private SFTPUtil() {
    }

    public static void uploadPath(String host, int port, String user, String pass, String remotePath, String localPath)
            throws IOException, SftpException, JSchException {
        uploadPath(host, port, user, pass, remotePath, localPath, null, false);
    }

    public static void uploadPath(
            String host,
            int port,
            String user,
            String pass,
            String remotePath,
            String localPath,
            String knownHostsPath,
            boolean strictHostKeyChecking) throws IOException, SftpException, JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        ChannelSftp channelSftp = null;

        try {
            session.setPassword(pass);
            configureHostKeyChecking(jsch, session, knownHostsPath, strictHostKeyChecking);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            uploadPath(channelSftp, new File(localPath), normalizeRemotePath(remotePath));
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static void uploadFile(String host, int port, String user, String pass, String remotePath, String localPath)
            throws IOException, SftpException, JSchException {
        uploadPath(host, port, user, pass, remotePath, localPath);
    }

    private static void configureHostKeyChecking(
            JSch jsch,
            Session session,
            String knownHostsPath,
            boolean strictHostKeyChecking) throws IOException, JSchException {
        if (!strictHostKeyChecking) {
            session.setConfig("StrictHostKeyChecking", "no");
            return;
        }

        if (knownHostsPath == null || knownHostsPath.trim().isEmpty()) {
            throw new IOException("SFTP strict host key checking is enabled, but no known-hosts file is configured.");
        }

        File knownHostsFile = new File(knownHostsPath);
        if (!knownHostsFile.exists()) {
            throw new IOException("SFTP known-hosts file does not exist: " + knownHostsFile.getPath());
        }

        jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
        session.setConfig("StrictHostKeyChecking", "yes");
    }

    private static void uploadPath(ChannelSftp channelSftp, File localPath, String remotePath)
            throws IOException, SftpException {
        if (!localPath.exists()) {
            throw new IOException("Local path does not exist: " + localPath.getPath());
        }

        if (localPath.isDirectory()) {
            ensureDirectoryExists(channelSftp, remotePath);

            File[] children = localPath.listFiles();
            if (children == null) {
                throw new IOException("Could not read local directory: " + localPath.getPath());
            }

            for (File child : children) {
                uploadPath(channelSftp, child, appendRemotePath(remotePath, child.getName()));
            }
            return;
        }

        ensureParentDirectoryExists(channelSftp, remotePath);
        channelSftp.put(localPath.getAbsolutePath(), remotePath);
    }

    private static void ensureParentDirectoryExists(ChannelSftp channelSftp, String remoteFilePath) throws SftpException {
        int lastSlashIndex = remoteFilePath.lastIndexOf('/');
        if (lastSlashIndex <= 0) {
            return;
        }

        ensureDirectoryExists(channelSftp, remoteFilePath.substring(0, lastSlashIndex));
    }

    private static void ensureDirectoryExists(ChannelSftp channelSftp, String remoteDirectory) throws SftpException {
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

            try {
                channelSftp.stat(currentPath);
            } catch (SftpException e) {
                channelSftp.mkdir(currentPath);
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
