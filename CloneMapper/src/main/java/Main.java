import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Main {
    /* TODO there is some code that has more than one annotaion e.g. @Test/@Override */
    private static HashMap<String, ArrayList<Method>> methodMap;
    private static BufferedWriter bw;

    private static String origCloneFile = "decomp-"
            + Utilities.SYSTEM + "_functions-" + Utilities.FOLDER_NAME_ENDING[Utilities.TYPE] + "/"
            + Utilities.SYSTEM + "_functions-" + Utilities.CLONE_FILE_ENDING[Utilities.TYPE] + ".xml";
    private static String processedCloneFile = Utilities.SYSTEM + "_clone_pairs_mapped.xml";
    private static String clonePairsDecompFile = Utilities.SYSTEM + "_clone_pairs_decomp.csv";
    private static String clonePairsFile = Utilities.SYSTEM + "_clone_pairs_orig.csv";
    private static Method matchedMethod = new Method();
    // minimum line to consider as clone
    private static int SIZE_THRESHOLD = 10;

    public static void main(String[] args) {
        methodMap = readBaseDir(Utilities.HOME_DIR + "/" + Utilities.BASED_PATH);
        System.out.println("BEGIN MAPPING ... ");
        readSearchFileAndMatch(Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM
                + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + origCloneFile, Utilities.HOME_DIR + "/results/"
                + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + processedCloneFile);
        HashSet<Fragment> flistDecomp = processXML(
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + processedCloneFile,
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + clonePairsDecompFile,
                false);
        HashSet<Fragment> flistOrig = processOrigXML(
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + "/orig-"
                        + Utilities.SYSTEM + "_functions-" + Utilities.FOLDER_NAME_ENDING[Utilities.TYPE] + "/" + Utilities.SYSTEM + "_functions-" + Utilities.CLONE_FILE_ENDING[Utilities.TYPE] + ".xml",
                Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + "/" + clonePairsFile);
        findDifferences(flistDecomp, flistOrig, Utilities.HOME_DIR + "/results/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] );

        AutomaticManualChecker.process();
    }

    public static HashMap<String, ArrayList<Method>> readBaseDir(String inputFolder) {
        HashMap<String, ArrayList<Method>> map = new HashMap<>();
        System.out.println("PROCESSING ORIGINAL FILES ... ");
        File folder = new File(inputFolder);
        List<File> listOfFiles = (List<File>) FileUtils.listFiles(folder, Utilities.EXTENSION, true);
        for (File file : listOfFiles) {
            MethodParser parser = new MethodParser(file.getAbsolutePath(), Utilities.HOME_DIR + "/" + Utilities.BASED_PATH + "/");
            ArrayList<Method> methodsArr = parser.parseMethods();
            for (Method m : methodsArr) {
                ArrayList<Method> mArr = map.get(m.getHeader());
                if (mArr != null) {
                    if (!mArr.contains(m)) {
                        mArr.add(m);
                    }
                } else {
                    ArrayList<Method> ma = new ArrayList<>();
                    ma.add(m);
                    map.put(m.getHeader(), ma);
                }
            }
        }
        return map;
    }

    public static void readSearchFileAndMatch(String file, String outfile) {
        try {
            int count = 1;
            File oFile = new File(outfile);
            FileWriter fw = new FileWriter(oFile.getAbsoluteFile(), false);
            bw = new BufferedWriter(fw);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("file=")) {
                    System.out.print(count + " ");
                    count++;
                    String filePath = line.substring(line.indexOf("file=\"") + 6, line.indexOf(".java") + 5);
                    int startLine = Integer.valueOf(line.substring(line.indexOf("startline=\"") + 11, line.indexOf("\" endline")));
                    int endLine = Integer.valueOf(line.substring(line.indexOf("endline=\"") + 9, line.indexOf("\" pcid")));
                    if (line.contains("$")) {
                        line = line.substring(0, line.indexOf("$")) + line.substring(line.indexOf(".java"), line.length());
                    }
                    MethodParser parser = new MethodParser(filePath, Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/");
                    ArrayList<Method> methodsArr = parser.parseMethods();
                    boolean doLinesMatch = false;
                    boolean isMatched = false;
                    for (Method m : methodsArr) {
                        if ((m.getStartLine() == startLine || m.getStartLine() + 1 == startLine)
                                && m.getEndLine() == endLine) {
                            isMatched = matchMethods(m);
                            doLinesMatch = true;
                            break;
                        }
                    }
                    if (!doLinesMatch) {
                        System.out.print("\nERROR: can't find (maybe lines not match) ");
                        System.out.println(filePath + " (" + startLine + "," + endLine + ")");
                        bw.write(line.replace("<source", "<source matched=\"false\"") + "\n");
                    } else {
                        if (isMatched)
                            bw.write(line.replace("<source", "<source matched=\"true\"")
                                    .replace("pcid=", "origstartline=\"" + matchedMethod.getStartLine() + "\" pcid=")
                                    .replace("pcid=", "origendline=\"" + matchedMethod.getEndLine() + "\" pcid=")
                                    + "\n");
                        else {
                            bw.write(line.replace("<source", "<source matched=\"false\"") + "\n");
                        }
                    }
                } else {
                    bw.write(line + "\n");
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean matchMethods(Method m) throws IOException {
        ArrayList<Method> matchedMathods = methodMap.get(m.getHeader());
        if (matchedMathods != null) {
            if (matchedMathods.size() == 1) {
                System.out.print(". ");
                matchedMethod = matchedMathods.get(0);
                return true;
            } else {
                boolean isMathced = false;
                for (int i = 0; i < matchedMathods.size(); i++) {
                    Method mx = matchedMathods.get(i);
                    if (mx.equals(m)) {
                        System.out.print(". ");
                        matchedMethod = mx;
                        isMathced = true;
                        break;
                    } else if (mx.equalsWithoutFinal(m)) {
                        System.out.print("f ");
                        matchedMethod = mx;
                        isMathced = true;
                        break;
                    } else if (mx.equalsInnerClassWithoutFinal(m)) {
                        System.out.print("$ ");
                        matchedMethod = mx;
                        isMathced = true;
                        break;
                    }
                }
                if (!isMathced) {
                    System.out.println("\nERROR: can't find " + m.toString());
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            System.out.println("\nERROR: can't find (matched method = 0) " + m.toString());
            return false;
        }
    }

    public static int getAnnotationCountFromFile(String basePath, String file, int sline, int noOfLines) {
        int annCount = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(basePath + file));
            String line;
            int count = 1;
            while ((line = br.readLine()) != null) {
                if (count >= sline && count < sline + noOfLines) {
                    // found the beginning of a method
                    if (line.contains("public") || line.contains("private") || line.contains("protected"))
                        break;
                    else if (line.contains("@")) {
                        annCount++;
                    }
                } else if (count == sline + noOfLines)
                    break;
                count++;
            }
            br.close();
            return annCount;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ArrayList<Fragment> findDifferentClones(String file1, String file2) {
        return null;
    }

    public static HashSet<Fragment> processXML(String file, String outfile, boolean isPrintUnmatched) {
        HashSet<Fragment> fSet = new HashSet<>();
        try {
            File oFile = new File(outfile);
            FileWriter fw = new FileWriter(oFile.getAbsoluteFile(), false);
            BufferedWriter bwriter = new BufferedWriter(fw);
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("class");
            System.out.println("\n--- processing clone pairs (decompiled) ---");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                NodeList sList = nNode.getChildNodes();
                ArrayList<Clone> cloneList = new ArrayList<>();
                for (int i = 1; i < sList.getLength(); i += 2) {
                    Node sNode = sList.item(i);
                    Clone c;
                    if (Boolean.valueOf(sNode.getAttributes().getNamedItem("matched").getNodeValue())) {
                        c = new Clone(
                                sNode.getAttributes().getNamedItem("file").getNodeValue(),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("startline").getNodeValue()),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("endline").getNodeValue()),
                                Boolean.valueOf(sNode.getAttributes().getNamedItem("matched").getNodeValue()),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("origstartline").getNodeValue()),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("origendline").getNodeValue()),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("pcid").getNodeValue()));
                    } else {
                        c = new Clone(
                                sNode.getAttributes().getNamedItem("file").getNodeValue(),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("startline").getNodeValue()),
                                Integer.valueOf(sNode.getAttributes().getNamedItem("endline").getNodeValue()),
                                Boolean.valueOf(sNode.getAttributes().getNamedItem("matched").getNodeValue()),
                                -1,
                                -1,
                                Integer.valueOf(sNode.getAttributes().getNamedItem("pcid").getNodeValue()));
                    }
                    cloneList.add(c);
                }
                for (int i = 0; i < cloneList.size(); i++) {
                    for (int j = i + 1; j < cloneList.size(); j++) {
                        Clone cx = cloneList.get(i);
                        Clone cy = cloneList.get(j);
                        Clone c1 = null;
                        Clone c2 = null;
                        if (cx.getFile().compareTo(cy.getFile()) > 0) {
                            c1 = cx;
                            c2 = cy;
                        } else if (cx.getFile().compareTo(cy.getFile()) < 0) {
                            c1 = cy;
                            c2 = cx;
                        } else {
                            if (cx.getOrigstartline() < cy.getOrigstartline()) {
                                c1 = cx;
                                c2 = cy;
                            } else {
                                c1 = cy;
                                c2 = cx;
                            }
                        }

                        // check size, if less than the size threshold, skip it
                        if (c1.getOrigendline() - c1.getOrigstartline() + 1 >= SIZE_THRESHOLD && c2.getOrigendline() - c2.getOrigstartline() + 1 >= SIZE_THRESHOLD) {

                            Fragment f = new Fragment();
                            int lineToGoUp = 5;

                            String firstFile = c1.getFile().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/", "");
                            String secondFile = c2.getFile().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/", "");

                            if (isPrintUnmatched) {
                                bwriter.write(c1.toString().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/", "")
                                        + ","
                                        + c2.toString().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/", "") + "\n");
                                f.setFirstFile(firstFile);
                                f.setSecondFile(secondFile);
                                // checking if there's annotation on top of the method header
                                int annotationCount = getAnnotationCountFromFile(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", firstFile, c1.getStartline(), lineToGoUp);
                                f.setfStart(c1.getStartline() + annotationCount);

                                // f.setfStart(c1.getOrigstartline());
                                f.setfEnd(c1.getOrigendline());
                                // checking if there's annotation on top of the method header
                                annotationCount = getAnnotationCountFromFile(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", secondFile, c2.getStartline(), lineToGoUp);

                                f.setsStart(c2.getStartline() + annotationCount);
//                          f.setsStart(c2.getOrigstartline());
                                f.setsEnd(c2.getOrigendline());
                                fSet.add(f);
                            } else {
                                if (c1.isMatched() && c2.isMatched()) {
                                    bwriter.write(c1.toString().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/", "")
                                            + ","
                                            + c2.toString().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.SEARCH_PATH + "/", "") + "\n");
                                    f.setFirstFile(firstFile);
                                    f.setSecondFile(secondFile);

                                    // checking if there's annotation on top of the method header
                                    int annotationCount = getAnnotationCountFromFile(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", firstFile, c1.getOrigstartline(), lineToGoUp);

                                    if (c1.getFile().contains("Rfc6265CookieProcessor.java"))
                                        System.out.println("ANNOTATION: " + annotationCount + ", " + c1.getFile() + "," + c1.getOrigstartline() + "," + c1.getOrigendline());

                                    f.setfStart(c1.getOrigstartline() + annotationCount);

                                    // f.setfStart(c1.getOrigstartline());
                                    f.setfEnd(c1.getOrigendline());
                                    // checking if there's annotation on top of the method header
                                    annotationCount = getAnnotationCountFromFile(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", secondFile, c2.getOrigstartline(), lineToGoUp);

                                    f.setsStart(c2.getOrigstartline() + annotationCount);
//                                f.setsStart(c2.getOrigstartline());
                                    f.setsEnd(c2.getOrigendline());
                                    fSet.add(f);
                                } else {
                                    System.out.println("ERROR: found unmatched pair: " + c1.toString()
                                            + ", " + c2.toString());
                                }
                            }
                        }
                    }
                }
            }
            bwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fSet;
    }

    public static HashSet<Fragment> processOrigXML(String file, String outfile) {
        HashSet<Fragment> fset = new HashSet<>();
        try {
            File oFile = new File(outfile);
            FileWriter fw = new FileWriter(oFile.getAbsoluteFile(), false);
            BufferedWriter bwriter = new BufferedWriter(fw);
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("class");
            System.out.println("\n--- processing clone pairs (original) ---");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                NodeList sList = nNode.getChildNodes();
                ArrayList<Clone> cloneList = new ArrayList<>();
                for (int i = 1; i < sList.getLength(); i += 2) {
                    Node sNode = sList.item(i);
                    Clone c = new Clone(
                            sNode.getAttributes().getNamedItem("file").getNodeValue(),
                            Integer.valueOf(sNode.getAttributes().getNamedItem("startline").getNodeValue()),
                            Integer.valueOf(sNode.getAttributes().getNamedItem("endline").getNodeValue()),
                            true,
                            -1,
                            -1,
                            Integer.valueOf(sNode.getAttributes().getNamedItem("pcid").getNodeValue()));
                    cloneList.add(c);
                }
                for (int i = 0; i < cloneList.size(); i++) {
                    for (int j = i + 1; j < cloneList.size(); j++) {
                        Clone cx = cloneList.get(i);
                        Clone cy = cloneList.get(j);
                        Clone c1 = null;
                        Clone c2 = null;
                        if (cx.getFile().compareTo(cy.getFile()) > 0) {
                            c1 = cx;
                            c2 = cy;
                        } else if (cx.getFile().compareTo(cy.getFile()) < 0) {
                            c1 = cy;
                            c2 = cx;
                        } else {
                            if (cx.getStartline() < cy.getStartline()) {
                                c1 = cx;
                                c2 = cy;
                            } else {
                                c1 = cy;
                                c2 = cx;
                            }
                        }
                        Fragment f = new Fragment();
                        bwriter.write(c1.toStringOrig().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", "")
                                + ","
                                + c2.toStringOrig().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", "") + "\n");
                        f.setFirstFile(c1.getFile().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", ""));
                        f.setSecondFile(c2.getFile().replace(Utilities.SRC_HOME_DIR + "/" + Utilities.BASED_PATH + "/", ""));

                        f.setfStart(c1.getStartline());
                        f.setfEnd(c1.getEndline());
                        f.setsStart(c2.getStartline());
                        f.setsEnd(c2.getEndline());
                        fset.add(f);
                    }
                }
            }
            bwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fset;
    }

    public static void findDifferences(HashSet<Fragment> cpDecomp, HashSet<Fragment> cpOrig, String resultsPath) {
        System.out.println("BEFORE: DECOMP = " + cpDecomp.size() + ", ORIG = " + cpOrig.size());
//        HashSet<Fragment> union = new HashSet(cpDecomp);
//        union.addAll(cpOrig);
//        System.out.println("UNION: " + union.size());
        HashSet<Fragment> intersect = new HashSet<>(cpDecomp);
        intersect.retainAll(cpOrig);
        System.out.println("INTERSECT: " + intersect.size());
        HashSet<Fragment> cpDecompDiff = new HashSet<>(cpDecomp);
        cpDecompDiff.removeAll(cpOrig);
        HashSet<Fragment> cpOrigDiff = new HashSet<>(cpOrig);
        cpOrigDiff.removeAll(cpDecomp);
        System.out.println("AFTER: DECOMP = " + cpDecompDiff.size() + ", ORIG = " + cpOrigDiff.size());
        try {
            File oFileAgreed = new File(resultsPath + "/" + Utilities.SYSTEM + "_agreed.csv");
            FileWriter fwAgreed = new FileWriter(oFileAgreed.getAbsoluteFile(), false);
            BufferedWriter bwAgreed = new BufferedWriter(fwAgreed);
            for (Fragment f : intersect) {
                bwAgreed.write(f.toString() + "\n");
            }
            bwAgreed.close();
            File oFile = new File(resultsPath + "/" + Utilities.SYSTEM + "_only_decomp.csv");
            FileWriter fw = new FileWriter(oFile.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Fragment f : cpDecompDiff) {
                bw.write(f.toString() + "\n");
            }
            bw.close();
            File oFile2 = new File(resultsPath + "/" + Utilities.SYSTEM + "_only_orig.csv");
            FileWriter fw2 = new FileWriter(oFile2.getAbsoluteFile(), false);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            for (Fragment f : cpOrigDiff) {
                bw2.write(f.toString() + "\n");
            }
            bw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
