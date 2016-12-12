package org.apache.catalina.ssi;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.io.IOException;
import java.util.Locale;
import java.io.Writer;
import org.apache.catalina.util.IOTools;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
public class SSIProcessor {
    protected static final String COMMAND_START = "<!--#";
    protected static final String COMMAND_END = "-->";
    protected final SSIExternalResolver ssiExternalResolver;
    protected final HashMap<String, SSICommand> commands;
    protected final int debug;
    protected final boolean allowExec;
    public SSIProcessor ( final SSIExternalResolver ssiExternalResolver, final int debug, final boolean allowExec ) {
        this.commands = new HashMap<String, SSICommand>();
        this.ssiExternalResolver = ssiExternalResolver;
        this.debug = debug;
        this.allowExec = allowExec;
        this.addBuiltinCommands();
    }
    protected void addBuiltinCommands() {
        this.addCommand ( "config", new SSIConfig() );
        this.addCommand ( "echo", new SSIEcho() );
        if ( this.allowExec ) {
            this.addCommand ( "exec", new SSIExec() );
        }
        this.addCommand ( "include", new SSIInclude() );
        this.addCommand ( "flastmod", new SSIFlastmod() );
        this.addCommand ( "fsize", new SSIFsize() );
        this.addCommand ( "printenv", new SSIPrintenv() );
        this.addCommand ( "set", new SSISet() );
        final SSIConditional ssiConditional = new SSIConditional();
        this.addCommand ( "if", ssiConditional );
        this.addCommand ( "elif", ssiConditional );
        this.addCommand ( "endif", ssiConditional );
        this.addCommand ( "else", ssiConditional );
    }
    public void addCommand ( final String name, final SSICommand command ) {
        this.commands.put ( name, command );
    }
    public long process ( final Reader reader, long lastModifiedDate, final PrintWriter writer ) throws IOException {
        final SSIMediator ssiMediator = new SSIMediator ( this.ssiExternalResolver, lastModifiedDate );
        StringWriter stringWriter = new StringWriter();
        IOTools.flow ( reader, stringWriter );
        final String fileContents = stringWriter.toString();
        stringWriter = null;
        int index = 0;
        boolean inside = false;
        final StringBuilder command = new StringBuilder();
        try {
            while ( index < fileContents.length() ) {
                final char c = fileContents.charAt ( index );
                if ( !inside ) {
                    if ( c == "<!--#".charAt ( 0 ) && this.charCmp ( fileContents, index, "<!--#" ) ) {
                        inside = true;
                        index += "<!--#".length();
                        command.setLength ( 0 );
                    } else {
                        if ( !ssiMediator.getConditionalState().processConditionalCommandsOnly ) {
                            writer.write ( c );
                        }
                        ++index;
                    }
                } else if ( c == "-->".charAt ( 0 ) && this.charCmp ( fileContents, index, "-->" ) ) {
                    inside = false;
                    index += "-->".length();
                    final String strCmd = this.parseCmd ( command );
                    if ( this.debug > 0 ) {
                        this.ssiExternalResolver.log ( "SSIProcessor.process -- processing command: " + strCmd, null );
                    }
                    final String[] paramNames = this.parseParamNames ( command, strCmd.length() );
                    final String[] paramValues = this.parseParamValues ( command, strCmd.length(), paramNames.length );
                    final String configErrMsg = ssiMediator.getConfigErrMsg();
                    final SSICommand ssiCommand = this.commands.get ( strCmd.toLowerCase ( Locale.ENGLISH ) );
                    String errorMessage = null;
                    if ( ssiCommand == null ) {
                        errorMessage = "Unknown command: " + strCmd;
                    } else if ( paramValues == null ) {
                        errorMessage = "Error parsing directive parameters.";
                    } else if ( paramNames.length != paramValues.length ) {
                        errorMessage = "Parameter names count does not match parameter values count on command: " + strCmd;
                    } else if ( !ssiMediator.getConditionalState().processConditionalCommandsOnly || ssiCommand instanceof SSIConditional ) {
                        final long lmd = ssiCommand.process ( ssiMediator, strCmd, paramNames, paramValues, writer );
                        if ( lmd > lastModifiedDate ) {
                            lastModifiedDate = lmd;
                        }
                    }
                    if ( errorMessage == null ) {
                        continue;
                    }
                    this.ssiExternalResolver.log ( errorMessage, null );
                    writer.write ( configErrMsg );
                } else {
                    command.append ( c );
                    ++index;
                }
            }
        } catch ( SSIStopProcessingException ex ) {}
        return lastModifiedDate;
    }
    protected String[] parseParamNames ( final StringBuilder cmd, final int start ) {
        int bIdx = start;
        int i = 0;
        int quotes = 0;
        boolean inside = false;
        final StringBuilder retBuf = new StringBuilder();
        while ( bIdx < cmd.length() ) {
            if ( !inside ) {
                while ( bIdx < cmd.length() && this.isSpace ( cmd.charAt ( bIdx ) ) ) {
                    ++bIdx;
                }
                if ( bIdx >= cmd.length() ) {
                    break;
                }
                inside = !inside;
            } else {
                while ( bIdx < cmd.length() && cmd.charAt ( bIdx ) != '=' ) {
                    retBuf.append ( cmd.charAt ( bIdx ) );
                    ++bIdx;
                }
                retBuf.append ( '=' );
                inside = !inside;
                quotes = 0;
                boolean escaped = false;
                while ( bIdx < cmd.length() && quotes != 2 ) {
                    final char c = cmd.charAt ( bIdx );
                    if ( c == '\\' && !escaped ) {
                        escaped = true;
                    } else {
                        if ( c == '\"' && !escaped ) {
                            ++quotes;
                        }
                        escaped = false;
                    }
                    ++bIdx;
                }
            }
        }
        final StringTokenizer str = new StringTokenizer ( retBuf.toString(), "=" );
        final String[] retString = new String[str.countTokens()];
        while ( str.hasMoreTokens() ) {
            retString[i++] = str.nextToken().trim();
        }
        return retString;
    }
    protected String[] parseParamValues ( final StringBuilder cmd, final int start, final int count ) {
        int valIndex = 0;
        boolean inside = false;
        final String[] vals = new String[count];
        final StringBuilder sb = new StringBuilder();
        char endQuote = '\0';
        for ( int bIdx = start; bIdx < cmd.length(); ++bIdx ) {
            if ( !inside ) {
                while ( bIdx < cmd.length() && !this.isQuote ( cmd.charAt ( bIdx ) ) ) {
                    ++bIdx;
                }
                if ( bIdx >= cmd.length() ) {
                    break;
                }
                inside = !inside;
                endQuote = cmd.charAt ( bIdx );
            } else {
                boolean escaped = false;
                while ( bIdx < cmd.length() ) {
                    final char c = cmd.charAt ( bIdx );
                    if ( c == '\\' && !escaped ) {
                        escaped = true;
                    } else {
                        if ( c == endQuote && !escaped ) {
                            break;
                        }
                        if ( c == '$' && escaped ) {
                            sb.append ( '\\' );
                        }
                        escaped = false;
                        sb.append ( c );
                    }
                    ++bIdx;
                }
                if ( bIdx == cmd.length() ) {
                    return null;
                }
                vals[valIndex++] = sb.toString();
                sb.delete ( 0, sb.length() );
                inside = !inside;
            }
        }
        return vals;
    }
    private String parseCmd ( final StringBuilder cmd ) {
        int firstLetter = -1;
        int lastLetter = -1;
        for ( int i = 0; i < cmd.length(); ++i ) {
            final char c = cmd.charAt ( i );
            if ( Character.isLetter ( c ) ) {
                if ( firstLetter == -1 ) {
                    firstLetter = i;
                }
                lastLetter = i;
            } else {
                if ( !this.isSpace ( c ) ) {
                    break;
                }
                if ( lastLetter > -1 ) {
                    break;
                }
            }
        }
        if ( firstLetter == -1 ) {
            return "";
        }
        return cmd.substring ( firstLetter, lastLetter + 1 );
    }
    protected boolean charCmp ( final String buf, final int index, final String command ) {
        return buf.regionMatches ( index, command, 0, command.length() );
    }
    protected boolean isSpace ( final char c ) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }
    protected boolean isQuote ( final char c ) {
        return c == '\'' || c == '\"' || c == '`';
    }
}
