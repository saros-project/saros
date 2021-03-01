---
title: Logging
---

All components of Saros are using the log4j2 framework as logging backend.
Since Saros is an IDE plugin, the log dir depends on the IDEs configurations.

## Initializing log4j2

Therefore, initializing the logging in Saros comprises the steps:
1. The default log4j2 initializing is executed that uses the configuration file `log4j2.xml` (has to be included in the classpath).
2. Our custom initializing starts:
  * We determine the IDEs log directory and the required log level (e.g. `debug` if Saros is running the debug-mode).
  * We set the main arguments `logLevel` and `logDir`.
  * We force log4j to reinitialize with the log configuration `saros_log4j2.xml` that uses the defined arguments ( with the format `main:<argument>`).

See the initializing in the classes [`Saros`](https://github.com/saros-project/saros/blob/master/eclipse/src/saros/Saros.java) (in Eclipse)
or [`SarosComponent`](https://github.com/saros-project/saros/blob/master/intellij/src/saros/intellij/SarosComponent.java) (in IntelliJ)
for example implementations. Sometimes (as in the Saros Server) using the default initializing is enough.

### Settings a different log level during development

Sometimes you might have the need for a different log level during development, e.g. if you are interested in `trace` level logging.
To adjust the log level, you can modify the `root` log level in the `Loggers` section of the `saros_log4j2.xml`.

## Ongoing migration from log4j-1.2 -> log4j2

In most cases, migrating to log4j2 and only requires to change the
used logger (see [here](https://logging.apache.org/log4j/2.x/manual/migration.html) for more details):
`org.apache.log4j.Logger.getLogger()` -> `org.apache.logging.log4j.LogManager.getLogger()`

However, beside the required (minimal) migration step we should adopt the new functionality
of log4j2 that allow us to avoid log level checks.

### Avoid log level checks 

With log4j2 supporting format string evaluation and (with Java 8) closure evaluation, we
can migrate logging statements as:
```java
if (log.isTraceEnabled())
  log.trace(
  "id: "
  + namespaceId
  + " , namespace: "
  + namespace);
```
to a format string
```java
log.trace("id: {}, namespace: {}", namespaceId, namespace)
```
or also a format string using a closure:
```java
log.trace("id: {}, namespace: {}", () -> calculateId(), () -> namespace())
```

{% alert warning %}
The [Log4j2 API](https://logging.apache.org/log4j/2.x/log4j-api/apidocs/index.html) does not support to mix both variants.<br/>
Therefore, **the following is invalid**:

```java
log.trace("id: {}, namespace: {}", () -> calculateId(), namespace)
```
{% endalert %}

### Log4j-1.2-api

In order to migrate from log4j-1.2 to log4j2 step-by-step
we use the additional project dependency `log4j2-1.2-api` that
routes all log4j-1.2 calls to log4j2.
As soon as we migrated all these calls, we can remove this dependency and this migration
documentation. 
