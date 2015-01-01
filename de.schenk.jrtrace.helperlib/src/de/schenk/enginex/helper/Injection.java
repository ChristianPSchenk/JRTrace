/*
 * Copyright (c) Christian P. Schenk
 */
package de.schenk.enginex.helper;


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
       public enum InjectionType
       {
         PARAMETER, INVOKE_PARAMETER,FIELD
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
          this.n=n;
          this.fieldname=fieldname;
          this.type=type;
          
        }

        public static Injection createParameterInjection(int n)
        {
            return new Injection(n,null,InjectionType.PARAMETER);
        }
        public static Injection createInvokeParameterInjection(int n)
        {
            return new Injection(n,null,InjectionType.INVOKE_PARAMETER);
        }
        public static Injection createFieldInjection(String name)
        {
            return new Injection(-1,name,InjectionType.FIELD);
        }
}
