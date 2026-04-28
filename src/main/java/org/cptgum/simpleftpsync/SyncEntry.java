package org.cptgum.simpleftpsync;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class SyncEntry {
    private final File localPath;
    private final String remotePath;
    private final List<String> includes;
    private final List<String> excludes;
    private final boolean changedOnly;
    private final List<Pattern> includePatterns;
    private final List<Pattern> excludePatterns;

    public SyncEntry(File localPath, String remotePath, List<String> includes, List<String> excludes, boolean changedOnly) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.includes = immutableCopy(includes);
        this.excludes = immutableCopy(excludes);
        this.changedOnly = changedOnly;
        this.includePatterns = compilePatterns(this.includes);
        this.excludePatterns = compilePatterns(this.excludes);
    }

    public File getLocalPath() {
        return localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public boolean isChangedOnly() {
        return changedOnly;
    }

    public boolean matches(String relativePath) {
        String normalizedPath = normalize(relativePath);
        boolean included = includePatterns.isEmpty() || matchesAny(includePatterns, normalizedPath);
        return included && !matchesAny(excludePatterns, normalizedPath);
    }

    private List<String> immutableCopy(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(new ArrayList<String>(values));
    }

    private List<Pattern> compilePatterns(List<String> patterns) {
        List<Pattern> compiled = new ArrayList<Pattern>();
        for (String pattern : patterns) {
            if (pattern != null && !pattern.trim().isEmpty()) {
                compiled.add(Pattern.compile(globToRegex(normalize(pattern.trim()))));
            }
        }
        return compiled;
    }

    private boolean matchesAny(List<Pattern> patterns, String normalizedPath) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(normalizedPath).matches()) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String path) {
        return path.replace('\\', '/');
    }

    private String globToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < glob.length(); i++) {
            char current = glob.charAt(i);
            if (current == '*') {
                boolean doubleStar = i + 1 < glob.length() && glob.charAt(i + 1) == '*';
                if (doubleStar) {
                    boolean followedBySlash = i + 2 < glob.length() && glob.charAt(i + 2) == '/';
                    if (followedBySlash) {
                        regex.append("(?:.*/)?");
                        i += 2;
                    } else {
                        regex.append(".*");
                        i++;
                    }
                } else {
                    regex.append("[^/]*");
                }
            } else if (current == '?') {
                regex.append("[^/]");
            } else if ("\\.[]{}()+-^$|".indexOf(current) >= 0) {
                regex.append('\\').append(current);
            } else {
                regex.append(current);
            }
        }

        return regex.append('$').toString();
    }
}
