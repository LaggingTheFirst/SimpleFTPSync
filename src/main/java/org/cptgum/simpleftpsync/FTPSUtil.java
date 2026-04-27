package org.cptgum.simpleftpsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPSClient;

public class FTPSUtil {
    public static void uploadFile(String server, int port, String user, String pass, String remotePath, String localPath)
            throws IOException {
        FTPSClient ftpsClient = new FTPSClient();

        try {
            ftpsClient.connect(server, port);
            ftpsClient.login(user, pass);

            File localFile = new File(localPath);
            FileInputStream inputStream = new FileInputStream(localFile);
            boolean success = ftpsClient.storeFile(remotePath, inputStream);

            if (!success) {
                throw new IOException("Failed to upload file to FTPS server");
            }

            System.out.println("File uploaded successfully to FTPS server");
        } finally {
            if (ftpsClient.isConnected()) {
                ftpsClient.disconnect();
            }
        }
    }
}
