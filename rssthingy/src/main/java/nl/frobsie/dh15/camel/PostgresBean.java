package nl.frobsie.dh15.camel;

import java.util.Map;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.dbcp.*;

/**
 * TODO
 * Dit ding kan echt zo veel beter
 */
public class PostgresBean
{
    private BasicDataSource dataSource;

    public PostgresBean() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        dataSource.setUrl("jdbc:postgresql://192.168.3.60:5432/dh15");

        truncate();
        resetSequence();
    }

    protected void truncate() {
        doQuery("TRUNCATE TABLE feedentry");
    }

    protected void resetSequence() {
        doQuery("ALTER SEQUENCE feedentry_id_seq RESTART");
    }

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

    public DataSource getDataSource() {
       return dataSource;
    }

    public void setDataSource(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }
}
