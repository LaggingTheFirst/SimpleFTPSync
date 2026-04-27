package org.cptgum.simpleftpsync;

import java.io.File;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPClient;

public class FTPSUtil {
    private FTPSUtil() {
    }

    public static void uploadPath(String server, int port, String user, String pass, String remotePath, String localPath)
            throws IOException {
        FTPSClient ftpsClient = new FTPSClient();

        try {
            ftpsClient.connect(server, port);
            if (!ftpsClient.login(user, pass)) {
                throw new IOException("FTPS login failed for user " + user);
            }

            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");
            ftpsClient.enterLocalPassiveMode();
            ftpsClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            FtpUploadSupport.uploadPath(ftpsClient, new File(localPath), remotePath);
        } finally {
            if (ftpsClient.isConnected()) {
                ftpsClient.logout();
                ftpsClient.disconnect();
            }
        }
    }

    public static void uploadFile(String server, int port, String user, String pass, String remotePath, String localPath)
            throws IOException {
        uploadPath(server, port, user, pass, remotePath, localPath);
    }
}
