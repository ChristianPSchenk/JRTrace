/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.markers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import de.schenk.objectweb.asm.Type;

public class JRTraceJavaSearch {

	SearchEngine engine = new SearchEngine();

	/**
	 * Searches for the declaration of the specified class.
	 *
	 * 
	 * @param className
	 *            the fully qualified classname
	 * @param monitor
	 *            the progress monitor
	 * 
	 * @return a SearchMatch with the location or null if the class wasn't
	 *         found.
	 */
	public SearchMatch searchClass(String className, IProgressMonitor monitor) {

		SearchPattern classSearchPattern = SearchPattern.createPattern(
				className, IJavaSearchConstants.CLASS,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

		final SearchMatch[] result = new SearchMatch[1];
		SearchRequestor requestor = new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match)
					throws CoreException {
				result[0] = match;

			}

		};

		try {
			engine.search(classSearchPattern,
					new SearchParticipant[] { SearchEngine
							.getDefaultSearchParticipant() }, SearchEngine
							.createWorkspaceScope(), requestor, monitor);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		return result[0];

	}

	public IMethod searchMethod(IType type, String methodName, String descriptor)

	{

		try {

			Type[] types = Type.getArgumentTypes(descriptor);
			IMethod[] methods = type.getMethods();

			for (IMethod m : methods) {

				String[] mtypes = m.getParameterTypes();

				if (m.getElementName().equals(methodName)) {

					if (mtypes.length != types.length)
						continue;

					boolean matches = true;
					for (int i = 0; i < mtypes.length; i++) {
						String msimple = Signature
								.getSignatureSimpleName(mtypes[i]);
						String className = types[i].getClassName();
						if (!className.contains(msimple)) {
							matches = false;
							break;
						}

					}
					if (!matches)
						continue;

					return m;
				}

			}

		} catch (JavaModelException e) {
			return null;
		}
		return null;
	}
}
