package nebulose;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Config {
  public static class Check {
    String url;
    String host;
    String query;
    Instant nextCheck;
    int interval = 60;
    int timeout;

    Check schedule() {
      nextCheck = Instant.now().plusSeconds(interval);
      return this;
    }
  }

  public int defaultInterval = 60;
  public int defaultTimeout = 30;
  public List<Check> checks = new ArrayList<>();

  List<Check> pendingChecks() {
    Instant now = Instant.now();
    return checks.stream().filter(c -> c.nextCheck.isAfter(now)).toList();
  }

  Config addCheck(String url, String host, Integer interval, Integer timeout) {
    var c = new Check();
    c.url = url;
    c.host = host;
    c.interval = interval == null ? defaultInterval : interval;
    c.timeout = timeout == null ? defaultTimeout : timeout;
    c.nextCheck = Instant.now().minusSeconds(1);
    checks.add(c.schedule());
    return this;
  }

  static Config inetCheck() {
    var c = new Config();
    return c.addCheck("http://www.msftconnecttest.com/connecttest.txt", null, 15 * 60, 10);
  }
}
