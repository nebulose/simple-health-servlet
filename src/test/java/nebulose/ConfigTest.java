package nebulose;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {
  @Test
  void testConfigFromString() {
    String conf =
        """
       defaultTimeout: 123
       defaultInterval: 456
       checks:
          - url: http://localhost/pim
            ask: tal.com
          - url: jdbc://localhost/tmp
            ask: insert into tmp values (now())
            interval: 600
            timeout: 300
 """;

    System.out.printf(conf);
    var config = Config.fromString(conf, System.err::println);
    Assertions.assertEquals(123, config.defaultTimeout);
    Assertions.assertEquals(456, config.defaultInterval);

    var one = config.checks.getFirst();
    Assertions.assertEquals("http://localhost/pim", one.url);
    Assertions.assertEquals("tal.com", one.ask);
    Assertions.assertEquals(123, one.timeout);
    Assertions.assertEquals(456, one.interval);

    var two = config.checks.get(1);
    Assertions.assertEquals("jdbc://localhost/tmp", two.url);
    Assertions.assertNotNull(two.ask);
    Assertions.assertEquals(600, two.interval);
    Assertions.assertEquals(300, two.timeout);
    Assertions.assertEquals("insert into tmp values (now())", two.ask);
  }
}
