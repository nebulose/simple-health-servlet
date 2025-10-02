package nebulose;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.*;
import java.util.function.Consumer;

public class HealthServlet extends HttpServlet {

  Health health;
  private String lastError;

  @Override
  public void init() {
    getServletContext().log("Starting HealthServlet");
    readConfig();
  }

  private void readConfig() {
    // var f = new File("config.yaml");
    health = new Health();
    health.inetCheck().addCheck("https://google.com", null, -1);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Consumer<String> logger = s -> request.getServletContext().log(s);

    lastError = null;
    for (var check : health.checks().toList()) {
      try {
        if (check.url.startsWith("http")) {
          lastError = checkHTTP(check);
        } else if (check.url.startsWith("jdbc://")) {
          lastError = checkSQL(check);
        }
      } catch (Exception e) {
        var msg = "ERROR " + check + ": " + e.getMessage();
        log(msg);
        lastError = msg;
      }
      if (lastError != null) break;
    }
    if (lastError == null) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print("OK");
    } else {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print(lastError);
    }
    response.getWriter().print("\n");
  }

  private String checkHTTP(Health.Check check) throws URISyntaxException {
    var req = HttpRequest.newBuilder(new URI(check.url)).GET();
    if (check.host != null) {
      req.setHeader("Host", check.host);
    }
    var client = HttpClient.newHttpClient();
    return null;
  }

  private String checkSQL(Health.Check check) throws SQLException {
    if (check.query == null) {
      check.query = "SELECT 1";
    }
    try (Connection c = DriverManager.getConnection(check.url);
        PreparedStatement stmt = c.prepareStatement(check.query)) {

      stmt.setQueryTimeout(check.timeout());
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
