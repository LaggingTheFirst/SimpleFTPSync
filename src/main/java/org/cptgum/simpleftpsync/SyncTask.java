package org.cptgum.simpleftpsync;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class SyncTask extends BukkitRunnable {
    private final SimpleFTPSync plugin;

    public SyncTask(SimpleFTPSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.beginSyncRun()) {
            plugin.getLogger().warning("Skipping sync because a previous sync run is still in progress.");
            return;
        }

        try {
            SyncType syncType = SyncType.fromConfig(plugin.getPluginConfig().getString("sync-type"));
            if (syncType == null) {
                plugin.getLogger().warning("Unrecognized sync type in config.yml. Expected FTP, FTPS, or SFTP.");
                return;
            }

            ChangeDetectionMode changeDetectionMode =
                    ChangeDetectionMode.fromConfig(plugin.getPluginConfig().getString("change-detection"));

            List<SyncEntry> syncEntries = loadSyncEntries(syncType);
            if (syncEntries.isEmpty()) {
                plugin.getLogger().warning("No valid sync entries found in config.yml.");
                return;
            }

            SyncStateStore stateStore = new SyncStateStore(plugin.getDataFolder());
            int uploadedCount = 0;
            int skippedCount = 0;
            int failedCount = 0;

            for (SyncEntry syncEntry : syncEntries) {
                List<SyncFile> syncFiles = collectSyncFiles(syncEntry);
                if (syncFiles.isEmpty()) {
                    plugin.getLogger().info("No files matched for " + syncEntry.getLocalPath() + ".");
                    continue;
                }

                for (SyncFile syncFile : syncFiles) {
                    try {
                        if (syncEntry.isChangedOnly()
                                && stateStore.isUpToDate(syncFile.getStateKey(), syncFile.getLocalFile(), changeDetectionMode)) {
                            skippedCount++;
                            continue;
                        }

                        upload(syncType, syncFile);
                        stateStore.markSynced(syncFile.getStateKey(), syncFile.getLocalFile(), changeDetectionMode);
                        uploadedCount++;
                    } catch (IOException | SftpException | JSchException e) {
                        failedCount++;
                        plugin.getLogger().warning(
                                "Failed to synchronize " + syncFile.getLocalFile().getPath() + " -> "
                                        + syncFile.getRemoteFilePath() + ": " + e.getMessage());
                    }
                }
            }

            stateStore.save();
            plugin.getLogger().info(
                    "Sync complete. Uploaded: " + uploadedCount + ", skipped unchanged: " + skippedCount
                            + ", failed: " + failedCount + ".");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to initialize sync state: " + e.getMessage());
        } finally {
            plugin.endSyncRun();
        }
    }

    private void upload(SyncType syncType, SyncFile syncFile) throws IOException, SftpException, JSchException {
        switch (syncType) {
            case FTP:
                FTPUtil.uploadPath(
                        plugin.getPluginConfig().getString("ftp.server"),
                        plugin.getPluginConfig().getInt("ftp.port"),
                        plugin.getPluginConfig().getString("ftp.username"),
                        plugin.getPluginConfig().getString("ftp.password"),
                        syncFile.getRemoteFilePath(),
                        syncFile.getLocalFile().getPath());
                break;
            case SFTP:
                SFTPUtil.uploadPath(
                        plugin.getPluginConfig().getString("sftp.server"),
                        plugin.getPluginConfig().getInt("sftp.port"),
                        plugin.getPluginConfig().getString("sftp.username"),
                        plugin.getPluginConfig().getString("sftp.password"),
                        syncFile.getRemoteFilePath(),
                        syncFile.getLocalFile().getPath(),
                        plugin.getPluginConfig().getString("sftp.known-hosts-file"),
                        plugin.getPluginConfig().getBoolean("sftp.strict-host-key-checking", true));
                break;
            case FTPS:
                FTPSUtil.uploadPath(
                        plugin.getPluginConfig().getString("ftps.server"),
                        plugin.getPluginConfig().getInt("ftps.port"),
                        plugin.getPluginConfig().getString("ftps.username"),
                        plugin.getPluginConfig().getString("ftps.password"),
                        syncFile.getRemoteFilePath(),
                        syncFile.getLocalFile().getPath());
                break;
            default:
                throw new IllegalStateException("Unsupported sync type: " + syncType);
        }
    }

    private List<SyncEntry> loadSyncEntries(SyncType syncType) {
        List<SyncEntry> syncEntries = new ArrayList<SyncEntry>();
        FileConfiguration config = plugin.getPluginConfig();

        for (Map<?, ?> entry : config.getMapList("sync-folders")) {
            String localPath = asString(entry.get("local-path"));
            String remotePath = asString(entry.get("remote-path"));
            boolean changedOnly = getChangedOnly(entry.get("changed-only"), config.getBoolean("sync-only-changed", true));
            addSyncEntry(syncEntries, localPath, remotePath, getStringList(entry.get("include")), getStringList(entry.get("exclude")), changedOnly);
        }

        ConfigurationSection syncFolderSection = config.getConfigurationSection("sync-folders");
        if (syncFolderSection != null) {
            for (String key : syncFolderSection.getKeys(false)) {
                String localPath = syncFolderSection.getString(key + ".local-path");
                String remotePath = syncFolderSection.getString(key + ".remote-path");
                boolean changedOnly = syncFolderSection.getBoolean(key + ".changed-only", config.getBoolean("sync-only-changed", true));
                addSyncEntry(
                        syncEntries,
                        localPath,
                        remotePath,
                        syncFolderSection.getStringList(key + ".include"),
                        syncFolderSection.getStringList(key + ".exclude"),
                        changedOnly);
            }
        }

        if (syncEntries.isEmpty()) {
            String localPath = config.getString("local-path");
            String remotePath = config.getString(syncType.getConfigKey() + ".remote-path");
            addSyncEntry(syncEntries, localPath, remotePath, Collections.<String>emptyList(), Collections.<String>emptyList(),
                    config.getBoolean("sync-only-changed", true));
        }

        return syncEntries;
    }

    private void addSyncEntry(
            List<SyncEntry> syncEntries,
            String localPathValue,
            String remotePathValue,
            List<String> includes,
            List<String> excludes,
            boolean changedOnly) {
        if (isBlank(localPathValue) || isBlank(remotePathValue)) {
            return;
        }

        File localPath = new File(localPathValue);
        if (!localPath.exists()) {
            plugin.getLogger().warning("Skipping missing local path: " + localPath.getPath());
            return;
        }

        syncEntries.add(new SyncEntry(localPath, normalizeRemotePath(remotePathValue), includes, excludes, changedOnly));
    }

    private List<SyncFile> collectSyncFiles(SyncEntry syncEntry) {
        List<SyncFile> syncFiles = new ArrayList<SyncFile>();
        File localPath = syncEntry.getLocalPath();

        if (localPath.isFile()) {
            if (syncEntry.matches(localPath.getName())) {
                syncFiles.add(new SyncFile(localPath, syncEntry.getRemotePath(), createStateKey(localPath, syncEntry.getRemotePath())));
            }
            return syncFiles;
        }

        collectDirectoryFiles(syncEntry, localPath, localPath, syncFiles);
        return syncFiles;
    }

    private void collectDirectoryFiles(SyncEntry syncEntry, File rootDirectory, File currentFile, List<SyncFile> syncFiles) {
        if (currentFile.isDirectory()) {
            File[] children = currentFile.listFiles();
            if (children == null) {
                plugin.getLogger().warning("Could not read local directory: " + currentFile.getPath());
                return;
            }

            for (File child : children) {
                collectDirectoryFiles(syncEntry, rootDirectory, child, syncFiles);
            }
            return;
        }

        String relativePath = rootDirectory.toURI().relativize(currentFile.toURI()).getPath();
        if (!syncEntry.matches(relativePath)) {
            return;
        }

        String remoteFilePath = appendRemotePath(syncEntry.getRemotePath(), relativePath);
        syncFiles.add(new SyncFile(currentFile, remoteFilePath, createStateKey(currentFile, remoteFilePath)));
    }

    private String createStateKey(File localFile, String remoteFilePath) {
        return localFile.getAbsolutePath() + "->" + remoteFilePath;
    }

    private String appendRemotePath(String remoteRoot, String relativePath) {
        String normalizedRoot = normalizeRemotePath(remoteRoot);
        String normalizedRelativePath = relativePath.replace('\\', '/');
        if (normalizedRoot.endsWith("/")) {
            return normalizedRoot + normalizedRelativePath;
        }

        return normalizedRoot + "/" + normalizedRelativePath;
    }

    private String normalizeRemotePath(String remotePath) {
        return remotePath.replace('\\', '/');
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private boolean getChangedOnly(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        return Boolean.parseBoolean(value.toString());
    }

    private List<String> getStringList(Object value) {
        if (!(value instanceof List<?>)) {
            return Collections.emptyList();
        }

        List<String> strings = new ArrayList<String>();
        for (Object item : (List<?>) value) {
            if (item != null) {
                strings.add(item.toString());
            }
        }
        return strings;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
