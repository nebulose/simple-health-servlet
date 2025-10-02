package nebulose;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class FakeResponse implements HttpServletResponse {

  PrintWriter writer;
  StringWriter out;
  int status;

  @Override
  public void addCookie(Cookie cookie) {}

  @Override
  public boolean containsHeader(String name) {
    return false;
  }

  @Override
  public String encodeURL(String url) {
    return "";
  }

  @Override
  public String encodeRedirectURL(String url) {
    return "";
  }

  @Override
  public void sendError(int sc, String msg) {}

  @Override
  public void sendError(int sc) {}

  @Override
  public void sendRedirect(String location) {}

  @Override
  public void setDateHeader(String name, long date) {}

  @Override
  public void addDateHeader(String name, long date) {}

  @Override
  public void setHeader(String name, String value) {}

  @Override
  public void addHeader(String name, String value) {}

  @Override
  public void setIntHeader(String name, int value) {}

  @Override
  public void addIntHeader(String name, int value) {}

  @Override
  public void setStatus(int sc) {
    this.status = sc;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public String getHeader(String name) {
    return "";
  }

  @Override
  public Collection<String> getHeaders(String name) {
    return List.of();
  }

  @Override
  public Collection<String> getHeaderNames() {
    return List.of();
  }

  @Override
  public String getCharacterEncoding() {
    return "";
  }

  @Override
  public String getContentType() {
    return "";
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return null;
  }

  @Override
  public PrintWriter getWriter() {
    if (writer == null) {
      out = new StringWriter();
      writer = new PrintWriter(out);
    }
    return writer;
  }

  @Override
  public void setCharacterEncoding(String charset) {}

  @Override
  public void setContentLength(int len) {}

  @Override
  public void setContentLengthLong(long len) {}

  @Override
  public void setContentType(String type) {}

  @Override
  public void setBufferSize(int size) {}

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() {}

  @Override
  public void resetBuffer() {}

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {}

  @Override
  public void setLocale(Locale loc) {}

  @Override
  public Locale getLocale() {
    return null;
  }
}
