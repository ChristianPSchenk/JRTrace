/*
 * Copyright (c) Christian P. Schenk
 */
package de.schenk.jrtrace.helper;

/**
 * @author Christian P. Schenk
 * 
 *
 */
public class Injection {

	/**
	 * Possible injection types:
	 * 
	 *
	 */
	public enum InjectionType {
		/**
		 * Injection of a parameter of the instrumented method
		 */
		PARAMETER,

		/**
		 * Injection of a parameter of the invoked method
		 * */
		INVOKE_PARAMETER,
		/**
		 * Injection of a field of the instrumented class
		 */
		FIELD,
		/**
		 * Injection of the name and signature of the instrumented method
		 */
		METHODNAME,
		/**
		 * Injection of the thrown exception into the first parameter of the
		 * method
		 */
		EXCEPTION
	}

	/**
	 * @return the n
	 */
	public int getN() {
		return n;
	}

	/**
	 * @return the fieldname
	 */
	public String getFieldname() {
		return fieldname;
	}

	/**
	 * @return the type
	 */
	public InjectionType getType() {
		return type;
	}

	private int n;
	private String fieldname;
	private InjectionType type;

	/**
	 * @param n
	 * @param String
	 * @param type
	 */
	private Injection(int n, String fieldname, InjectionType type) {
		this.n = n;
		this.fieldname = fieldname;
		this.type = type;

	}

	public static Injection createParameterInjection(int n) {
		return new Injection(n, null, InjectionType.PARAMETER);
	}

	public static Injection createInvokeParameterInjection(int n) {
		return new Injection(n, null, InjectionType.INVOKE_PARAMETER);
	}

	public static Injection createFieldInjection(String name) {
		return new Injection(-1, name, InjectionType.FIELD);
	}

	public static Injection createMethodNameInjection() {
		return new Injection(-1, null, InjectionType.METHODNAME);
	}

	public static Injection createExceptionInjection() {
		return new Injection(-1, null, InjectionType.EXCEPTION);
	}
}
