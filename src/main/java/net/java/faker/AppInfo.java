package net.java.faker;

import java.io.File;

/**
 * Application level metadata and utility helpers.
 */
public final class AppInfo {
    public static final String NAME = "Phantom";
    public static final String LEGACY_NAME = "Faker";
    public static final String RESOURCE_ROOT = "/assets/phantom";
    public static final String LEGACY_RESOURCE_ROOT = "/assets/faker";
    public static final String CONFIG_FILE = "phantom_config.json";
    public static final String LEGACY_CONFIG_FILE = "faker_config.json";
    public static final String ACCOUNT_FILE = "phantom_accounts.json";
    public static final String LEGACY_ACCOUNT_FILE = "faker_accounts.json";
    public static final String STDOUT_LOG = "phantom_stdout.log";
    public static final String STDERR_LOG = "phantom_stderr.log";
    public static final String LEGACY_STDOUT_LOG = "std.log";
    public static final String LEGACY_STDERR_LOG = "err.log";

    private AppInfo() {
    }

    public static File resolveDataFile(File directory, String primary, String legacy) {
        File modern = new File(directory, primary);
        if (modern.exists()) {
            return modern;
        }
        File legacyFile = new File(directory, legacy);
        if (legacyFile.exists()) {
            return legacyFile;
        }
        return modern;
    }
}
