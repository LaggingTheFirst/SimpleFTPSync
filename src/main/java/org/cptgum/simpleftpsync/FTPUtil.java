package org.cptgum.simpleftpsync;

import java.io.File;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;

public class FTPUtil {
    private FTPUtil() {
    }

    public static void uploadPath(String server, int port, String user, String pass, String remotePath, String localPath)
            throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(server, port);
            if (!ftpClient.login(user, pass)) {
                throw new IOException("FTP login failed for user " + user);
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            FtpUploadSupport.uploadPath(ftpClient, new File(localPath), remotePath);
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

    public static void uploadFile(String server, int port, String user, String pass, String remotePath, String localPath)
            throws IOException {
        uploadPath(server, port, user, pass, remotePath, localPath);
    }
}
