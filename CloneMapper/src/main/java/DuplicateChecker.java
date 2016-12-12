import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chaiyong on 21/11/2016.
 */
public class DuplicateChecker {

    public static void main(String[] args) {
        for (int type=1; type<=3; type++)
            process(type);
    }

    public static void process(int type) {
        Utilities u = new Utilities();
        System.out.println("CHECKING DUPLICATES ... ");
        checkPairAndCopyDetails(
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type-1] + "/" + Utilities.SYSTEM + "_only_orig_with_checks.csv",
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type] + "/" + Utilities.SYSTEM + "_only_orig_with_checks.csv",
                0, 0, false, ",Found in type" + (type-1) + " clones", ".csv", "_type1.csv");
        if (type == 3) {
            checkPairAndCopyDetails(
                    Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type-2] + "/" + Utilities.SYSTEM + "_only_orig_with_checks.csv",
                    Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type] + "/" + Utilities.SYSTEM + "_only_orig_with_checks_type1.csv",
                    0, 0, false, ",Found in type" + (type-2) + " clones", ".csv", "_2.csv");
        }
        checkPairAndCopyDetails(
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type-1] + "/" + Utilities.SYSTEM + "_only_decomp_with_checks.csv",
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type] + "/" + Utilities.SYSTEM + "_only_decomp_with_checks.csv",
                0, 0, false, ",Found in type" + (type-1) + " cloness", ".csv", "_type1.csv");
        if (type == 3) {
            checkPairAndCopyDetails(
                    Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type-2] + "/" + Utilities.SYSTEM + "_only_decomp_with_checks.csv",
                    Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type] + "/" + Utilities.SYSTEM + "_only_decomp_with_checks_type1.csv",
                    0, 0, false, ",Found in type" + (type-2) + " clones", ".csv", "_2.csv");
        }
        checkPairAndCopyDetails(
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type-1] + "/" + Utilities.SYSTEM + "_agreed_with_checks.csv",
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type] + "/" + Utilities.SYSTEM + "_agreed_with_checks.csv",
                0, 0, false, ",Found in type" + (type-1) + " clones", ".csv", "_type1.csv");
        if (type == 3) {
            checkPairAndCopyDetails(
                    Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type-2] + "/" + Utilities.SYSTEM + "_agreed_with_checks.csv",
                    Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[type] + "/" + Utilities.SYSTEM + "_agreed_with_checks_type1.csv",
                    0, 0, false, ",Found in type" + (type-2) + " clones", ".csv", "_2.csv");
        }
        System.out.println("DONE ... ");
    }

    public static void checkPairAndCopyDetails(String baseFile, String searchFile, int offset, int offset2, boolean copyComments, String text, String replacedStr, String replacingStr) {
        HashMap<String, String> baseFileMap = new HashMap<>();
        ArrayList<Fragment> searchFileArr = new ArrayList<>();
        FileWriter fw = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String line = "";
        String cvsSplitBy = ",";
        String outFileName = searchFile.replace ( replacedStr, replacingStr );

        System.out.println(outFileName);
        File oFile = new File (outFileName);

        try {
            fw = new FileWriter ( oFile.getAbsoluteFile(), false );
            bw = new BufferedWriter ( fw );

            br = new BufferedReader(new FileReader(baseFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] clone = line.split(cvsSplitBy);

                Fragment f = new Fragment(
                        clone[offset].trim(),
                        Integer.parseInt(clone[1 + offset].trim()),
                        Integer.parseInt(clone[2 + offset].trim()),
                        clone[3 + offset].trim(),
                        Integer.parseInt(clone[4 + offset].trim()),
                        Integer.parseInt(clone[5 + offset].trim()));
                baseFileMap.put(f.toString(), line);
            }
            br.close();

            br = new BufferedReader(new FileReader(searchFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] clone = line.split(cvsSplitBy);
                Fragment f = new Fragment(
                        clone[offset2].trim(),
                        Integer.parseInt(clone[1+offset2].trim()),
                        Integer.parseInt(clone[2+offset2].trim()),
                        clone[3+offset2].trim(),
                        Integer.parseInt(clone[4+offset2].trim()),
                        Integer.parseInt(clone[5+offset2].trim()));
                f.setOther(line);
                searchFileArr.add(f);
            }
            br.close();

            // start searching
            for (Fragment f : searchFileArr) {
                if (baseFileMap.containsKey(f.toString())) {
                    if (copyComments) {
                        String fline = baseFileMap.get(f.toString());
                        // System.out.println(fline);
                        bw.write(fline + "\n");
                    } else {
                        // System.out.println(f.getOther() + text);
                        bw.write(f.getOther() + text + "\n");
                    }
                } else {
                    // System.out.println(f.getOther());
                    bw.write(f.getOther() + "\n");
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bw != null) {
                try {
                    bw.close();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
