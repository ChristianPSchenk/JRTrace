/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;


public class Test19 {

	public static int stage = 0;
	
	public int test19() {
	    stage=1;
	    
	    if(stage!=1) return -1;
	    int j=test19sub("hallo");
	    
	    
		return stage+j;
	}

  /**
   * 
   */
  private int test19sub(String hallo) {
      throw new RuntimeException("Should have been replaced.");
      
    
  }

}
