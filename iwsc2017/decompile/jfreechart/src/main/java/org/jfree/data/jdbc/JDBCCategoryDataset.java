package org.jfree.data.jdbc;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import org.jfree.data.category.DefaultCategoryDataset;
public class JDBCCategoryDataset extends DefaultCategoryDataset {
    static final long serialVersionUID = -3080395327918844965L;
    private transient Connection connection;
    private boolean transpose;
    public JDBCCategoryDataset ( final String url, final String driverName, final String user, final String passwd ) throws ClassNotFoundException, SQLException {
        this.transpose = true;
        Class.forName ( driverName );
        this.connection = DriverManager.getConnection ( url, user, passwd );
    }
    public JDBCCategoryDataset ( final Connection connection ) {
        this.transpose = true;
        if ( connection == null ) {
            throw new NullPointerException ( "A connection must be supplied." );
        }
        this.connection = connection;
    }
    public JDBCCategoryDataset ( final Connection connection, final String query ) throws SQLException {
        this ( connection );
        this.executeQuery ( query );
    }
    public boolean getTranspose() {
        return this.transpose;
    }
    public void setTranspose ( final boolean transpose ) {
        this.transpose = transpose;
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
            if ( columnCount < 2 ) {
                throw new SQLException ( "JDBCCategoryDataset.executeQuery() : insufficient columns returned from the database." );
            }
            int i = this.getRowCount();
            while ( --i >= 0 ) {
                this.removeRow ( i );
            }
            while ( resultSet.next() ) {
                final Comparable rowKey = resultSet.getString ( 1 );
                for ( int column = 2; column <= columnCount; ++column ) {
                    final Comparable columnKey = metaData.getColumnName ( column );
                    final int columnType = metaData.getColumnType ( column );
                    switch ( columnType ) {
                    case -6:
                    case -5:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8: {
                        final Number value = ( Number ) resultSet.getObject ( column );
                        if ( this.transpose ) {
                            this.setValue ( value, columnKey, rowKey );
                            break;
                        }
                        this.setValue ( value, rowKey, columnKey );
                        break;
                    }
                    case 91:
                    case 92:
                    case 93: {
                        final Date date = ( Date ) resultSet.getObject ( column );
                        final Number value2 = new Long ( date.getTime() );
                        if ( this.transpose ) {
                            this.setValue ( value2, columnKey, rowKey );
                            break;
                        }
                        this.setValue ( value2, rowKey, columnKey );
                        break;
                    }
                    case -1:
                    case 1:
                    case 12: {
                        final String string = ( String ) resultSet.getObject ( column );
                        try {
                            final Number value2 = Double.valueOf ( string );
                            if ( this.transpose ) {
                                this.setValue ( value2, columnKey, rowKey );
                            } else {
                                this.setValue ( value2, rowKey, columnKey );
                            }
                        } catch ( NumberFormatException ex ) {}
                        break;
                    }
                    }
                }
            }
            this.fireDatasetChanged();
        } finally {
            if ( resultSet != null ) {
                try {
                    resultSet.close();
                } catch ( Exception ex2 ) {}
            }
            if ( statement != null ) {
                try {
                    statement.close();
                } catch ( Exception ex3 ) {}
            }
        }
    }
}
