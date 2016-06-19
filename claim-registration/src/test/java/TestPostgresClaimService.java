import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.andrewrea.claim.registration.domain.dtos.ClaimDto;

import java.sql.*;
import java.util.Properties;

/**
 * Created by vagrant on 6/17/16.
 */
@Ignore
public class TestPostgresClaimService {

    private Connection conn;

    @Before
    public void before() throws SQLException {
        String url = "jdbc:postgresql://0.0.0.0/claims";
        Properties props = new Properties();
        props.setProperty("user","claim");
        props.setProperty("password","docker");
        props.setProperty("ssl","false");
        conn = DriverManager.getConnection(url, props);
    }

    @After
    public void after() throws SQLException {
        conn.close();
    }

    @Test
    public void testItSavesTheClaimDto() throws SQLException {
        ClaimDto claim = new SystemUnderTest().getSampleClaim();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM mytable WHERE columnfoo = 500");
        while (rs.next())
        {
            System.out.print("Column 1 returned ");
            System.out.println(rs.getString(1));
        } rs.close();
        st.close();

        conn.close();
    }
}
