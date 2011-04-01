package de.fu_berlin.inf.dpp.concurrent.watchdog;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * This class is responsible for opening the SessionView if an inconsistency has
 * been detected.
 * 
 */
@Component(module = "util")
public class SessionViewOpener {
	private static final Logger log = Logger.getLogger(SessionViewOpener.class
			.getName());

	public SessionViewOpener(IsInconsistentObservable isInconsistentObservable,
			final SarosUI sarosUI) {
		isInconsistentObservable.add(new ValueChangeListener<Boolean>() {
			public void setValue(Boolean inconsistency) {
				if (!inconsistency) {
					return;
				}

				Utils.runSafeSWTSync(log, new Runnable() {
					public void run() {
						sarosUI.openSarosView();
					}
				});
			}
		});
	}
}
