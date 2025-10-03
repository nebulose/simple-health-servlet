mvn package and drop it to tomcat or whatever

# Configuration

Configuration is done via a YAML file.

## Example

    defaultInterval: 60
    defaultTimeout: 30
    checks:
    - url: http://127.0.0.1:8080/webapp
      host: example.com
    - url: jdbc://localhost/tmp
      query: insert into tmp values (now())
      interval: 60
    - url: jdbc://localhost/tmp
      query: delete from tmp where now() > '2023-01-01'
      interval: 60 * 60 * 24
      timeout: 60 * 60 * 24

## Defaults

### defaultInterval

Check interval in seconds for checks without interval specified

Default is 60 seconds

## defaultTimeout

Connection timeout in seconds for checks without timeout specified

Default is 30 seconds

## Checks

### url

The URL to check. If starts with http:// or https:// it will be checked
as an HTTP check for a 200 status code. If starts with jdbc: it will be
checked as a JDBC connection and a query will be run. See `query` below

### query

If set, and `url` is HTTP the host header will be set to this value. Useful if the
app is behind a reverse proxy or load balancer. If `url` is jdbc is the sql query to run
against the connection. If missing in jdbc mode the query will be `SELECT 1`.

### interval

The interval in seconds to run the check. If missing the default interval
will be used.

### timeout

The timeout in seconds to run the check. If missing the default timeout
will be used.