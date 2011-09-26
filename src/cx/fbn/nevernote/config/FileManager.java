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

    private final String programDirPath;
    private final File programDir;
    
    private final String homeDirPath;
    private final File homeDir;

    private final String dbDirPath;
    private final File dbDir;

    private final File logsDir;

    private final String imagesDirPath;
    private final File imagesDir;

    private final String spellDirPath;
    private final File spellDir;
    
    private final String spellDirPathUser;
    private final File spellDirUser;
    
    private final String qssDirPath;
    private final File qssDir;
    
    private final String qssDirPathUser;
    private final File qssDirUser;

    private final String resDirPath;
    private final File resDir;

    private final File xmlDir;

    private final String translateDirPath;
    private final File translateDir;

    /**
     * Check or create the db, log and res directories.
     *
     * @param homeDirPath the installation dir containing db/log/res directories, must exist
     * @throws InitializationException for missing directories or file permissions problems
     */
    public FileManager(String homeDirPath, String programDirPath) throws InitializationException {
        if (homeDirPath == null) {
            throw new IllegalArgumentException("homeDirPath must not be null");
        }
        if (programDirPath == null) {
            throw new IllegalArgumentException("programDirPath must not be null");
        }

        this.homeDir = new File(toPlatformPathSeparator(homeDirPath));
        this.programDir = new File(toPlatformPathSeparator(programDirPath));
        createDirOrCheckWriteable(homeDir);
        this.homeDirPath = slashTerminatePath(homeDir.getPath());
        this.programDirPath = slashTerminatePath(programDir.getPath());
        
        // Read-only
        imagesDir = new File(programDir, "images");
        checkExistingReadableDir(imagesDir);
        imagesDirPath = slashTerminatePath(imagesDir.getPath());

        qssDir = new File(programDir, "qss");
        checkExistingReadableDir(qssDir);
        qssDirPath = slashTerminatePath(qssDir.getPath());
        
        qssDirUser = new File(homeDir, "qss");
        createDirOrCheckWriteable(qssDirUser);
        qssDirPathUser = slashTerminatePath(qssDirUser.getPath());

        spellDir = new File(programDir, "spell");
        checkExistingReadableDir(spellDir);
        spellDirPath = slashTerminatePath(spellDir.getPath());
        

        spellDirUser = new File(homeDir, "spell");
        createDirOrCheckWriteable(spellDirUser);
        spellDirPathUser = slashTerminatePath(spellDirUser.getPath());
        
        xmlDir = new File(programDir, "xml");
        checkExistingReadableDir(xmlDir);

        translateDir = new File(programDir, "translations");
        checkExistingReadableDir(translateDir);
        translateDirPath= slashTerminatePath(translateDir.getPath());

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
     * Get a file below the base user home directory.
     */
    public File getProgramDirFile(String relativePath) {
        return new File(programDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the base user home directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getProgramDirPath(String relativePath) {
        return programDirPath + toPlatformPathSeparator(relativePath);
    }
    
    /**
     * Get a file below the base user home directory.
     */
    public File getHomeDirFile(String relativePath) {
        return new File(homeDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the base user home directory, using native {@link File#separator}.
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
     * Get a path below the 'spell' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getSpellDirPath(String relativePath) {
        return dbDirPath + toPlatformPathSeparator(relativePath);
    }

    /**
     * Get a file below the 'spell' directory.
     */
    public File getSpellDirFile(String relativePath) {
        return new File(spellDir, toPlatformPathSeparator(relativePath));
    }
    
    /** 
     * Get the spell directory for the jazzy word list
     */
    public String getSpellDirPath() {
    	return spellDirPath;
    }

    /**
     * Get a file below the 'spell' directory for user dictionaries.
     */
    public File getSpellDirFileUser(String relativePath) {
        return new File(spellDirUser, toPlatformPathSeparator(relativePath));
    }
    
    /** 
     * Get the spell directory for the jazzy word list (user dictionary).
     */
    public String getSpellDirPathUser() {
    	return spellDirPathUser;
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
     * Get a path below the 'qss' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getQssDirPathUser(String relativePath) {
        return qssDirPathUser + toPlatformPathSeparator(relativePath);
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
     * Get a path below the 'res' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.  This is different from the 
     * one above in that it will encode the relative path
     */
    public String getResDirPathSpecialChar(String relativePath) {
   		return resDirPath + toPlatformPathSeparator(relativePath).replace("#", "%23");
    }

    /**
     * Get a file below the 'xml' directory.
     */
    public File getXMLDirFile(String relativePath) {
        return new File(xmlDir, toPlatformPathSeparator(relativePath));
    }

    /**
     * Get a path below the 'translate' directory, using native {@link File#separator}.
     * This will contain backslashes on Windows.
     */
    public String getTranslateFilePath(String relativePath) {
        return translateDirPath + toPlatformPathSeparator(relativePath);
    }

    private static String toPlatformPathSeparator(String relativePath) {
    	// Sometimes a space in the file name comes across as a %20.  This is to put it back as a space.
    	relativePath = relativePath.replace("%20", " ");
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
    private static void deleteTopLevelFiles(File dir, boolean exitOnFail) throws InitializationException {
        File[] toDelete = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        for (File f : toDelete) {
            if (!f.delete() && exitOnFail) {
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
    @SuppressWarnings("unused")
	private static void checkExistingWriteableDir(File dir) throws InitializationException {
        checkExistingReadableDir(dir);
        if (!dir.canWrite()) {
            throw new InitializationException("Directory '" + dir + "' does not have write permission");
        }
    }

    /**
     * Called at startup to purge files from 'res' directory.
     */
    public void purgeResDirectory(boolean exitOnFail) throws InitializationException {
        deleteTopLevelFiles(resDir, exitOnFail);
    }
}
