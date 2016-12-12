package org.jfree.data.jdbc;
import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import org.jfree.data.general.DefaultPieDataset;
public class JDBCPieDataset extends DefaultPieDataset {
    static final long serialVersionUID = -8753216855496746108L;
    private transient Connection connection;
    public JDBCPieDataset ( final String url, final String driverName, final String user, final String password ) throws SQLException, ClassNotFoundException {
        Class.forName ( driverName );
        this.connection = DriverManager.getConnection ( url, user, password );
    }
    public JDBCPieDataset ( final Connection con ) {
        if ( con == null ) {
            throw new NullPointerException ( "A connection must be supplied." );
        }
        this.connection = con;
    }
    public JDBCPieDataset ( final Connection con, final String query ) throws SQLException {
        this ( con );
        this.executeQuery ( query );
    }
    public void executeQuery ( final String query ) throws SQLException {
        this.executeQuery ( this.connection, query );
    }
    public void executeQuery ( final Connection con, final String query ) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery ( query );
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();
            if ( columnCount != 2 ) {
                throw new SQLException ( "Invalid sql generated.  PieDataSet requires 2 columns only" );
            }
            final int columnType = metaData.getColumnType ( 2 );
            while ( resultSet.next() ) {
                final Comparable key = resultSet.getString ( 1 );
                switch ( columnType ) {
                case -5:
                case 2:
                case 3:
                case 4:
                case 6:
                case 7:
                case 8: {
                    final double value = resultSet.getDouble ( 2 );
                    this.setValue ( key, value );
                    continue;
                }
                case 91:
                case 92:
                case 93: {
                    final Timestamp date = resultSet.getTimestamp ( 2 );
                    final double value = date.getTime();
                    this.setValue ( key, value );
                    continue;
                }
                default: {
                    System.err.println ( "JDBCPieDataset - unknown data type" );
                    continue;
                }
                }
            }
            this.fireDatasetChanged();
        } finally {
            if ( resultSet != null ) {
                try {
                    resultSet.close();
                } catch ( Exception e ) {
                    System.err.println ( "JDBCPieDataset: swallowing exception." );
                }
            }
            if ( statement != null ) {
                try {
                    statement.close();
                } catch ( Exception e ) {
                    System.err.println ( "JDBCPieDataset: swallowing exception." );
                }
            }
        }
    }
    public void close() {
        try {
            this.connection.close();
        } catch ( Exception e ) {
            System.err.println ( "JdbcXYDataset: swallowing exception." );
        }
    }
}
