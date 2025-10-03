package nebulose;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Static functions for easy testing */
public class Health {

  static String checkHTTP(Config.Check check) throws Exception {
    var req = HttpRequest.newBuilder(new URI(check.url)).GET();
    if (check.ask != null) {
      req.setHeader("Host", check.ask);
    }
    try (var client =
        HttpClient.newBuilder().connectTimeout(Duration.of(check.timeout, SECONDS)).build()) {
      var resp = client.send(req.build(), HttpResponse.BodyHandlers.discarding());
      int code = resp.statusCode();
      if (code >= 200 && code < 300) {
        return null;
      } else {
        return check.url + " " + code;
      }
    }
  }

  static String checkSQL(Config.Check check) throws SQLException {
    if (check.ask == null) {
      check.ask = "SELECT 1";
    }
    try (Connection c = DriverManager.getConnection(check.url);
        PreparedStatement stmt = c.prepareStatement(check.ask)) {

      stmt.setQueryTimeout(check.timeout);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return null;
        } else {
          return "No results for " + check.ask;
        } // executeQuery try
      }
    } // Connection try
  }

  static long nextRunMillis(List<Config.Check> checks) {
    var nextRun =
        checks.stream()
            .map(c -> c.nextCheck)
            .min(Comparator.naturalOrder())
            .orElseGet(() -> Instant.now().plusSeconds(5));
    var waitMillis = nextRun.toEpochMilli() - Instant.now().toEpochMilli();
    waitMillis = Math.min(Math.max(waitMillis, 1000), 60 * 1000);
    return waitMillis;
  }
}
