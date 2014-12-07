/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import de.schenk.jrtrace.ui.Activator;

/**
 * a combo box that stores the last choices in the .settings.
 * 
 * @author Schenk
 *
 */
public class LastChoicesCombo extends Composite {

	private static final String LAST_CHOICES = "list_of_last_choices";
	private static final String LAST_ENTRY = "last_used_entry";
	private Combo combo;
	private Set<ModifyListener> listeners = new HashSet<ModifyListener>();

	private String prefix;

	/**
	 * 
	 * @param box
	 * @param style
	 * @param prefix
	 *            prefix to identify stored settings, same prefix = same stored
	 *            entries
	 */
	public LastChoicesCombo(Composite box, int style, String prefix) {
		super(box, SWT.FILL);

		this.prefix = prefix;
		{
			this.setLayout(new FillLayout());
			combo = new Combo(this, SWT.BORDER);

			combo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					fireModifiedEvent(e);
				}
			});

		}

	}

	public void restoreSettings() {
		IDialogSettings settings = Activator.getInstance().getDialogSettings();
		String paths = settings.get(prefix + LAST_CHOICES);
		if (paths != null) {
			String[] cl = paths.split("@@");

			combo.setItems(cl);

		}
		String lastValue = settings.get(prefix + LAST_ENTRY);

		if (lastValue != null) {
			combo.setText(lastValue);
		}
	}

	public String getText() {
		return combo.getText();
	}

	public void storeSettings() {
		IDialogSettings settings = Activator.getInstance().getDialogSettings();

		String[] items = combo.getItems();
		String current = combo.getText();
		StringBuilder result = new StringBuilder();
		if (current != null && !current.isEmpty()) {
			result.append(current);
		}
		for (int i = 0; i < items.length; i++) {
			if (!items[i].equals(current)) {
				if (result.length() != 0) {
					result.append("@@");
				}
				result.append(items[i]);

			}
			if (i == 20)
				break; // store max 20 last choices.
		}
		settings.put(prefix + LAST_CHOICES, result.toString());
		settings.put(prefix + LAST_ENTRY, current);

	}

	public void setText(String currentValue) {
		combo.setText(currentValue);

	}

	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	private void fireModifiedEvent(ModifyEvent e2) {
		for (ModifyListener l : listeners) {

			l.modifyText(e2);
		}
	}

	@Override
	public void dispose() {
		listeners.clear();
		super.dispose();
	}

}
