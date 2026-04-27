package org.cptgum.simpleftpsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;

public class FTPUtil {
    public static void uploadFile(String server, int port, String user, String pass, String remotePath, String localPath)
            throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            FileInputStream inputStream = new FileInputStream(new File(localPath));
            boolean done = ftpClient.storeFile(remotePath, inputStream);
            inputStream.close();

            if (done) {
                System.out.println("File uploaded successfully.");
            } else {
                System.out.println("Failed to upload file.");
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
