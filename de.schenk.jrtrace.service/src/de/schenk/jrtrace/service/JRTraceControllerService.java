/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service;

import de.schenk.jrtrace.service.internal.JRTraceControllerImpl;

public class JRTraceControllerService {
	private  static JRTraceController controller=new JRTraceControllerImpl();
		public static JRTraceController getInstance()
		{
			return controller;
		}
}
