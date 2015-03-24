package nl.frobsie.dh15.camel;

import java.util.Map;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.dbcp.*;

import org.apache.log4j.Logger;

/**
 * TODO
 * Dit ding kan echt zo veel beter
 */
public class PostgresBean
{
    /** Het DataSource object */
    private BasicDataSource dataSource;

    /** Logger */
    final static Logger logger = Logger.getLogger(PostgresBean.class);

    /**
     * Constructor
     * @return void
     */
    public PostgresBean() {
        // TODO
        // gegevens richting properties file
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        dataSource.setUrl("jdbc:postgresql://192.168.3.60:5432/dh15");

        truncate();
        resetSequence();
    }

    /**
     * Voert een truncate cmd uit op
     * de feedentry tabel
     */
    protected void truncate() {
        doQuery("TRUNCATE TABLE feedentry");
    }

    /**
     * Reset de sequence zodat autoincrementen
     * weer vanaf 1 begint.
     */
    protected void resetSequence() {
        doQuery("ALTER SEQUENCE feedentry_id_seq RESTART");
    }

    /**
     * Handmatig query uitvoeren
     * @param String sql
     */
    protected void doQuery(String sql) {
        Connection con;
        PreparedStatement pstmt;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            // TODO
        }
    }

    /**
     * Geeft het DataSource object terug.
     * @return [description]
     */
    public DataSource getDataSource() {
       return dataSource;
    }

    /**
     * Zet het DataSource object.
     * 
     * @param BasicDataSource dataSource
     */
    public void setDataSource(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }
}
