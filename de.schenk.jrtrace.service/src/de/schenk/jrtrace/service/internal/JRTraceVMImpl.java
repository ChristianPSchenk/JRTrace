/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.io.IOException;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import de.schenk.jrtrace.service.ICancelable;
import de.schenk.jrtrace.service.JarLocator;

public class JRTraceVMImpl extends AbstractVM {

	VirtualMachine vm;
	private String thePID;

	//

	public JRTraceVMImpl(String pid) {
		thePID = pid;
	}

	/**
	 * 
	 * @return true if successful, excception can be retrieved via
	 *         getException() if not successful
	 */
	public boolean attach(ICancelable stopper) {
		boolean result = attachVM();
		if (!result)
			return result;

		int port = installAgent();
		if (port == -1)
			return false;

		return connectToAgent(port, stopper);
	}

	private boolean attachVM() {
		VirtualMachine theVM = null;
		try {
			theVM = VirtualMachine.attach(thePID);
		} catch (AttachNotSupportedException e) {
			lastException = e;
			return false;

		} catch (IOException e) {
			lastException = e;
			return false;
		}

		vm = theVM;
		return true;
	}

	private int installAgent() {

		int port = PortUtil.getFreePort();
		try {
			String agent = JarLocator.getJRTraceHelperAgent();
			String helperPath = JarLocator.getHelperLibJar();
			vm.loadAgent(agent,
					String.format("port=%d,bootjar=%s", port, helperPath));

		} catch (AgentInitializationException e) {
			lastException = e;
			return -1;
		} catch (AgentLoadException e) {
			lastException = e;
			return -1;
		} catch (IOException e) {
			lastException = e;
			return -1;
		}
		return port;
	}

	public boolean detach() {
		boolean result = true;

		result = stopConnection(false);
		if (!detachVM()) {
			result = false;
		}
		return result;

	}

	private boolean detachVM() {
		try {
			if (vm != null) {
				vm.detach();
			}
		} catch (IOException e) {
			lastException = e;
			return false;
		}
		return true;
	}

	@Override
	public String getPID() {
		return thePID;
	}

}
