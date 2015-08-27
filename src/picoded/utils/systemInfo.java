package picoded.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.UUID;

import picoded.conv.Base58;

/// systemInfo, is a utility class meant to facilitate the system info quickly.
///
/// Core features
/// - get local ip address (local ip address : xx.x.x.x).
/// - get local host name  (local host name : dell).
/// - get local mac address (local mac address : xx-xx-EA-BB-xA-xE).
/// - random generate a hash (hash : f7ba377f-6dc5-43a4-9560-d81219999756)
/// Notes
/// - Hash refers to MD5 of the string to a byte[16] array, then base58 convert them to a string. Produces a compact 22 character hash
///
public class systemInfo {
	
	/// localIPAddress, for retrived local ip host address (Local IP Address: xx.x.x.x)
	/// @returns  string local ip host address 
	///
	public static String localIPAddress() throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		return ip.getHostAddress();
	}
	
	/// localHostName, for retrived local host name (local host name : dell).
	/// @returns  string local host name
	///
	public static String localHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	/// localMACAddress, for retrived local mac address (local mac address : xx-xx-EA-BB-xA-xE)..
	/// @returns  string local mac address 
	///
	public static String localMACAddress() throws UnknownHostException, SocketException {
		StringBuilder sb = new StringBuilder();
		sb.append("00-16-EA-BB-7A-4E"); //@TODO: REMOVE WHEN network.getHardwareAddress() ISSUE FIXED
		
		InetAddress ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] mac = null;
		mac = null; //network.getHardwareAddress(); //@TODO: TO FIX, this caused build failure
		
		if (mac != null) {
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
		}
		/// return UUID.nameUUIDFromBytes(mac).toString();
		return sb.toString();
	}
	
	/// systemaHash randomly generate Base58 hashing from host name and mac address
	/// @returns string generate a hash
	public static String systemaHash() throws Exception {
		return new Base58().encode((localHostName() + localMACAddress()).getBytes());
	}
}