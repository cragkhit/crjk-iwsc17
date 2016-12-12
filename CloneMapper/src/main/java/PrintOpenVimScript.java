import javax.rmi.CORBA.Util;
import javax.swing.*;
import java.io.*;
public class PrintOpenVimScript {
    public static void main ( String[] args ) {
        String SYS1_DIR = Utilities.HOME_DIR + "/" + Utilities.BASED_PATH;
        String SYS2_DIR = Utilities.HOME_DIR + "/" + Utilities.BASED_PATH;

        System.out.println ( "PRINTING VIM SCRIPT ..." );
        printVimScript (Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + Utilities.SYSTEM + "_only_orig_with_checks.csv"
                         , SYS1_DIR, SYS2_DIR, 6, 0 );
//        printVimScript (Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + Utilities.SYSTEM + "_only_decomp_with_checks_type1_2.csv"
//                , SYS1_DIR, SYS2_DIR, 433, 0 );
        System.out.println ( "PRINTED" );
    }
    public static void printVimScript ( String file1, String systems1, String systems2,
                                        int startingLine, int startingIndex ) {
        String okFile = file1;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            // for the original code
            File oFile = new File ( file1.replace ( ".csv", ".sh" ) );
            FileWriter fw = new FileWriter ( oFile.getAbsoluteFile(), false );
            BufferedWriter bw = new BufferedWriter ( fw );
            // for the decompiled code
            File oFile2 = new File ( file1.replace ( ".csv", "_decomp.sh" ) );
            FileWriter fw2 = new FileWriter ( oFile2.getAbsoluteFile(), false );
            BufferedWriter bw2 = new BufferedWriter ( fw2 );

            FileReader fr = new FileReader ( okFile );
            br = new BufferedReader ( fr );
            int count = 1;
            while ( ( line = br.readLine() ) != null ) {
                if ( count >= startingLine ) {
                    // for the original code
                    String[] clone = line.split ( cvsSplitBy );
                    bw.write ( "vim -c \":e " + systems1 + "/" + clone[startingIndex + 3]
                               + "|:" + clone[startingIndex + 4]
                               + "|:vsplit " + systems1 + "/" + clone[startingIndex]
                               + "|:" + clone[startingIndex + 1] + "\"\n" );

                    // for the decompiled code
                    bw2.write ( "vim -c \":e " + systems1.replace(Utilities.BASED_PATH, Utilities.SEARCH_PATH) + "/" + clone[startingIndex + 3]
                            + "|:1"
                            + "|:vsplit " + systems1.replace(Utilities.BASED_PATH, Utilities.SEARCH_PATH) + "/" + clone[startingIndex]
                            + "|:1\"\n" );
                }
                count++;
            }
            br.close();
            fr.close();
            bw.close();
            fw.close();
            bw2.close();
            fw2.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
