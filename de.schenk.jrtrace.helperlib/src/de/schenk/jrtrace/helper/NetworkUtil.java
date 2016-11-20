/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetworkUtil {
	/**
	 * 
	 * @return a list of all network address that are not loopback or linklocal
	 */
	public static String[] getNonLoopbackAndNonLinkLocalAddresses() {
		ArrayList<String> names = new ArrayList<String>();
		try {

			Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {

				NetworkInterface iface = ifaces.nextElement();

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (!address.isLinkLocalAddress()
							&& !address.isLoopbackAddress()) {
						names.add(address.getHostAddress());
					}

				}
			}

		} catch (IOException e) {
			throw new RuntimeException(
					"Unable to obtain the network interfaces.", e);
		}
		return names.toArray(new String[names.size()]);
	}
}
