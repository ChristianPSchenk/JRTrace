/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helper;

import java.util.HashMap;
import java.util.Map;

import de.schenk.objectweb.asm.Opcodes;

public class FieldList {

	public class FieldEntry {
		public FieldEntry(int access2, String signature2) {
			this.access = access2;
			this.signature = signature2;
		}

		private int access;
		private String signature;

		public boolean isStatic() {
			return ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC);
		}

		public String getDescriptor() {
			return signature;
		}
	}

	Map<String, FieldEntry> fields = new HashMap<String, FieldEntry>();

	public void put(String name, int access, String signature) {
		fields.put(name, new FieldEntry(access, signature));

	}

	public FieldEntry getFieldEntry(String injectionSource) {
		return fields.get(injectionSource);
	}
}
