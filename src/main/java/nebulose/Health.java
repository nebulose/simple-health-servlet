package nebulose;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.bootstrap.RequesterBootstrap;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.Timeout;

import java.net.URI;
import java.sql.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Static functions for easy testing */
public class Health {

  static String checkHTTP(Config.Check check) throws Exception {

    int code;
    try (HttpRequester httpRequester =
        RequesterBootstrap.bootstrap()
            .setSocketConfig(
                SocketConfig.custom().setSoTimeout(check.timeout, TimeUnit.SECONDS).build())
            .create()) {

      URI uri = new URI(check.url);

      final HttpHost target = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
      final var request = ClassicRequestBuilder.get();
      if (uri.getRawQuery() == null || uri.getRawQuery().isEmpty()) {
        request.setPath(uri.getPath());
      } else {
        request.setPath(uri.getPath() + "?" + uri.getRawQuery());
      }
      if (check.ask != null) {
        request.addHeader("Host", check.ask);
      }

      final HttpCoreContext context = HttpCoreContext.create();
      try (ClassicHttpResponse response =
          httpRequester.execute(
              target, request.build(), Timeout.ofSeconds(check.timeout), context)) {
        code = response.getCode();
      }
    }

    if (code >= 200 && code < 300) {
      return null;
    } else {
      return check.url + " " + code;
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
