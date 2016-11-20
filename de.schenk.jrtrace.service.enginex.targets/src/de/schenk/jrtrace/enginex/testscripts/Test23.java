/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;



public class Test23 {


	public static int value=0;


	public static int hitpoint=0;


	private int field=5;
	
	
	public int test23() {
	 
		int j=field;
		
		j=j*j;
		
		field=j;
		
	   return j;
	}


  

}
