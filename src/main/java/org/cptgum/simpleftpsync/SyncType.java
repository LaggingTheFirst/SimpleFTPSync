package org.cptgum.simpleftpsync;

public enum SyncType {
    FTP("ftp"),
    SFTP("sftp"),
    FTPS("ftps");

    private final String configKey;

    SyncType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static SyncType fromConfig(String value) {
        if (value == null) {
            return null;
        }

        for (SyncType syncType : values()) {
            if (syncType.name().equalsIgnoreCase(value)) {
                return syncType;
            }
        }

        return null;
    }
}
