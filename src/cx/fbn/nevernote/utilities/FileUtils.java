package cx.fbn.nevernote.utilities;

import java.io.File;

/**
 * @author Nick Clarke
 *
 */
public final class FileUtils {

    private FileUtils() {}

    public static String toFileURLString(File file) {
        // NFC TODO: is it safe to use file.toURI().toURL() instead?
        String prefix = (System.getProperty("os.name").contains("Windows") ? "file:///" : "file://");
        return prefix + toForwardSlashedPath(file.getAbsolutePath());
    }

    public static String toForwardSlashedPath(String path) {
        return path.replace('\\', '/');
    }
  
}
