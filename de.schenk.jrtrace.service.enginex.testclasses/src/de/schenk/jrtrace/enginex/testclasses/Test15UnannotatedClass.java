package de.schenk.jrtrace.enginex.testclasses;


public class Test15UnannotatedClass {
	static public boolean doit() {
		if (Test15UnannotatedClass.class.getClassLoader().getParent() != null)
			throw new RuntimeException(
					"unannotated class should have been loaded to the bootclass");
		return true;
	}
}
