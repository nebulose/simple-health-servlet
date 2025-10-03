package nebulose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {

  private static final Logger log = LoggerFactory.getLogger(Config.class);

  public static class Check {
    String url;
    String ask;
    Instant nextCheck;
    int interval = 60;
    int timeout = 30;

    Check(String url) {
      this.url = url;
    }

    public Check(String url, String ask, int interval, int timeout) {
      this.url = url;
      this.ask = ask;
      this.interval = interval;
      this.timeout = timeout;
    }

    void schedule(int seconds) {
      nextCheck = Instant.now().plusSeconds(seconds);
    }

    void schedule() {
      schedule(interval);
    }
  }

  public int defaultInterval = 60;
  public int defaultTimeout = 30;
  public final List<Check> checks = new ArrayList<>();

  static Config fromString(String conf) {
    var yaml = new Yaml();
    Map<String, Object> map = yaml.load(conf);
    Config config = new Config();

    // DEFAULTS -----------
    if (map.containsKey("defaultTimeout")) {
      try {
        config.defaultTimeout = (int) map.get("defaultTimeout");
      } catch (ClassCastException e) {
        log.warn(
            "Error parsing{} defaultTimeout must be an integer. Using default {}",
            map.get("defaultTimeout"),
            config.defaultTimeout);
      }
    }
    if (map.containsKey("defaultInterval")) {
      try {
        config.defaultInterval = (int) map.get("defaultInterval");
      } catch (ClassCastException e) {
        log.warn(
            "Error parsing{} defaultInterval must be an integer. Using default {}",
            map.get("defaultInterval"),
            config.defaultInterval);
      }
    }

    // CHECKS -----------
    var configChecks = map.get("checks");
    if (configChecks instanceof List<?> checks)
      for (var configCheck : checks) {
        try {
          var check = (Map<?, ?>) configCheck;
          config.addCheck(
              (String) check.get("url"),
              (String) check.get("ask"),
              (Integer) check.get("interval"),
              (Integer) check.get("timeout"));
        } catch (ClassCastException e) {
          log.warn("Error parsing {}. Skipping. ", configCheck);
        }
      }
    if (config.checks.isEmpty()) {
      log.warn("No checks specified. Using default.");
      config.checks.add(inetCheck());
    }
    return config;
  }

  void addCheck(String url, String ask, Integer interval, Integer timeout) {
    var c = new Check(url);
    c.ask = ask;
    c.interval = interval == null ? defaultInterval : interval;
    c.timeout = timeout == null ? defaultTimeout : timeout;
    c.nextCheck = Instant.now().minusSeconds(1);
    checks.add(c);
  }

  static Check inetCheck() {
    return new Check("http://www.msftconnecttest.com/connecttest.txt", null, 15 * 60, 10);
  }
}
