/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;


public class Test20 {


  private String id;

  public Test20()
  {
    this("not the invoked Object");
  }
  public Test20(String id)
  {
    this.id=id;
  }
	
	public int test20() {
	 
	    int j=new Test20("invokedObject").test20sub("hallo");
	    
	    
		return j;
	}

  /**
   * 
   */
  private Integer test20sub(String hallo) {
      return 10;
    
  }

  /**
   * @return
   */
  public String getId() {
  return id;
  }

}
