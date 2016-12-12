import java.io.*;
public class AutomaticManualChecker {
    private static String SYSTEM = Utilities.SYSTEM;
    private static String projectLocation = Utilities.HOME_DIR + "/results";
    private static String SO_DIR = Utilities.HOME_DIR + "/" + Utilities.BASED_PATH + "/";

    public static void main ( String[] args ) {
        process(SYSTEM + "_agreed.csv");
    }

    public static void process (String fileName) {
        System.out.println ( "FILTERING BOILTER-PLATE CODE OF " + fileName + " ...");
        checkEqualsMethodsAndGettersSetters (
                fileName
                , 1, -1, 0
                , SO_DIR );
//        checkEqualsMethodsAndGettersSetters ( projectLocation + "/" + Utilities.SYSTEM + "/" + Utilities.RESULTS_FOLDER_NAME[Utilities.TYPE] + "/" + SYSTEM + "_only_decomp.csv"
//                , 1, -1, 0
//                , SO_DIR );
        System.out.println ( "DONE" );
    }


    public static void checkEqualsMethodsAndGettersSetters ( String file1, int start, int end, int so_starting_index, String path ) {
        String cloneFile = file1;
        String line, sLine, qLine;
        String cvsSplitBy = ",";
        try {
            File oFile = new File ( file1.replace ( ".csv", "_with_checks.csv" ) );
            FileWriter fw = new FileWriter ( oFile.getAbsoluteFile(), false );
            BufferedWriter bw = new BufferedWriter ( fw );
            FileReader fr = new FileReader ( cloneFile );
            BufferedReader br = new BufferedReader ( fr );
            int count = 1;
            while ( ( line = br.readLine() ) != null ) {

                /* check for identical inner class methods */
                String[] lines = line.split(",");
                if (lines[so_starting_index].trim().equals(lines[so_starting_index+3].trim()) &&
                        lines[so_starting_index+1].trim().equals(lines[so_starting_index+4].trim()) &&
                        lines[so_starting_index+2].trim().equals(lines[so_starting_index+5].trim()))
                {
                    bw.write ( line + ",inner class generates the same method after decompiled\n" );
                    continue;
                }

                    /* check boiler-plate code */
                if ( end == -1 || ( count >= start && count <= end ) ) {
                    String[] clone = line.split ( cvsSplitBy );
                    boolean foundFirst = false, foundSecond = false, foundGetter = false, foundSetter = false;
                    boolean foundFirstGetter = false, foundSecondGetter = false, foundFirstHashCode = false, foundSecondHashCode = false;
                    String getterText = "";
                    BufferedReader sF = new BufferedReader ( new FileReader ( path + clone[so_starting_index] ) );
                    int lineCount = 0;
                    while ( ( sLine = sF.readLine() ) != null ) {
                        lineCount++;
                        if ( lineCount == Integer.parseInt ( clone[so_starting_index + 1].trim() ) ) {
                            if ( sLine.contains ( "boolean equals (" ) ) {
                                foundFirst = true;
                            } else if ( sLine.contains ( "@Override" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "boolean equals (" ) ) {
                                    foundFirst = true;
                                } else if ( sLine.contains ( "int hashCode()" ) ) {
                                    foundFirstHashCode = true;
                                }
                            } else if ( sLine.contains ( "return result;" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "}" ) ) {
                                    sLine = sF.readLine();
                                    if ( sLine.contains ( "@Override" ) ) {
                                        sLine = sF.readLine();
                                        if ( sLine.contains ( "boolean equals (" ) ) {
                                            foundFirst = true;
                                        }
                                    }
                                }
                            } else if ( sLine.contains ( "this.name = name;" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "}" ) ) {
                                    sLine = sF.readLine();
                                    if ( sLine.contains ( "getId() {" ) ) {
                                        sLine = sF.readLine();
                                        if ( sLine.contains ( "return id;" ) ) {
                                            foundFirstGetter = true;
                                        }
                                    }
                                }
                            } else if ( sLine.matches ( "\\s*public\\s*int\\s*hashCode\\(\\)\\s*\\{\\s*" ) ) {
                                foundFirstHashCode = true;
                            } else if ( sLine.matches ( "\\s*result\\s*=\\s*prime\\s*\\*\\s*result\\s*\\+\\s*\\(\\s*\\(\\s*[a-zA-z0-9]*\\s*==\\s*null\\s*\\)\\s*\\?\\s*0\\s*:\\s*[a-zA-Z0-9]*\\.hashCode\\(\\)\\s*\\);" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "return result;" ) ) {
                                    foundFirstHashCode = true;
                                }
                            } else if ( sLine.contains ( "result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "return result;" ) ) {
                                    foundFirstHashCode = true;
                                }
                            } else if ( sLine.contains ( "this.id = id;" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "}" ) ) {
                                    foundFirstGetter = true;
                                }
                            } else if ( sLine.matches ( "\\s*public\\s*String\\s*get[A-Z]?[a-zA-Z0-9]*\\(\\)\\s*\\{\\s*" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.matches ( "\\s*return\\s*[a-zA-Z0-9]*;" ) ) {
                                    foundFirstGetter = true;
                                }
                            } else if ( sLine.matches ( "\\s*public.*get[A-Z].*().*\\{" ) ) {
                                foundGetter = true;
                                getterText = sLine;
                            } else if ( sLine.matches ( "\\s*return\\s*.*;.*" ) ) {
                                sLine = sF.readLine();
                                if ( sLine.contains ( "}" ) ) {
                                    sLine = sF.readLine();
                                    if ( sLine.matches ( "\\s*public.*set[A-Z].*().*\\{" ) ) {
                                        foundSetter = true;
                                        getterText = sLine;
                                    }
                                }
                            }
                            break;
                        }
                    }
                    sF.close();
                    BufferedReader qF = new BufferedReader ( new FileReader ( path + clone[so_starting_index + 3] ) );
                    lineCount = 0;
                    while ( ( qLine = qF.readLine() ) != null ) {
                        lineCount++;
                        if ( lineCount == Integer.parseInt ( clone[so_starting_index + 4].trim() ) ) {
                            if ( qLine.contains ( "boolean equals (" ) ) {
                                foundSecond = true;
                            } else if ( qLine.contains ( "@Override" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.contains ( "boolean equals (" ) ) {
                                    foundSecond = true;
                                } else if ( qLine.contains ( "int hashCode()" ) ) {
                                    foundSecondHashCode = true;
                                }
                            } else if ( qLine.contains ( "return result;" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.contains ( "}" ) ) {
                                    qLine = qF.readLine();
                                    if ( qLine.contains ( "@Override" ) ) {
                                        qLine = qF.readLine();
                                        if ( qLine.contains ( "boolean equals (" ) ) {
                                            foundSecond = true;
                                        }
                                    }
                                }
                            } else if ( sLine.matches ( "\\s*public\\s*int\\s*hashCode\\(\\)\\s*\\{\\s*" ) ) {
                                foundSecondHashCode = true;
                            } else if ( qLine.contains ( "result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.contains ( "return result;" ) ) {
                                    foundSecondHashCode = true;
                                }
                            } else if ( qLine.matches ( "\\s*result\\s*=\\s*prime\\s*\\*\\s*result\\s*\\+\\s*\\(\\s*\\(\\s*[a-zA-z0-9]*\\s*==\\s*null\\s*\\)\\s*\\?\\s*0\\s*:\\s*[a-zA-Z0-9]*\\.hashCode\\(\\)\\s*\\);" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.contains ( "return result;" ) ) {
                                    foundSecondHashCode = true;
                                }
                            } else if ( qLine.contains ( "this.id = id;" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.contains ( "}" ) ) {
                                    foundSecondGetter = true;
                                }
                            } else if ( qLine.matches ( "\\s*public\\s*String\\s*get[A-Z]?[a-zA-Z0-9]*\\(\\)\\s*\\{\\s*" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.matches ( "\\s*return\\s*[a-zA-Z0-9]*;" ) ) {
                                    foundSecondGetter = true;
                                }
                            } else if ( qLine.contains ( "this.name = name;" ) ) {
                                qLine = qF.readLine();
                                if ( qLine.contains ( "}" ) ) {
                                    qLine = qF.readLine();
                                    if ( qLine.contains ( "getId() {" ) ) {
                                        qLine = qF.readLine();
                                        if ( qLine.contains ( "return id;" ) ) {
                                            foundSecondGetter = true;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    qF.close();
                    if ( foundFirst && foundSecond ) {
                        bw.write ( line + ",similar equals() methods\n" );
                    } else if ( foundFirstGetter && foundSecondGetter ) {
                        bw.write ( line + ",similar getters & setters methods\n" );
                    } else if ( foundGetter ) {
                        bw.write ( line + ",similar getter\n" );
                    } else if ( foundSetter ) {
                        bw.write ( line + ",similar setter\n" );
                    } else if ( foundFirstHashCode && foundSecondHashCode ) {
                        bw.write ( line + ",similar hashCode() and equals()\n" );
                    } else {
                        bw.write ( line + "\n" );
                    }
                }
                count++;
            }
            br.close();
            bw.close();
            fw.close();
            fr.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
