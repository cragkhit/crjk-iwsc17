package org.apache.jasper.compiler;
import java.util.ArrayList;
import java.util.List;
public class SmapStratum {
    private static class LineInfo {
        private int inputStartLine = -1;
        private int outputStartLine = -1;
        private int lineFileID = 0;
        private int inputLineCount = 1;
        private int outputLineIncrement = 1;
        private boolean lineFileIDSet = false;
        public void setInputStartLine ( int inputStartLine ) {
            if ( inputStartLine < 0 ) {
                throw new IllegalArgumentException ( "" + inputStartLine );
            }
            this.inputStartLine = inputStartLine;
        }
        public void setOutputStartLine ( int outputStartLine ) {
            if ( outputStartLine < 0 ) {
                throw new IllegalArgumentException ( "" + outputStartLine );
            }
            this.outputStartLine = outputStartLine;
        }
        public void setLineFileID ( int lineFileID ) {
            if ( lineFileID < 0 ) {
                throw new IllegalArgumentException ( "" + lineFileID );
            }
            this.lineFileID = lineFileID;
            this.lineFileIDSet = true;
        }
        public void setInputLineCount ( int inputLineCount ) {
            if ( inputLineCount < 0 ) {
                throw new IllegalArgumentException ( "" + inputLineCount );
            }
            this.inputLineCount = inputLineCount;
        }
        public void setOutputLineIncrement ( int outputLineIncrement ) {
            if ( outputLineIncrement < 0 ) {
                throw new IllegalArgumentException ( "" + outputLineIncrement );
            }
            this.outputLineIncrement = outputLineIncrement;
        }
        public String getString() {
            if ( inputStartLine == -1 || outputStartLine == -1 ) {
                throw new IllegalStateException();
            }
            StringBuilder out = new StringBuilder();
            out.append ( inputStartLine );
            if ( lineFileIDSet ) {
                out.append ( "#" + lineFileID );
            }
            if ( inputLineCount != 1 ) {
                out.append ( "," + inputLineCount );
            }
            out.append ( ":" + outputStartLine );
            if ( outputLineIncrement != 1 ) {
                out.append ( "," + outputLineIncrement );
            }
            out.append ( '\n' );
            return out.toString();
        }
        @Override
        public String toString() {
            return getString();
        }
    }
    private final String stratumName;
    private final List<String> fileNameList;
    private final List<String> filePathList;
    private final List<LineInfo> lineData;
    private int lastFileID;
    public SmapStratum ( String stratumName ) {
        this.stratumName = stratumName;
        fileNameList = new ArrayList<>();
        filePathList = new ArrayList<>();
        lineData = new ArrayList<>();
        lastFileID = 0;
    }
    public void addFile ( String filename ) {
        addFile ( filename, filename );
    }
    public void addFile ( String filename, String filePath ) {
        int pathIndex = filePathList.indexOf ( filePath );
        if ( pathIndex == -1 ) {
            fileNameList.add ( filename );
            filePathList.add ( filePath );
        }
    }
    public void optimizeLineSection() {
        int i = 0;
        while ( i < lineData.size() - 1 ) {
            LineInfo li = lineData.get ( i );
            LineInfo liNext = lineData.get ( i + 1 );
            if ( !liNext.lineFileIDSet
                    && liNext.inputStartLine == li.inputStartLine
                    && liNext.inputLineCount == 1
                    && li.inputLineCount == 1
                    && liNext.outputStartLine
                    == li.outputStartLine
                    + li.inputLineCount * li.outputLineIncrement ) {
                li.setOutputLineIncrement (
                    liNext.outputStartLine
                    - li.outputStartLine
                    + liNext.outputLineIncrement );
                lineData.remove ( i + 1 );
            } else {
                i++;
            }
        }
        i = 0;
        while ( i < lineData.size() - 1 ) {
            LineInfo li = lineData.get ( i );
            LineInfo liNext = lineData.get ( i + 1 );
            if ( !liNext.lineFileIDSet
                    && liNext.inputStartLine == li.inputStartLine + li.inputLineCount
                    && liNext.outputLineIncrement == li.outputLineIncrement
                    && liNext.outputStartLine
                    == li.outputStartLine
                    + li.inputLineCount * li.outputLineIncrement ) {
                li.setInputLineCount ( li.inputLineCount + liNext.inputLineCount );
                lineData.remove ( i + 1 );
            } else {
                i++;
            }
        }
    }
    public void addLineData (
        int inputStartLine,
        String inputFileName,
        int inputLineCount,
        int outputStartLine,
        int outputLineIncrement ) {
        int fileIndex = filePathList.indexOf ( inputFileName );
        if ( fileIndex == -1 )
            throw new IllegalArgumentException (
                "inputFileName: " + inputFileName );
        if ( outputStartLine == 0 ) {
            return;
        }
        LineInfo li = new LineInfo();
        li.setInputStartLine ( inputStartLine );
        li.setInputLineCount ( inputLineCount );
        li.setOutputStartLine ( outputStartLine );
        li.setOutputLineIncrement ( outputLineIncrement );
        if ( fileIndex != lastFileID ) {
            li.setLineFileID ( fileIndex );
        }
        lastFileID = fileIndex;
        lineData.add ( li );
    }
    public String getStratumName() {
        return stratumName;
    }
    public String getString() {
        if ( fileNameList.size() == 0 || lineData.size() == 0 ) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        out.append ( "*S " + stratumName + "\n" );
        out.append ( "*F\n" );
        int bound = fileNameList.size();
        for ( int i = 0; i < bound; i++ ) {
            if ( filePathList.get ( i ) != null ) {
                out.append ( "+ " + i + " " + fileNameList.get ( i ) + "\n" );
                String filePath = filePathList.get ( i );
                if ( filePath.startsWith ( "/" ) ) {
                    filePath = filePath.substring ( 1 );
                }
                out.append ( filePath + "\n" );
            } else {
                out.append ( i + " " + fileNameList.get ( i ) + "\n" );
            }
        }
        out.append ( "*L\n" );
        bound = lineData.size();
        for ( int i = 0; i < bound; i++ ) {
            LineInfo li = lineData.get ( i );
            out.append ( li.getString() );
        }
        return out.toString();
    }
    @Override
    public String toString() {
        return getString();
    }
}
