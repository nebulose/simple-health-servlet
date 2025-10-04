package nebulose;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CustomHostIT {

  @Test
  void testCustomHost() throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse(200, Headers.EMPTY, ""));
    server.start();

    var ok =
        Health.checkHTTP(
            new Config.Check("http://127.0.0.1:" + server.getPort() + "/one/two?q=1&q2=2+2", "domain.com", 60, 60));

    var req = server.takeRequest();

    assertNull(ok);
    assertEquals("GET /one/two?q=1&q2=2+2 HTTP/1.1", req.getRequestLine());
    assertEquals(1, req.getHeaders().toMultimap().get("Host").size());
    assertEquals("domain.com", req.getHeaders().get("Host"));

    server.close();
  }
}
