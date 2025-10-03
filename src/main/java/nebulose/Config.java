package nebulose;

import org.yaml.snakeyaml.Yaml;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Config {
  public static class Check {
    String url;
    String host;
    String query;
    Instant nextCheck;
    int interval = 60;
    int timeout = 30;

    Check() {}

    Check(String url) {
      this.url = url;
    }

    public Check(String url, String query, int interval, int timeout) {
      this.url = url;
      this.query = query;
      this.interval = interval;
      this.timeout = timeout;
    }

    void schedule() {
      nextCheck = Instant.now().plusSeconds(interval);
    }
  }

  public int defaultInterval = 60;
  public int defaultTimeout = 30;
  public final List<Check> checks = new ArrayList<>();

  static Config fromString(String conf, Consumer<String> logger) {
    var yaml = new Yaml();
    Map<String, Object> map = yaml.load(conf);
    Config config = new Config();

    // DEFAULTS -----------
    if (map.containsKey("defaultTimeout")) {
      try {
        config.defaultTimeout = (int) map.get("defaultTimeout");
      } catch (ClassCastException e) {
        logger.accept(
            "Error parsing"
                + map.get("defaultTimeout")
                + " defaultTimeout must be an integer. Using default "
                + config.defaultTimeout);
      }
    }
    if (map.containsKey("defaultInterval")) {
      try {
        config.defaultInterval = (int) map.get("defaultInterval");
      } catch (ClassCastException e) {
        logger.accept(
            "Error parsing"
                + map.get("defaultInterval")
                + " defaultInterval must be an integer. Using default "
                + config.defaultInterval);
      }
    }
    if (!map.containsKey("checks")) {
      logger.accept("No checks specified. Using default.");
      config.checks.add(inetCheck());
    }

    // CHECKS -----------
    var configChecks = map.get("checks");
    if (configChecks instanceof List<?> checks)
      for (var configCheck : checks) {
        try {
          var check = (Map<?, ?>) configCheck;
          config.addCheck(
              (String) check.get("url"),
              (String) check.get("host"),
              (Integer) check.get("interval"),
              (Integer) check.get("timeout"));
        } catch (ClassCastException e) {
          logger.accept("Error parsing" + configCheck + ". Skipping. ");
        }
      }
    return config;
  }

  List<Check> pendingChecks() {
    Instant now = Instant.now();
    return checks.stream().filter(c -> c.nextCheck.isAfter(now)).toList();
  }

  void addCheck(String url, String host, Integer interval, Integer timeout) {
    var c = new Check(url);
    c.host = host;
    c.interval = interval == null ? defaultInterval : interval;
    c.timeout = timeout == null ? defaultTimeout : timeout;
    c.nextCheck = Instant.now().minusSeconds(1);
    checks.add(c);
  }

  static Check inetCheck() {

    return new Check("http://www.msftconnecttest.com/connecttest.txt", null, 15 * 60, 10);
  }
}
