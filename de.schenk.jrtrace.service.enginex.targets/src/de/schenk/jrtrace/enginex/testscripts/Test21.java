/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import java.util.ArrayList;
import java.util.HashSet;


public class Test21 {





	
	public int test21() {
	 
	    ArrayList<String> x=new ArrayList<String>();
	    HashSet<String> y=new HashSet<String>();
	    x.add("a");
	    y.add("b");
	    
	    x.clear();
	    y.clear();
	    
	    
		return 10*x.size()+y.size();
	}

  

}
