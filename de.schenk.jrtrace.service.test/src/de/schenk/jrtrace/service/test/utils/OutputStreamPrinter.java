/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service.test.utils;

import java.io.IOException;
import java.io.InputStream;

public class OutputStreamPrinter extends Thread {
	InputStream stream;
	public OutputStreamPrinter(InputStream inputStream) {
		stream=inputStream;
	}

	@Override
	public void run() {
		byte[] buffer=new byte[1024];
		while(true)
		{
			try {
				int size = stream.read(buffer);
				if(size==-1) { System.out.println(">> No more data");break;}
				if(size==buffer.length)
				{
					System.out.println(new String(buffer));
				}
				else
				{
					byte[] buffer2=new byte[size];
					for(int i=0;i<size;i++) buffer2[i]=buffer[i];
					System.out.println(new String(buffer2));
				}
			} catch (IOException e) {
				System.err.println(">>  IOException: No more data from process");
				break;
			}
		}
	}
}
