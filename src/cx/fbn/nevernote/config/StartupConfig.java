package cx.fbn.nevernote.config;

import java.io.File;



/**
 * Things that can only be changed at startup
 *
 * @author Nick Clarke
 */
public class StartupConfig {

    // Init to default values
    private String name = "NeverNote";
    private String homeDirPath;
    private String programDirPath;
    private boolean disableViewing = false;


    public String getName() {
        return name;
    }

    public void setName(String n) {
        if (isNonEmpty(n)) {
            name = "NeverNote-" + n;
        }
    }

    public String getProgramDirPath() {
    	if (programDirPath == null) {
    	   programDirPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    	   if (programDirPath.endsWith(".jar")) {
    		   programDirPath = programDirPath.substring(0,programDirPath.lastIndexOf("/"));
    	   } else {
    		   if (programDirPath.endsWith("/")) {
    			   programDirPath = programDirPath.substring(0,programDirPath.length()-1);
    		   }
   			   programDirPath = programDirPath.substring(0,programDirPath.lastIndexOf("/"));
    	   }
    	}
    	return programDirPath;
    }
    
    public String getHomeDirPath() {
    	if (homeDirPath == null) {
    		homeDirPath = System.getProperty("user.home") + File.separator 
    				+ "." +name.toLowerCase() + File.separator;
    	}
        return homeDirPath;
    }

    public void setHomeDirPath(String path) {
        if (isNonEmpty(path)) {
            homeDirPath = path;
        }
    }
    
    public void setProgramDirPath(String path) {
        if (isNonEmpty(path)) {
            programDirPath = path;
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
