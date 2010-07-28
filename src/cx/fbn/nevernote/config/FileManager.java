package cx.fbn.nevernote.config;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Provides access to NeverNote standard runtime directories.
 *
 * @author Nick Clarke
 */
public class FileManager {

    private static final Pattern ALL_PATH_SEPARATORS_REGEX = Pattern.compile("[/\\\\]");

    private final String homeDirPath;
    private final File homeDir;

    private final String dbDirPath;
    private final File dbDir;

    private final File logsDir;

    private final String imagesDirPath;
    private final File imagesDir;

    private final String qssDirPath;
    private final File qssDir;

    private final String resDirPath;
    private final File resDir;

    private final File xmlDir;

    /**
     * Check or create the db, log and res directories.
     *
     * @param homeDirPath the installation dir containing db/log/res directories, must exist
     * @throws InitializationException for missing directories or file permissions problems
     */
    public FileManager(String homeDirPath) throws InitializationException {
        if (homeDirPath == null) {
            throw new IllegalArgumentException("homeDirPath must not be null");
        }

        this.homeDir = new File(toPlatformPathSeparator(homeDirPath));
        checkExistingWriteableDir(homeDir);
        this.homeDirPath = slashTerminatePath(homeDir.getPath());

        // Read-only
        imagesDir = new File(homeDir, "images");
        checkExistingReadableDir(imagesDir);
        imagesDirPath = slashTerminatePath(imagesDir.getPath());

        qssDir = new File(homeDir, "qss");
        checkExistingReadableDir(qssDir);
        qssDirPath = slashTerminatePath(qssDir.getPath());

        xmlDir = new File(homeDir, "xml");
        checkExistingReadableDir(xmlDir);

        // Read-write
        dbDir = new File(homeDir, "db");
        createDirOrCheckWriteable(dbDir);
        dbDirPath = slashTerminatePath(dbDir.getPath());

        logsDir = new File(homeDir, "logs");
        createDirOrCheckWriteable(logsDir);

        resDir = new File(homeDir, "res");
        createDirOrCheckWriteable(resDir);
        resDirPath = slashTerminatePath(resDir.getPath());
    }

    /**
     * Get a file below the base installation directory.
     */
    public File getHomeDirFile(String relativePath) {
        return new File(homeDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the base installation directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getHomeDirPath(String relativePath) {
        return homeDirPath + toPlatformPathSeparator(relativePath);
    }

    /**
     * Get a file below the 'db' directory.
     */
    public File getDbDirFile(String relativePath) {
        return new File(dbDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the 'db' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getDbDirPath(String relativePath) {
        return dbDirPath + toPlatformPathSeparator(relativePath);
    }

    /**
     * Get a file below the 'images' directory.
     */
    public File getImageDirFile(String relativePath) {
        return new File(imagesDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the 'images' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getImageDirPath(String relativePath) {
        return imagesDirPath + toPlatformPathSeparator(relativePath);
    }

    /**
     * Get a file below the 'logs' directory.
     */
    public File getLogsDirFile(String relativePath) {
        return new File(logsDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the 'qss' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getQssDirPath(String relativePath) {
        return qssDirPath + toPlatformPathSeparator(relativePath);
    }

    /**
     * Get a path to the 'res' directory, terminated with native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getResDirPath() {
        return resDirPath;
    }

    /**
     * Get a path below the 'res' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getResDirPath(String relativePath) {
        return resDirPath + toPlatformPathSeparator(relativePath);
    }

    /**
     * Get a file below the 'xml' directory.
     */
    public File getXMLDirFile(String relativePath) {
        return new File(xmlDir, toPlatformPathSeparator(relativePath));
    }

    private static String toPlatformPathSeparator(String relativePath) {
		return ALL_PATH_SEPARATORS_REGEX.matcher(relativePath).replaceAll(
				// Must double-escape backslashes,
				// because they have special meaning in the replacement string of Matcher.replaceAll
				(File.separator.equals("\\") ? "\\\\" : File.separator));
    }

    private static String slashTerminatePath(String path) {
        if (!path.substring(path.length() - 1).equals(File.separator)) {
            return path + File.separator;
        }
        return path;
    }

    /**
     * Delete first-level files (but not directories) from the directory.
     *
     * @throws InitializationException for file deletion failures
     */
    private static void deleteTopLevelFiles(File dir) throws InitializationException {
        File[] toDelete = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        for (File f : toDelete) {
            if (!f.delete()) {
                throw new InitializationException("Failed to delete file: '" + f + "'");
            }
        }
    }

    /**
     * @throws InitializationException for bad file permissions, or a file instead of a directory
     */
    private static void createDirOrCheckWriteable(File dir) throws InitializationException {
        if (dir.isDirectory()) {
            // Dir exists, check permissions
            if (!dir.canRead()) {
                throw new InitializationException("Directory '" + dir + "' does not have read permission");
            }
            if (!dir.canWrite()) {
                throw new InitializationException("Directory '" + dir + "' does not have write permission");
            }
        } else if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new InitializationException("Failed to create directory '" + dir + "'");
            }
        } else {
            throw new InitializationException("Expected directory '" + dir + "' but found a file instead");
        }
    }

    /**
     * @throws InitializationException if non-existent, bad file permissions, or a file instead of a directory
     */
    private static void checkExistingReadableDir(File dir) throws InitializationException {
        if (dir.isDirectory()) {
            // Dir exists, check permissions
            if (!dir.canRead()) {
                throw new InitializationException("Directory '" + dir + "' does not have read permission");
            }
        } else if (!dir.exists()) {
            throw new InitializationException("Directory '" + dir + "' does not exist");
        } else {
            throw new InitializationException("Expected directory '" + dir + "' but found a file instead");
        }
    }

    /**
     * @throws InitializationException if non-existent, bad file permissions, or a file instead of a directory
     */
    private static void checkExistingWriteableDir(File dir) throws InitializationException {
        checkExistingReadableDir(dir);
        if (!dir.canWrite()) {
            throw new InitializationException("Directory '" + dir + "' does not have write permission");
        }
    }

    /**
     * Called at startup to purge files from 'res' directory.
     */
    public void purgeResDirectory() throws InitializationException {
        deleteTopLevelFiles(resDir);
    }
}
