package nebulose;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class HealthIT {

  @Test
  void testOkRun() throws Exception {

    var res = new FakeResponse();

    var props = new Properties();
    props.load(Files.newInputStream(Path.of(".env")));
    var servlet =
        new HealthServlet() {
          @Override
          public String getInitParameter(String name) {
            return props.getProperty(name);
          }
        };
    servlet.init();
    assertNotNull(servlet.health);

    servlet.doGet(null, res);
    assertEquals("OK\n", res.out.toString());
  }

  @Test
  void testFailRun() throws Exception {

    var res = new FakeResponse();

    var health =
        new HealthServlet() {
          @Override
          public String getInitParameter(String name) {
            return "";
          }
        };
    health.init();
    assertNotNull(health.health);

    health.doGet(null, res);
    assertEquals(500, res.getStatus());
  }
}
