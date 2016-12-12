package org.apache.catalina.ssi;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.catalina.util.RequestUtil;
import java.util.TimeZone;
import java.util.Date;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Locale;
import org.apache.catalina.util.Strftime;
import org.apache.catalina.util.URLEncoder;
public class SSIMediator {
    protected static final String DEFAULT_CONFIG_ERR_MSG = "[an error occurred while processing this directive]";
    protected static final String DEFAULT_CONFIG_TIME_FMT = "%A, %d-%b-%Y %T %Z";
    protected static final String DEFAULT_CONFIG_SIZE_FMT = "abbrev";
    protected static final URLEncoder urlEncoder;
    protected String configErrMsg;
    protected String configTimeFmt;
    protected String configSizeFmt;
    protected final String className;
    protected final SSIExternalResolver ssiExternalResolver;
    protected final long lastModifiedDate;
    protected Strftime strftime;
    protected final SSIConditionalState conditionalState;
    public SSIMediator ( final SSIExternalResolver ssiExternalResolver, final long lastModifiedDate ) {
        this.configErrMsg = "[an error occurred while processing this directive]";
        this.configTimeFmt = "%A, %d-%b-%Y %T %Z";
        this.configSizeFmt = "abbrev";
        this.className = this.getClass().getName();
        this.conditionalState = new SSIConditionalState();
        this.ssiExternalResolver = ssiExternalResolver;
        this.lastModifiedDate = lastModifiedDate;
        this.setConfigTimeFmt ( "%A, %d-%b-%Y %T %Z", true );
    }
    public void setConfigErrMsg ( final String configErrMsg ) {
        this.configErrMsg = configErrMsg;
    }
    public void setConfigTimeFmt ( final String configTimeFmt ) {
        this.setConfigTimeFmt ( configTimeFmt, false );
    }
    public void setConfigTimeFmt ( final String configTimeFmt, final boolean fromConstructor ) {
        this.configTimeFmt = configTimeFmt;
        this.strftime = new Strftime ( configTimeFmt, Locale.US );
        this.setDateVariables ( fromConstructor );
    }
    public void setConfigSizeFmt ( final String configSizeFmt ) {
        this.configSizeFmt = configSizeFmt;
    }
    public String getConfigErrMsg() {
        return this.configErrMsg;
    }
    public String getConfigTimeFmt() {
        return this.configTimeFmt;
    }
    public String getConfigSizeFmt() {
        return this.configSizeFmt;
    }
    public SSIConditionalState getConditionalState() {
        return this.conditionalState;
    }
    public Collection<String> getVariableNames() {
        final Set<String> variableNames = new HashSet<String>();
        variableNames.add ( "DATE_GMT" );
        variableNames.add ( "DATE_LOCAL" );
        variableNames.add ( "LAST_MODIFIED" );
        this.ssiExternalResolver.addVariableNames ( variableNames );
        final Iterator<String> iter = variableNames.iterator();
        while ( iter.hasNext() ) {
            final String name = iter.next();
            if ( this.isNameReserved ( name ) ) {
                iter.remove();
            }
        }
        return variableNames;
    }
    public long getFileSize ( final String path, final boolean virtual ) throws IOException {
        return this.ssiExternalResolver.getFileSize ( path, virtual );
    }
    public long getFileLastModified ( final String path, final boolean virtual ) throws IOException {
        return this.ssiExternalResolver.getFileLastModified ( path, virtual );
    }
    public String getFileText ( final String path, final boolean virtual ) throws IOException {
        return this.ssiExternalResolver.getFileText ( path, virtual );
    }
    protected boolean isNameReserved ( final String name ) {
        return name.startsWith ( this.className + "." );
    }
    public String getVariableValue ( final String variableName ) {
        return this.getVariableValue ( variableName, "none" );
    }
    public void setVariableValue ( final String variableName, final String variableValue ) {
        if ( !this.isNameReserved ( variableName ) ) {
            this.ssiExternalResolver.setVariableValue ( variableName, variableValue );
        }
    }
    public String getVariableValue ( String variableName, final String encoding ) {
        final String lowerCaseVariableName = variableName.toLowerCase ( Locale.ENGLISH );
        String variableValue = null;
        if ( !this.isNameReserved ( lowerCaseVariableName ) ) {
            variableValue = this.ssiExternalResolver.getVariableValue ( variableName );
            if ( variableValue == null ) {
                variableName = variableName.toUpperCase ( Locale.ENGLISH );
                variableValue = this.ssiExternalResolver.getVariableValue ( this.className + "." + variableName );
            }
            if ( variableValue != null ) {
                variableValue = this.encode ( variableValue, encoding );
            }
        }
        return variableValue;
    }
    public String substituteVariables ( String val ) {
        if ( val.indexOf ( 36 ) < 0 && val.indexOf ( 38 ) < 0 ) {
            return val;
        }
        val = val.replace ( "&lt;", "<" );
        val = val.replace ( "&gt;", ">" );
        val = val.replace ( "&quot;", "\"" );
        val = val.replace ( "&amp;", "&" );
        final StringBuilder sb = new StringBuilder ( val );
        for ( int charStart = sb.indexOf ( "&#" ); charStart > -1; charStart = sb.indexOf ( "&#" ) ) {
            final int charEnd = sb.indexOf ( ";", charStart );
            if ( charEnd <= -1 ) {
                break;
            }
            final char c = ( char ) Integer.parseInt ( sb.substring ( charStart + 2, charEnd ) );
            sb.delete ( charStart, charEnd + 1 );
            sb.insert ( charStart, c );
        }
        int i = 0;
        while ( i < sb.length() ) {
            while ( i < sb.length() ) {
                if ( sb.charAt ( i ) == '$' ) {
                    ++i;
                    break;
                }
                ++i;
            }
            if ( i == sb.length() ) {
                break;
            }
            if ( i > 1 && sb.charAt ( i - 2 ) == '\\' ) {
                sb.deleteCharAt ( i - 2 );
                --i;
            } else {
                int nameStart = i;
                final int start = i - 1;
                int end = -1;
                int nameEnd = -1;
                char endChar = ' ';
                if ( sb.charAt ( i ) == '{' ) {
                    ++nameStart;
                    endChar = '}';
                }
                while ( i < sb.length() && sb.charAt ( i ) != endChar ) {
                    ++i;
                }
                end = ( nameEnd = i );
                if ( endChar == '}' ) {
                    ++end;
                }
                final String varName = sb.substring ( nameStart, nameEnd );
                String value = this.getVariableValue ( varName );
                if ( value == null ) {
                    value = "";
                }
                sb.replace ( start, end, value );
                i = start + value.length();
            }
        }
        return sb.toString();
    }
    protected String formatDate ( final Date date, final TimeZone timeZone ) {
        String retVal;
        if ( timeZone != null ) {
            final TimeZone oldTimeZone = this.strftime.getTimeZone();
            this.strftime.setTimeZone ( timeZone );
            retVal = this.strftime.format ( date );
            this.strftime.setTimeZone ( oldTimeZone );
        } else {
            retVal = this.strftime.format ( date );
        }
        return retVal;
    }
    protected String encode ( final String value, final String encoding ) {
        String retVal = null;
        if ( encoding.equalsIgnoreCase ( "url" ) ) {
            retVal = SSIMediator.urlEncoder.encode ( value, "UTF-8" );
        } else if ( encoding.equalsIgnoreCase ( "none" ) ) {
            retVal = value;
        } else {
            if ( !encoding.equalsIgnoreCase ( "entity" ) ) {
                throw new IllegalArgumentException ( "Unknown encoding: " + encoding );
            }
            retVal = RequestUtil.filter ( value );
        }
        return retVal;
    }
    public void log ( final String message ) {
        this.ssiExternalResolver.log ( message, null );
    }
    public void log ( final String message, final Throwable throwable ) {
        this.ssiExternalResolver.log ( message, throwable );
    }
    protected void setDateVariables ( final boolean fromConstructor ) {
        final boolean alreadySet = this.ssiExternalResolver.getVariableValue ( this.className + ".alreadyset" ) != null;
        if ( !fromConstructor || !alreadySet ) {
            this.ssiExternalResolver.setVariableValue ( this.className + ".alreadyset", "true" );
            final Date date = new Date();
            final TimeZone timeZone = TimeZone.getTimeZone ( "GMT" );
            String retVal = this.formatDate ( date, timeZone );
            this.setVariableValue ( "DATE_GMT", null );
            this.ssiExternalResolver.setVariableValue ( this.className + ".DATE_GMT", retVal );
            retVal = this.formatDate ( date, null );
            this.setVariableValue ( "DATE_LOCAL", null );
            this.ssiExternalResolver.setVariableValue ( this.className + ".DATE_LOCAL", retVal );
            retVal = this.formatDate ( new Date ( this.lastModifiedDate ), null );
            this.setVariableValue ( "LAST_MODIFIED", null );
            this.ssiExternalResolver.setVariableValue ( this.className + ".LAST_MODIFIED", retVal );
        }
    }
    static {
        ( urlEncoder = new URLEncoder() ).addSafeCharacter ( ',' );
        SSIMediator.urlEncoder.addSafeCharacter ( ':' );
        SSIMediator.urlEncoder.addSafeCharacter ( '-' );
        SSIMediator.urlEncoder.addSafeCharacter ( '_' );
        SSIMediator.urlEncoder.addSafeCharacter ( '.' );
        SSIMediator.urlEncoder.addSafeCharacter ( '*' );
        SSIMediator.urlEncoder.addSafeCharacter ( '/' );
        SSIMediator.urlEncoder.addSafeCharacter ( '!' );
        SSIMediator.urlEncoder.addSafeCharacter ( '~' );
        SSIMediator.urlEncoder.addSafeCharacter ( '\'' );
        SSIMediator.urlEncoder.addSafeCharacter ( '(' );
        SSIMediator.urlEncoder.addSafeCharacter ( ')' );
    }
}
