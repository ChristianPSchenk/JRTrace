/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.jar.JarFile;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.enginex.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.internal.JRTraceMXBeanImpl;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;

public class AgentMain {

  /*
   * The message codes the Agent sends
   */

  public static final String AGENT_READY = "READY";

  public static AgentMain theAgent = null;

  private static Instrumentation instrumentation;

  private JRTraceMXBeanImpl jrtraceBean;

  private ObjectName mxbeanName;

  public AgentMain() {

  }

  /**
   * check if the agent is still active (not null), if yes: stop it. start the new agent.
   * 
   * @param args
   * @param inst
   */
  public static void launch(int port, Instrumentation inst) {
    JRLog.debug(String.format("JRTrace Agent launched port:%d", port));
    HelperLib.setInstrumentation(inst);
    AgentMain.instrumentation = inst;
    if (theAgent != null) {
      theAgent.stop(false);
      theAgent = null;
    }
    if (theAgent == null) {
      theAgent = new AgentMain();
      theAgent.start(port);

    }

  }

  private MBeanServer mbs = null;
  JMXConnectorServer cs = null;
  private Registry mxbeanRegistry = null;

  private void stopMXBeanServer() {
    synchronized (AgentMain.class) {

      mxbeanName = NotificationUtil.getJRTraceObjectName();

      if (mbs.isRegistered(mxbeanName)) {
        try {
          mbs.unregisterMBean(mxbeanName);
        
        }
        catch (MBeanRegistrationException | InstanceNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      if (cs != null) {
        try {
          cs.stop();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        finally {
          cs = null;
        }
      }
      if (mxbeanRegistry != null) {

        try {
          UnicastRemoteObject.unexportObject(mxbeanRegistry, true);
        }
        catch (NoSuchObjectException e) {
          e.printStackTrace();
        }
        finally {
          mxbeanRegistry = null;
        }

      }


    }

  }

  private void startMXBeanServer(int port) {

    synchronized (AgentMain.class) {

      HashMap<String, String> environment = new HashMap<String, String>();
      environment.put("jmx.remote.x.daemon", "true");
      environment.put("com.sun.management.jmxremote.port", String.format("%d", port));
      environment.put("com.sun.management.jmxremote.authenticate", "false");
      environment.put("com.sun.management.jmxremote.ssl", "false");

      try {

        if (mxbeanRegistry == null) {

          try {
            mxbeanRegistry = LocateRegistry.getRegistry(port);

            // try to connect. In case of problem: createregistry.
            String[] list = mxbeanRegistry.list();
          }
          catch (RemoteException e) {

            mxbeanRegistry = LocateRegistry.createRegistry(port);
          }

        }


        mbs = ManagementFactory.getPlatformMBeanServer();

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");

        cs = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mbs);


        cs.start();

       
        
        
        mxbeanName = NotificationUtil.getJRTraceObjectName();
        
        jrtraceBean = new JRTraceMXBeanImpl(this);
        if (!mbs.isRegistered(mxbeanName)) {
          mbs.registerMBean(jrtraceBean, mxbeanName);
        }
      }
      catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
        throw new RuntimeException(e);
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  private PrintStream stdout;
  private PrintStream stderr;

  private EngineXClassFileTransformer enginextransformer;

  private boolean stdout_isredirected = false;

  private void start(int port) {

    startMXBeanServer(port);


  }

  public void redirectStandardOut(boolean enable) {
    synchronized (AgentMain.class) {


      if (enable && !stdout_isredirected) {
        stdout_isredirected = true;
        stdout = System.out;
        stderr = System.err;
        System.setOut(new PrintStream(new RedirectingOutputStream(jrtraceBean, System.out)));
        System.setErr(new PrintStream(new RedirectingOutputStream(jrtraceBean, System.err,
            NotificationConstants.NOTIFY_STDERR)));
        NotificationUtil.setNotificationSender(jrtraceBean);
      }
      else {
        if (enable == false && stdout_isredirected) {
          stdout_isredirected = false;
          System.setOut(stdout);
          System.setErr(stderr);
          NotificationUtil.setNotificationSender(null);
        }
      }
    }
  }

  public void connect() {
    synchronized (AgentMain.class) {
      if (enginextransformer != null)
        return;

      enginextransformer = new EngineXClassFileTransformer();
      instrumentation.addTransformer(enginextransformer, true);

      redirectStandardOut(true);

      JRLog.debug("AgentMain.connect() now connected");
    }
  }

  /**
   * disconnect the agent from the current connection
   */
  public void disconnect() {
    stop(true);
  }

  /**
   * shut down the agent
   */
  public void stop() {
    stop(false);
  }

  /**
   * stop all agent threads/activities/redirections
   * 
   * @param disconnect true: only disconnect, false: disable the command listener as well , the agent in this case
   *          effectively shut down
   */
  public void stop(boolean disconnect) {
    synchronized (AgentMain.class) {
      JRLog.debug(String.format("AgentMain.stop(disconnect:%b)", disconnect));
      EngineXHelper.clearEngineX();

      redirectStandardOut(false);

      if (enginextransformer != null)
        instrumentation.removeTransformer(enginextransformer);
      enginextransformer = null;

    

      theAgent = null;

      if (!disconnect) {
//        Thread stopServerThread = new Thread() {
//
//          @Override
//          public void run() {
//            stopMXBeanServer();
//          };
//        };
        stopMXBeanServer();
        
        //stopServerThread.start();


      }
    }
  }

  public void appendToBootstrapClassLoaderSearch(JarFile jarFile) {
    instrumentation.appendToBootstrapClassLoaderSearch(jarFile);

  }

  public static AgentMain getAgentInstance() {
    return theAgent;
  }
}
