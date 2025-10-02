package nebulose;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HealthServlet extends HttpServlet {

  Config health;

  @Override
  public void init() {
    //    getServletContext().log("Starting HealthServlet");
    readConfig();
  }

  private void readConfig() {
    // var f = new File("config.yaml");
    health = Config.inetCheck();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String error = null;
    for (var check : health.pendingChecks()) {
      try {
        if (check.url.startsWith("http")) {
          error = Health.checkHTTP(check);
        } else if (check.url.startsWith("jdbc:")) {
          error = Health.checkSQL(check);
        }
      } catch (Exception e) {
        var msg = "ERROR " + check + ": " + e.getMessage();
        log(msg);
        error = msg;
      }
      // If there is any error stop. In next GET it will get checked
      // Else schedule
      if (error != null) {
        break;
      } else {
        check.schedule();
      }
    }

    if (error == null) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print("OK");
    } else {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print(error);
    }
    response.getWriter().print("\n");
  }
}
