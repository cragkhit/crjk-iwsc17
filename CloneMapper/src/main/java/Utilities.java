import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Chaiyong on 11/19/16.
 */
public class Utilities {
    public static String HOME_DIR;
    public static String SRC_HOME_DIR;
    public static String BASED_PATH;
    public static String SEARCH_PATH;
    public static String[] EXTENSION = {"java"};
    public static String SYSTEM;

    public static int TYPE = 3;
    public static int DEFAULT = 0;
    public static int TYPE1 = 1;
    public static int TYPE2 = 2;
    public static int TYPE3 = 3;

    public static String[] RESULTS_FOLDER_NAME = {"default", "type1", "type2c", "type3-2c"};

    public static String[] FOLDER_NAME_ENDING = {"clones", "clones", "consistent-clones", "consistent-clones"};
    public static String[] CLONE_FILE_ENDING = {"clones-0.30-classes", "clones-0.00-classes", "consistent-clones-0.00-classes", "consistent-clones-0.30-classes"};

    public Utilities() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            HOME_DIR = prop.getProperty("HOME_DIR");
            SRC_HOME_DIR = prop.getProperty("SRC_HOME_DIR");
            BASED_PATH=prop.getProperty("BASED_PATH");
            SEARCH_PATH=prop.getProperty("SEARCH_PATH");

            SYSTEM=prop.getProperty("SYSTEM");
//            TYPE=Integer.parseInt(prop.getProperty("TYPE"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
