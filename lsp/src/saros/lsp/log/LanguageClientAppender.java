package saros.lsp.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

/** Appender that sends log events to the connected language client. */
public class LanguageClientAppender extends AppenderSkeleton {

  private final LanguageClient client;

  public LanguageClientAppender(LanguageClient client) {

    this.client = client;
  }

  @Override
  public void close() {}

  @Override
  public boolean requiresLayout() {
    return false;
  }

  @Override
  protected void append(LoggingEvent event) {
    MessageParams mp = new MessageParams();
    mp.setMessage(event.getMessage().toString());
    mp.setType(MessageType.Info);

    switch (event.getLevel().toInt()) {
      case Level.FATAL_INT:
      case Level.ERROR_INT:
        mp.setType(MessageType.Error);
        break;
      case Level.INFO_INT:
        mp.setType(MessageType.Info);
        break;
      case Level.WARN_INT:
        mp.setType(MessageType.Warning);
        break;
      default:
        return;
    }

    client.logMessage(mp);
  }
}
