/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperagent;

import de.schenk.jrtrace.helperlib.GroovyUtil;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.HelperLibConstants;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

/**
 * Run a groovy script
 * 
 * @author Christian Schenk
 *
 */
public class RunGroovyListener implements IJRTraceClientListener {

	/**
	 * expected input: a comma seperated string with two entries: a) classname
	 * name of the class from which to use the classloader b) fully qualified OS
	 * path to the groovy script to run
	 * 
	 */
	@Override
	public void messageReceived(String clientSentence) {
		String[] args = clientSentence.split(",");
		if (args.length != 2)
			throw new RuntimeException(
					"RunGroovyListener: expects two arguments separated with comma");
		String referenceClassName = args[0];
		String pathToGroovy = args[1];
		GroovyUtil groovy = new GroovyUtil(
				System.getProperty(HelperLibConstants.DE_SCHENK_JRTRACE_GROOVYJAR),
				null);
		groovy.evaluateFile(pathToGroovy,
				HelperLib.getCachedClassLoader(referenceClassName));

	}
}
