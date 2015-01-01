/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;


public class Test18 {

	public static int stage = 0;
	
	public int test18() {
	    stage=1;
	    if(stage!=1) return -1;
	    test18sub();
	
	    
		return stage;
	}

  /**
   * 
   */
  private void test18sub() {
      if(stage==2) stage=3;
    
  }

}
