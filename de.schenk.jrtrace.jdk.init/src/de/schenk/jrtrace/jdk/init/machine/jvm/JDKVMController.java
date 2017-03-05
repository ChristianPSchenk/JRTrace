package de.schenk.jrtrace.jdk.init.machine.jvm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import de.schenk.jrtrace.jdk.init.JDKInitActivator;
import de.schenk.jrtrace.jdk.init.machine.VirtualMachineException;
import de.schenk.jrtrace.jdk.init.utils.BundleFilesUtil;
import de.schenk.toolsjar.Activator;
import de.schenk.toolsjar.attach.app.JDKAttachApplication;

/**
 * A launcher to launch the JDKAttachApplication and execute either an attach or a list.
 * 
 * @author Christian P. Schenk
 *
 */
public class JDKVMController {

	private static JDKVMController invoker=new JDKVMController();
	
	public static JDKVMController getInstance() {
		return invoker;
	}
	
	private class InputStreamToStringReaderThread extends Thread
	{
		
		String resultString;
		private InputStream stream;
		Throwable failure=null;
		public InputStreamToStringReaderThread(InputStream stream) {
			this.stream=stream;
		}
		
		@Override
		public void run() {
			 ByteArrayOutputStream result=new ByteArrayOutputStream();
			int len=0;
			byte[] buffer=new byte[1024];
			do{	
			try {
				len=stream.read(buffer, 0,buffer.length);
				if(len>=0) result.write(buffer, 0, len);
			} catch (IOException e) {
				
				resultString="Exception while reading the stream:"+e.getMessage();
				failure=e;
				return;
			}
			} while(len!=-1);
			resultString=new String(result.toByteArray());
		}

		public String getResult() {
			return resultString;
		}

		public Throwable getFailure() {
			return failure;
		}
		
	}

	public String run(String... parameters ) {	
		String	javaExe=null;
		ArrayList<String> commandLine=new ArrayList<String>();
		if(JDKInitActivator.isJava9JDK()){
			javaExe=JDKInitActivator.getJDK9HomeDir()+File.separator+"bin"+File.separator+"java";	
			
			
			
			commandLine.add(javaExe);
			commandLine.add("-cp");
			commandLine.add(getJDKAttachApplicationClassPath());
			commandLine.add(JDKAttachApplication.class.getName());
		} else
		{
			
		javaExe=System.getProperty("java.home")+File.separator+"bin"+File.separator+"java";	
		String classPath=getJDKAttachApplicationClassPath()+System.getProperty("path.separator")+JDKInitActivator.getToolsJar().getAbsolutePath();
		
		
		commandLine.add(javaExe);
		commandLine.add("-cp");
		commandLine.add(classPath);
		commandLine.add(JDKAttachApplication.class.getName());
		}
		for(String parameter:parameters)commandLine.add(parameter);
		ProcessBuilder pb=new ProcessBuilder(commandLine);
		
		
		
		InputStreamToStringReaderThread stdOutThread;
		InputStreamToStringReaderThread stdErrThread;
		try {
			Process process=pb.start();
			 stdOutThread=new InputStreamToStringReaderThread(process.getInputStream());
			 stdErrThread=new InputStreamToStringReaderThread(process.getErrorStream());
			stdOutThread.start();
			stdErrThread.start();
		
			process.waitFor();
			stdOutThread.join();
			stdErrThread.join();
		
			if(stdOutThread.getFailure()!=null) throw new RuntimeException("Exception reading std out:",stdOutThread.getFailure());
			if(stdErrThread.getFailure()!=null) throw new RuntimeException("Exception reading std err:",stdErrThread.getFailure());
			
		} catch (IOException e) {
				throw new RuntimeException(e);
		} catch(InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		String error=stdErrThread.getResult();
		if(error!=null&&!error.isEmpty())
		{
			throw new VirtualMachineException(String.format("Error returned from process when starting with\n%s\nErrror:\n%s", commandLine.toString(),error));
		}
		return stdOutThread.getResult();
		
	}

	private String getJDKAttachApplicationClassPath() {

		String structure=JDKAttachApplication.class.getName().replace(".", File.separator)+".class";
		String relPath=structure;
		String appClassPath=BundleFilesUtil.getFile(Activator.getBundeId(), relPath);
		if(appClassPath==null)
		{
			relPath="bin"+File.separator+relPath;
			appClassPath=BundleFilesUtil.getFile(Activator.getBundeId(), relPath);
		}
		
		
		return appClassPath.replace(structure, "");
	}

}
