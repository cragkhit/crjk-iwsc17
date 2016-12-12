import com.github.javaparser.ast.body.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
public class Method {
    /* TODO fix the FrameType... = FrameTyep[] thing */
    private String file;
    private String name;
    private String src;
    private int startLine;
    private int endLine;
    private List<Parameter> params;
    public Method() {
    }
    public Method ( String file, String name, String src, int startLine, int endLine, List<Parameter> params ) {
        this.file = file;
        this.name = name;
        this.src = src;
        this.startLine = startLine;
        this.endLine = endLine;
        this.params = params;
    }
    public String getFile() {
        return file;
    }
    public void setFile ( String file ) {
        this.file = file;
    }
    public String getName() {
        return name;
    }
    public void setName ( String name ) {
        this.name = name;
    }
    public String getSrc() {
        return src;
    }
    public void setSrc ( String src ) {
        this.src = src;
    }
    public int getStartLine() {
        return startLine;
    }
    public int getEndLine() {
        return endLine;
    }
    public List<Parameter> getParams() {
        return params;
    }
    public void setStartLine ( int startLine ) {
        this.startLine = startLine;
    }
    public void setEndLine ( int endLine ) {
        this.endLine = endLine;
    }
    public void setParams ( List<Parameter> params ) {
        this.params = params;
    }
    public String getHeader() {
        return name;
    }
    public String toString() {
        return name + "," + params.toString() + ": " + startLine + "," + endLine + " from " + file;
    }
    public boolean equals ( Object o ) {
        Method m = ( Method ) o;
        if ( m.getParams().size() != params.size() ) {
            return false;
        } else {
            return ( ( file.equals ( m.getFile() ) ) && ( name.equals ( m.getName() ) ) && ( params.equals ( m.params ) ) );
        }
    }
    public boolean equalsWithoutFinal ( Object o ) {
        String FINAL = "final ";
        boolean allParamsMatched = false;
        Method m = ( Method ) o;
        if ( m.getParams().size() != params.size() ) {
            return false;
        } else {
            List<Parameter> mParams;
            int matchedParamCount = 0;
            if ( m.getParams() instanceof LinkedList ) {
                mParams = ( LinkedList<Parameter> ) m.getParams();
            } else {
                mParams = ( ArrayList<Parameter> ) m.getParams();
            }
            for (int i = 0; i < params.size(); i++) {
//                if (file.contains("ELParser") && m.getFile().contains("ELParser")) {
//                    System.out.println("\n|" + params.get(i).toString().replace(FINAL, "").trim() + "|" + ":|" + mParams.get(i).toString().replace(FINAL, "").trim() + "|");
//                }
                if (params.get(i).toString().replace(FINAL, "").trim().equals(mParams.get(i).toString().replace(FINAL, "").trim())) {
                    matchedParamCount++;
                } else if (params.get(i).toString().contains(".") || mParams.get(i).toString().contains(".")) {

                    String[] params1WithPackageArr = params.get(i).toString().split("\\.");
                    String[] params2WithPackageArr = m.getParams().get(i).toString().split("\\.");

                    // System.out.println("XXX " + params1WithPackageArr.length + ", " + params2WithPackageArr.length);
//                    if (file.contains("ELParser") && m.getFile().contains("ELParser")) {
//                        System.out.println("XXX " + params1WithPackageArr[params1WithPackageArr.length - 1].trim()
//                                + ", " + params2WithPackageArr[params2WithPackageArr.length - 1].trim());
//                    }

                    if (params1WithPackageArr.length >= 1 && params2WithPackageArr.length >= 1) {

                        if (params1WithPackageArr[params1WithPackageArr.length - 1].trim().replace(FINAL, "").trim()
                                .equals(params2WithPackageArr[params2WithPackageArr.length - 1].trim().replace(FINAL, "").trim())) {
                            matchedParamCount++;
                        }
                    }
                } else if (params.get(i).toString().contains("[]") && mParams.get(i).toString().contains("[]")) {
                    String p1 = params.get(i).toString().replace("[]", "").trim().replace(FINAL, "").trim();
                    String p2 = mParams.get(i).toString().replace("[]", "").trim().replace(FINAL, "").trim();
//                    System.out.println("XXX |" + p1 + "|, |" + p2 + "|");
                    if (p1.equals(p2)) {
                        matchedParamCount++;
                    }
                }
            }
            if ( matchedParamCount == m.getParams().size() ) {
                allParamsMatched = true;
            }

//            if (file.contains("ELParser") && m.getFile().contains("ELParser"))
//            {
//                System.out.println("\n" + this.getFile() + ":" + this.getParams() + "," + m.getFile() + ":" + m.getParams() + "," + allParamsMatched);
//            }

            return ( ( file.equals ( m.getFile() ) ) && ( name.equals ( m.getName() ) ) && allParamsMatched );
        }
    }
    public boolean equalsInnerClassWithoutFinal ( Object o ) {
        String FINAL = "final ";
        boolean allParamsMatched = false;
        Method m = ( Method ) o;
        if ( m.getParams().size() != params.size() ) {
            return false;
        } else {
            List<Parameter> mParams;
            int matchedParamCount = 0;
            if ( m.getParams() instanceof LinkedList ) {
                mParams = ( LinkedList<Parameter> ) m.getParams();
            } else {
                mParams = ( ArrayList<Parameter> ) m.getParams();
            }
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i).toString().replace(FINAL, "").trim().equals(mParams.get(i).toString().replace(FINAL, "").trim())) {
                    matchedParamCount++;
                } else if (params.get(i).toString().contains(".") || mParams.get(i).toString().contains(".")) {

                    String[] params1WithPackageArr = params.get(i).toString().split("\\.");
                    String[] params2WithPackageArr = m.getParams().get(i).toString().split("\\.");

                    if (params1WithPackageArr.length >= 1 && params2WithPackageArr.length >= 1) {

                        if (params1WithPackageArr[params1WithPackageArr.length - 1].trim().replace(FINAL, "").trim()
                                .equals(params2WithPackageArr[params2WithPackageArr.length - 1].trim().replace(FINAL, "").trim())) {
                            matchedParamCount++;
                        }
                    }
                } else if (params.get(i).toString().contains("[]") && mParams.get(i).toString().contains("[]")) {

                    String p1 = params.get(i).toString().replace("[]", "").trim().replace(FINAL, "").trim();
                    String p2 = mParams.get(i).toString().replace("[]", "").trim().replace(FINAL, "").trim();
//                    System.out.println("XXX |" + p1 + "|, |" + p2 + "|");

                    if (p1.equals(p2)) {
                        matchedParamCount++;
                    }
                }
            }
            if ( matchedParamCount == m.getParams().size() ) {
                allParamsMatched = true;
            }
            String fileOne = this.getFile();
            String fileTwo = "";
            if ( m.getFile().contains ( "$" ) ) {
                fileTwo = m.getFile().substring ( 0, m.getFile().indexOf ( "$" ) ) + ".java";
            } else {
                fileTwo = m.getFile();
            }
            return ( fileOne.equals ( fileTwo ) && name.equals ( m.getName() ) && allParamsMatched );
        }
    }
}
