package nebulose;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public class HealthServlet extends HttpServlet {

  private static final Logger log = LoggerFactory.getLogger(HealthServlet.class);

  private Config config;
  private Thread checkThread;
  private String[] errors;
  private boolean firstRun = true;

  @Override
  public void init() {
    String conf = getInitParameter("config");
    log.info("Health config:\n{}", conf);
    config = Config.fromString(conf);
    log.info("Health checks:\n{}", config);

    errors = new String[config.checks.size()];
    checkThread = new Thread(this::runChecksForever);
    checkThread.setName("Health Servlet forever checker");
    checkThread.setDaemon(true);
    checkThread.start();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String error = null;
    for (String e : errors) {
      if (e != null) {
        error = e;
        break;
      }
    }

    if (error == null) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print("OK");
    } else if (firstRun) {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print(error);
    } else {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setHeader("Content-Type", "text/plain");
      response.getWriter().print(error);
    }
    response.getWriter().print("\n");
  }

  void runChecksForever() {
    while (true) {
      for (int i = 0; i < config.checks.size(); i++) {
        var check = config.checks.get(i);

        if (check.nextCheck.isAfter(Instant.now())) continue;
        try {
          if (check.url.startsWith("http")) {
            errors[i] = Health.checkHTTP(check);
          } else if (check.url.startsWith("jdbc:")) {
            errors[i] = Health.checkSQL(check);
          }
        } catch (Exception e) {
          var msg = "ERROR " + check.url + ": " + e.getMessage();
          log.error(msg);
          errors[i] = msg;
        }
        if (errors[i] != null) {
          check.schedule(5);
        } else {
          check.schedule();
        }
      } // END FOR checks


      if (firstRun) {
        boolean anyError = Arrays.stream(errors).anyMatch(Objects::nonNull);
        if (!anyError) {
          log.info("First check run successfully. Future errors will be reported as 500");
          firstRun = false;
        }
      }
      long waitMillis = Health.nextRunMillis(config.checks);
      try {
          //noinspection BusyWait
          Thread.sleep(waitMillis);
      } catch (InterruptedException e) {
        log.warn("Interrupted. Bye.");
        return;
      }
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    var local = checkThread;
    if (local != null) {
      local.interrupt();
    }
  }
}
