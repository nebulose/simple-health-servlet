package nebulose;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

public class HealthIT {

  @Test
  void testOkHttp() throws Exception {
    var config = Config.inetCheck();
    var ok = Health.checkHTTP(config.checks.getFirst());
    Assertions.assertNull(ok);
  }

  @Test
  void testHttpFail() {
    var config = new Config();
    config.addCheck("http://127.0.0.1:34567/fail", null, null, null);
    Assertions.assertThrows(
        ConnectException.class, () -> Health.checkHTTP(config.checks.getFirst()));
  }

  @Test
  void testMySql() throws Exception {
    var config = new Config();
    config.addCheck("jdbc:mysql://root:example@localhost:3307/mysql", null, null, null);
    var ok = Health.checkSQL(config.checks.getFirst());
    Assertions.assertNull(ok);
  }

  @Test
  void testMySqlFail() {
    var config = new Config();
    config.addCheck("jdbc:mysql://root:bad@localhost:3307/mysql", null, null, null);
    Assertions.assertThrows(SQLException.class, () -> Health.checkSQL(config.checks.getFirst()));
  }
}
