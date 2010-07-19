package cx.fbn.nevernote.config;


/**
 * Things that can only be changed at startup
 *
 * @author Nick Clarke
 */
public class StartupConfig {

    // Init to default values
    private String name = "NeverNote";
    private String homeDirPath = System.getProperty("user.dir");
    private boolean disableViewing = false;


    public String getName() {
        return name;
    }

    public void setName(String n) {
        if (isNonEmpty(n)) {
            name = "NeverNote-" + n;
        }
    }

    public String getHomeDirPath() {
        return homeDirPath;
    }

    public void setHomeDirPath(String path) {
        if (isNonEmpty(path)) {
            homeDirPath = path;
        }
    }

    public boolean getDisableViewing() {
        return disableViewing;
    }

    public void setDisableViewing(boolean disableViewing) {
        this.disableViewing = disableViewing;
    }

    private static boolean isNonEmpty(String n) {
        return n != null && !n.trim().equals("");
    }

}
