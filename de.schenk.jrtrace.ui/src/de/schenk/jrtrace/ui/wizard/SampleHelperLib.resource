package ###package###;

import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;

/**
 * 
 * Example that shows the invocation of all IResourceChangeListener
 * 
 * @author Christian Schenk 
 *
 */
@XClass (classes="org.eclipse.core.resources.IResourceChangeListener",derived=true)
public class ###classname### extends HelperLib
{

	public ###classname###() {		
		
	}


	@XMethod(names="resourceChanged")
	public void showHelp(@XThis Object o)
	{
		System.out.println("in listener: "
		+o.toString());
	}
}