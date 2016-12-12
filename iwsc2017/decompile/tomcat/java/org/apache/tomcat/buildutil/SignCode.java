package org.apache.tomcat.buildutil;
import java.net.MalformedURLException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;
import org.apache.tomcat.util.codec.binary.Base64;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPPart;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPConnectionFactory;
import org.apache.tools.ant.DirectoryScanner;
import java.util.Iterator;
import java.io.IOException;
import javax.xml.soap.SOAPException;
import org.apache.tools.ant.BuildException;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.types.FileSet;
import java.util.List;
import javax.xml.soap.MessageFactory;
import java.net.URL;
import org.apache.tools.ant.Task;
public class SignCode extends Task {
    private static final URL SIGNING_SERVICE_URL;
    private static final String NS = "cod";
    private static final MessageFactory SOAP_MSG_FACTORY;
    private final List<FileSet> filesets;
    private String userName;
    private String password;
    private String partnerCode;
    private String applicationName;
    private String applicationVersion;
    private String signingService;
    public SignCode() {
        this.filesets = new ArrayList<FileSet>();
    }
    public void addFileset ( final FileSet fileset ) {
        this.filesets.add ( fileset );
    }
    public void setUserName ( final String userName ) {
        this.userName = userName;
    }
    public void setPassword ( final String password ) {
        this.password = password;
    }
    public void setPartnerCode ( final String partnerCode ) {
        this.partnerCode = partnerCode;
    }
    public void setApplicationName ( final String applicationName ) {
        this.applicationName = applicationName;
    }
    public void setApplicationVersion ( final String applicationVersion ) {
        this.applicationVersion = applicationVersion;
    }
    public void setSigningService ( final String signingService ) {
        this.signingService = signingService;
    }
    public void execute() throws BuildException {
        final List<File> filesToSign = new ArrayList<File>();
        for ( final FileSet fileset : this.filesets ) {
            final DirectoryScanner ds = fileset.getDirectoryScanner ( this.getProject() );
            final File basedir = ds.getBasedir();
            final String[] files = ds.getIncludedFiles();
            if ( files.length > 0 ) {
                for ( int i = 0; i < files.length; ++i ) {
                    final File file = new File ( basedir, files[i] );
                    filesToSign.add ( file );
                }
            }
        }
        try {
            final String signingSetID = this.makeSigningRequest ( filesToSign );
            this.downloadSignedFiles ( filesToSign, signingSetID );
        } catch ( SOAPException | IOException e ) {
            throw new BuildException ( ( Throwable ) e );
        }
    }
    private String makeSigningRequest ( final List<File> filesToSign ) throws SOAPException, IOException {
        this.log ( "Constructing the code signing request" );
        final SOAPMessage message = SignCode.SOAP_MSG_FACTORY.createMessage();
        final SOAPBody body = populateEnvelope ( message, "cod" );
        final SOAPElement requestSigning = body.addChildElement ( "requestSigning", "cod" );
        final SOAPElement requestSigningRequest = requestSigning.addChildElement ( "requestSigningRequest", "cod" );
        addCredentials ( requestSigningRequest, this.userName, this.password, this.partnerCode );
        final SOAPElement applicationName = requestSigningRequest.addChildElement ( "applicationName", "cod" );
        applicationName.addTextNode ( this.applicationName );
        final SOAPElement applicationVersion = requestSigningRequest.addChildElement ( "applicationVersion", "cod" );
        applicationVersion.addTextNode ( this.applicationVersion );
        final SOAPElement signingServiceName = requestSigningRequest.addChildElement ( "signingServiceName", "cod" );
        signingServiceName.addTextNode ( this.signingService );
        final List<String> fileNames = getFileNames ( filesToSign );
        final SOAPElement commaDelimitedFileNames = requestSigningRequest.addChildElement ( "commaDelimitedFileNames", "cod" );
        commaDelimitedFileNames.addTextNode ( this.listToString ( fileNames ) );
        final SOAPElement application = requestSigningRequest.addChildElement ( "application", "cod" );
        application.addTextNode ( getApplicationString ( fileNames, filesToSign ) );
        final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        final SOAPConnection connection = soapConnectionFactory.createConnection();
        this.log ( "Sending siging request to server and waiting for response" );
        final SOAPMessage response = connection.call ( message, SignCode.SIGNING_SERVICE_URL );
        this.log ( "Processing response" );
        final SOAPElement responseBody = response.getSOAPBody();
        final NodeList bodyNodes = responseBody.getChildNodes();
        final NodeList requestSigningResponseNodes = bodyNodes.item ( 0 ).getChildNodes();
        final NodeList returnNodes = requestSigningResponseNodes.item ( 0 ).getChildNodes();
        String signingSetID = null;
        String signingSetStatus = null;
        for ( int i = 0; i < returnNodes.getLength(); ++i ) {
            final Node returnNode = returnNodes.item ( i );
            if ( returnNode.getLocalName().equals ( "signingSetID" ) ) {
                signingSetID = returnNode.getTextContent();
            } else if ( returnNode.getLocalName().equals ( "signingSetStatus" ) ) {
                signingSetStatus = returnNode.getTextContent();
            }
        }
        if ( ( !this.signingService.contains ( "TEST" ) && !"SIGNED".equals ( signingSetStatus ) ) || ( this.signingService.contains ( "TEST" ) && !"INITIALIZED".equals ( signingSetStatus ) ) ) {
            throw new BuildException ( "Signing failed. Status was: " + signingSetStatus );
        }
        return signingSetID;
    }
    private String listToString ( final List<String> list ) {
        final StringBuilder sb = new StringBuilder ( list.size() * 6 );
        boolean doneFirst = false;
        for ( final String s : list ) {
            if ( doneFirst ) {
                sb.append ( ',' );
            } else {
                doneFirst = true;
            }
            sb.append ( s );
        }
        return sb.toString();
    }
    private void downloadSignedFiles ( final List<File> filesToSign, final String id ) throws SOAPException, IOException {
        this.log ( "Downloading signed files. The signing set ID is: " + id );
        final SOAPMessage message = SignCode.SOAP_MSG_FACTORY.createMessage();
        final SOAPBody body = populateEnvelope ( message, "cod" );
        final SOAPElement getSigningSetDetails = body.addChildElement ( "getSigningSetDetails", "cod" );
        final SOAPElement getSigningSetDetailsRequest = getSigningSetDetails.addChildElement ( "getSigningSetDetailsRequest", "cod" );
        addCredentials ( getSigningSetDetailsRequest, this.userName, this.password, this.partnerCode );
        final SOAPElement signingSetID = getSigningSetDetailsRequest.addChildElement ( "signingSetID", "cod" );
        signingSetID.addTextNode ( id );
        final SOAPElement returnApplication = getSigningSetDetailsRequest.addChildElement ( "returnApplication", "cod" );
        returnApplication.addTextNode ( "true" );
        final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        final SOAPConnection connection = soapConnectionFactory.createConnection();
        this.log ( "Requesting signed files from server and waiting for response" );
        final SOAPMessage response = connection.call ( message, SignCode.SIGNING_SERVICE_URL );
        this.log ( "Processing response" );
        final SOAPElement responseBody = response.getSOAPBody();
        final NodeList bodyNodes = responseBody.getChildNodes();
        final NodeList getSigningSetDetailsResponseNodes = bodyNodes.item ( 0 ).getChildNodes();
        final NodeList returnNodes = getSigningSetDetailsResponseNodes.item ( 0 ).getChildNodes();
        String result = null;
        String data = null;
        for ( int i = 0; i < returnNodes.getLength(); ++i ) {
            final Node returnNode = returnNodes.item ( i );
            if ( returnNode.getLocalName().equals ( "result" ) ) {
                result = returnNode.getChildNodes().item ( 0 ).getTextContent();
            } else if ( returnNode.getLocalName().equals ( "signingSet" ) ) {
                data = returnNode.getChildNodes().item ( 1 ).getTextContent();
            }
        }
        if ( !"0".equals ( result ) ) {
            throw new BuildException ( "Download failed. Result code was: " + result );
        }
        extractFilesFromApplicationString ( data, filesToSign );
    }
    private static SOAPBody populateEnvelope ( final SOAPMessage message, final String namespace ) throws SOAPException {
        final SOAPPart soapPart = message.getSOAPPart();
        final SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration ( "soapenv", "http://schemas.xmlsoap.org/soap/envelope/" );
        envelope.addNamespaceDeclaration ( namespace, "http://api.ws.symantec.com/webtrust/codesigningservice" );
        return envelope.getBody();
    }
    private static void addCredentials ( final SOAPElement requestSigningRequest, final String user, final String pwd, final String code ) throws SOAPException {
        final SOAPElement authToken = requestSigningRequest.addChildElement ( "authToken", "cod" );
        final SOAPElement userName = authToken.addChildElement ( "userName", "cod" );
        userName.addTextNode ( user );
        final SOAPElement password = authToken.addChildElement ( "password", "cod" );
        password.addTextNode ( pwd );
        final SOAPElement partnerCode = authToken.addChildElement ( "partnerCode", "cod" );
        partnerCode.addTextNode ( code );
    }
    private static List<String> getFileNames ( final List<File> filesToSign ) {
        final List<String> result = new ArrayList<String> ( filesToSign.size() );
        for ( int i = 0; i < filesToSign.size(); ++i ) {
            final File f = filesToSign.get ( i );
            final String fileName = f.getName();
            final int extIndex = fileName.lastIndexOf ( 46 );
            String newName;
            if ( extIndex < 0 ) {
                newName = Integer.toString ( i );
            } else {
                newName = Integer.toString ( i ) + fileName.substring ( extIndex );
            }
            result.add ( newName );
        }
        return result;
    }
    private static String getApplicationString ( final List<String> fileNames, final List<File> files ) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ( 16777216 );
        try ( final ZipOutputStream zos = new ZipOutputStream ( baos ) ) {
            final byte[] buf = new byte[32768];
            for ( int i = 0; i < files.size(); ++i ) {
                try ( final FileInputStream fis = new FileInputStream ( files.get ( i ) ) ) {
                    final ZipEntry zipEntry = new ZipEntry ( fileNames.get ( i ) );
                    zos.putNextEntry ( zipEntry );
                    int numRead;
                    while ( ( numRead = fis.read ( buf ) ) >= 0 ) {
                        zos.write ( buf, 0, numRead );
                    }
                }
            }
        }
        return Base64.encodeBase64String ( baos.toByteArray() );
    }
    private static void extractFilesFromApplicationString ( final String data, final List<File> files ) throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream ( Base64.decodeBase64 ( data ) );
        try ( final ZipInputStream zis = new ZipInputStream ( bais ) ) {
            final byte[] buf = new byte[32768];
            for ( int i = 0; i < files.size(); ++i ) {
                try ( final FileOutputStream fos = new FileOutputStream ( files.get ( i ) ) ) {
                    zis.getNextEntry();
                    int numRead;
                    while ( ( numRead = zis.read ( buf ) ) >= 0 ) {
                        fos.write ( buf, 0, numRead );
                    }
                }
            }
        }
    }
    static {
        try {
            SIGNING_SERVICE_URL = new URL ( "https://api.ws.symantec.com/webtrust/SigningService" );
        } catch ( MalformedURLException e ) {
            throw new IllegalArgumentException ( e );
        }
        try {
            SOAP_MSG_FACTORY = MessageFactory.newInstance ( "SOAP 1.1 Protocol" );
        } catch ( SOAPException e2 ) {
            throw new IllegalArgumentException ( e2 );
        }
    }
}
