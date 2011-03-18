package de.fu_berlin.inf.dpp.ui.sarosView;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

import de.fu_berlin.inf.dpp.annotations.Component;

@Component(module = "ui")
public class SessionViewTableViewer extends TableViewer {
    public SessionViewTableViewer(Table table) {
        super(table);
    }

}
