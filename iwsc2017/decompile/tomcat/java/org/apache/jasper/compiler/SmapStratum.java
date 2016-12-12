package org.apache.jasper.compiler;
import java.util.ArrayList;
import java.util.List;
public class SmapStratum {
    private final String stratumName;
    private final List<String> fileNameList;
    private final List<String> filePathList;
    private final List<LineInfo> lineData;
    private int lastFileID;
    public SmapStratum ( final String stratumName ) {
        this.stratumName = stratumName;
        this.fileNameList = new ArrayList<String>();
        this.filePathList = new ArrayList<String>();
        this.lineData = new ArrayList<LineInfo>();
        this.lastFileID = 0;
    }
    public void addFile ( final String filename ) {
        this.addFile ( filename, filename );
    }
    public void addFile ( final String filename, final String filePath ) {
        final int pathIndex = this.filePathList.indexOf ( filePath );
        if ( pathIndex == -1 ) {
            this.fileNameList.add ( filename );
            this.filePathList.add ( filePath );
        }
    }
    public void optimizeLineSection() {
        int i = 0;
        while ( i < this.lineData.size() - 1 ) {
            final LineInfo li = this.lineData.get ( i );
            final LineInfo liNext = this.lineData.get ( i + 1 );
            if ( !liNext.lineFileIDSet && liNext.inputStartLine == li.inputStartLine && liNext.inputLineCount == 1 && li.inputLineCount == 1 && liNext.outputStartLine == li.outputStartLine + li.inputLineCount * li.outputLineIncrement ) {
                li.setOutputLineIncrement ( liNext.outputStartLine - li.outputStartLine + liNext.outputLineIncrement );
                this.lineData.remove ( i + 1 );
            } else {
                ++i;
            }
        }
        i = 0;
        while ( i < this.lineData.size() - 1 ) {
            final LineInfo li = this.lineData.get ( i );
            final LineInfo liNext = this.lineData.get ( i + 1 );
            if ( !liNext.lineFileIDSet && liNext.inputStartLine == li.inputStartLine + li.inputLineCount && liNext.outputLineIncrement == li.outputLineIncrement && liNext.outputStartLine == li.outputStartLine + li.inputLineCount * li.outputLineIncrement ) {
                li.setInputLineCount ( li.inputLineCount + liNext.inputLineCount );
                this.lineData.remove ( i + 1 );
            } else {
                ++i;
            }
        }
    }
    public void addLineData ( final int inputStartLine, final String inputFileName, final int inputLineCount, final int outputStartLine, final int outputLineIncrement ) {
        final int fileIndex = this.filePathList.indexOf ( inputFileName );
        if ( fileIndex == -1 ) {
            throw new IllegalArgumentException ( "inputFileName: " + inputFileName );
        }
        if ( outputStartLine == 0 ) {
            return;
        }
        final LineInfo li = new LineInfo();
        li.setInputStartLine ( inputStartLine );
        li.setInputLineCount ( inputLineCount );
        li.setOutputStartLine ( outputStartLine );
        li.setOutputLineIncrement ( outputLineIncrement );
        if ( fileIndex != this.lastFileID ) {
            li.setLineFileID ( fileIndex );
        }
        this.lastFileID = fileIndex;
        this.lineData.add ( li );
    }
    public String getStratumName() {
        return this.stratumName;
    }
    public String getString() {
        if ( this.fileNameList.size() == 0 || this.lineData.size() == 0 ) {
            return null;
        }
        final StringBuilder out = new StringBuilder();
        out.append ( "*S " + this.stratumName + "\n" );
        out.append ( "*F\n" );
        for ( int bound = this.fileNameList.size(), i = 0; i < bound; ++i ) {
            if ( this.filePathList.get ( i ) != null ) {
                out.append ( "+ " + i + " " + this.fileNameList.get ( i ) + "\n" );
                String filePath = this.filePathList.get ( i );
                if ( filePath.startsWith ( "/" ) ) {
                    filePath = filePath.substring ( 1 );
                }
                out.append ( filePath + "\n" );
            } else {
                out.append ( i + " " + this.fileNameList.get ( i ) + "\n" );
            }
        }
        out.append ( "*L\n" );
        for ( int bound = this.lineData.size(), i = 0; i < bound; ++i ) {
            final LineInfo li = this.lineData.get ( i );
            out.append ( li.getString() );
        }
        return out.toString();
    }
    @Override
    public String toString() {
        return this.getString();
    }
    private static class LineInfo {
        private int inputStartLine;
        private int outputStartLine;
        private int lineFileID;
        private int inputLineCount;
        private int outputLineIncrement;
        private boolean lineFileIDSet;
        private LineInfo() {
            this.inputStartLine = -1;
            this.outputStartLine = -1;
            this.lineFileID = 0;
            this.inputLineCount = 1;
            this.outputLineIncrement = 1;
            this.lineFileIDSet = false;
        }
        public void setInputStartLine ( final int inputStartLine ) {
            if ( inputStartLine < 0 ) {
                throw new IllegalArgumentException ( "" + inputStartLine );
            }
            this.inputStartLine = inputStartLine;
        }
        public void setOutputStartLine ( final int outputStartLine ) {
            if ( outputStartLine < 0 ) {
                throw new IllegalArgumentException ( "" + outputStartLine );
            }
            this.outputStartLine = outputStartLine;
        }
        public void setLineFileID ( final int lineFileID ) {
            if ( lineFileID < 0 ) {
                throw new IllegalArgumentException ( "" + lineFileID );
            }
            this.lineFileID = lineFileID;
            this.lineFileIDSet = true;
        }
        public void setInputLineCount ( final int inputLineCount ) {
            if ( inputLineCount < 0 ) {
                throw new IllegalArgumentException ( "" + inputLineCount );
            }
            this.inputLineCount = inputLineCount;
        }
        public void setOutputLineIncrement ( final int outputLineIncrement ) {
            if ( outputLineIncrement < 0 ) {
                throw new IllegalArgumentException ( "" + outputLineIncrement );
            }
            this.outputLineIncrement = outputLineIncrement;
        }
        public String getString() {
            if ( this.inputStartLine == -1 || this.outputStartLine == -1 ) {
                throw new IllegalStateException();
            }
            final StringBuilder out = new StringBuilder();
            out.append ( this.inputStartLine );
            if ( this.lineFileIDSet ) {
                out.append ( "#" + this.lineFileID );
            }
            if ( this.inputLineCount != 1 ) {
                out.append ( "," + this.inputLineCount );
            }
            out.append ( ":" + this.outputStartLine );
            if ( this.outputLineIncrement != 1 ) {
                out.append ( "," + this.outputLineIncrement );
            }
            out.append ( '\n' );
            return out.toString();
        }
        @Override
        public String toString() {
            return this.getString();
        }
    }
}
