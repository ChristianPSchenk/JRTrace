/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

public interface IGroovy {

	public Object evaluate(String expression, ClassLoader classLoader,
			Object... parameters);

	public Object evaluateFile(String projectRelativeFilename,
			ClassLoader classLoader, Object... parameters);
}
