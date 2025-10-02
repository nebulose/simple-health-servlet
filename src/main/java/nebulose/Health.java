package nebulose;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/** Static functions for easy testing */
public class Health {

  static String checkHTTP(Config.Check check) throws Exception {
    var req = HttpRequest.newBuilder(new URI(check.url)).GET();
    if (check.host != null) {
      req.setHeader("Host", check.host);
    }
    try (var client =
        HttpClient.newBuilder().connectTimeout(Duration.of(check.timeout, SECONDS)).build()) {
      var resp = client.send(req.build(), HttpResponse.BodyHandlers.ofString());
      int code = resp.statusCode();
      if (code >= 200 && code < 300) {
        return null;
      } else {
        return check.url + " " + code;
      }
    }
  }

  static String checkSQL(Config.Check check) throws SQLException {
    if (check.query == null) {
      check.query = "SELECT 1";
    }
    try (Connection c = DriverManager.getConnection(check.url);
        PreparedStatement stmt = c.prepareStatement(check.query)) {

      stmt.setQueryTimeout(check.timeout);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return null;
        } else {
          return "No results for " + check.query;
        } // executeQuery try
      }
    } // Connection try
  }
}
