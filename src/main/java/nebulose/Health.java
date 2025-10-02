package nebulose;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Health {
  public class Check {
    public String url;
    public String host, update;
    public int _interval = -1;
    public String query;
    Instant nextCheck;
    private int timeout;

    public int interval() {
      return _interval == -1 ? Health.this.interval : _interval;
    }

    Check schedule() {
      nextCheck = Instant.now().plusSeconds(interval());
      return this;
    }

    public int timeout() {
      return timeout == -1 ? Health.this.timeout : timeout;
    }
  }

  public int interval = 60;
  private int timeout = 30;
  private List<Check> checks = new ArrayList<>();

  Stream<Check> checks() {
    Instant now = Instant.now();
    return checks.stream().filter(c -> c.nextCheck.isAfter(now));
  }

  Health addCheck(String url, String host, Integer interval) {
    var c = new Check();
    c.url = url;
    c.host = host;
    if (interval != null) {
      c._interval = interval;
    }
    checks.add(c.schedule());
    return this;
  }

  Health inetCheck() {
    addCheck("http://www.msftconnecttest.com/connecttest.txt", null, 15 * 60);
    return this;
  }
}
