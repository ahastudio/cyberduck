package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;
import com.sshtools.j2ssh.transport.HostKeyVerification;

public class
    Host {
	private static Logger log = Logger.getLogger(Host.class);

	private String protocol;
	private int port;
	private String hostname;
	private String nickname;
	private String identification;
	private String defaultpath = Path.HOME;
	private transient HostKeyVerification hostKeyVerification;
	private transient Login login;

	public static final String HOSTNAME = "Hostname";
	public static final String NICKNAME = "Nickname";
	public static final String PORT = "Port";
	public static final String PROTOCOL = "Protocol";
	public static final String USERNAME = "Username";
	public static final String PATH = "Path";
	public static final String KEYFILE = "Private Key File";

	public Host(NSDictionary dict) {
		Object protocolObj = dict.objectForKey(Host.PROTOCOL);
		if(protocolObj != null) {
			this.setProtocol((String)protocolObj);
		}
		Object hostnameObj = dict.objectForKey(Host.HOSTNAME);
		if(hostnameObj != null) {
			this.setHostname((String)hostnameObj);
			Object usernameObj = dict.objectForKey(Host.USERNAME);
			if(usernameObj != null) {
				this.setCredentials((String)usernameObj, null);
			}
			this.getCredentials().setPrivateKeyFile((String)dict.objectForKey(Host.KEYFILE));
		}
		Object portObj = dict.objectForKey(Host.PORT);
		if(portObj != null) {
			this.setPort(Integer.parseInt((String)portObj));
		}
		Object pathObj = dict.objectForKey(Host.PATH);
		if(pathObj != null) {
			this.setDefaultPath((String)pathObj);
		}
		Object nicknameObj = dict.objectForKey(Host.NICKNAME);
		if(nicknameObj != null) {
			this.setNickname((String)nicknameObj);
		}
		log.debug(this.toString());
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.getProtocol(), Host.PROTOCOL);
		dict.setObjectForKey(this.getNickname(), Host.NICKNAME);
		dict.setObjectForKey(this.getHostname(), Host.HOSTNAME);
		dict.setObjectForKey(this.getPort()+"", Host.PORT);
		dict.setObjectForKey(this.getCredentials().getUsername(), Host.USERNAME);
		dict.setObjectForKey(this.getDefaultPath(), Host.PATH);
		if(this.getCredentials().getPrivateKeyFile() != null) {
			dict.setObjectForKey(this.getCredentials().getPrivateKeyFile(), Host.KEYFILE);
		}
		return dict;
	}

	public Host copy() {
		Host copy = new Host(this.getProtocol(),
							 this.getHostname(),
							 this.getPort(), 
							 this.getDefaultPath());
		copy.setCredentials(this.login.getUsername(), this.login.getPassword());
		return copy;
	}
	
	/**
	 * For internal use only.
	 *
	 * @param url Must be in the format protocol://user@hostname:portnumber
	 */
	public Host(String url) throws java.net.MalformedURLException {
		try {
			this.protocol = url.substring(0, url.indexOf("://"));
			this.hostname = url.substring(url.indexOf("@")+1, url.lastIndexOf(":"));
			this.port = Integer.parseInt(url.substring(url.lastIndexOf(":")+1, url.length()));
			this.setCredentials(url.substring(url.indexOf("://")+3, url.lastIndexOf("@")), null);
			this.nickname = this.getCredentials().getUsername()+"@"+this.getHostname();
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
			throw new java.net.MalformedURLException("Not a valid URL: "+url);
		}
		catch(IndexOutOfBoundsException e) {
			log.error(e.getMessage());
			throw new java.net.MalformedURLException("Not a valid URL: "+url);
		}
		log.debug(this.toString());
	}

	/**
	 * New host with the default protocol for this port
	 *
	 * @param hostname The hostname of the server
	 * @param port     The port number to connect to
	 */
	public Host(String hostname, int port) {
		this(getDefaultProtocol(port), hostname, port);
	}

	public Host(String protocol, String hostname) {
		this(protocol, hostname, getDefaultPort(protocol));
	}
	
	/**
	 * @param protocol The protocol to use, must be either Session.HTTP, Session.FTP or Session.SFTP
	 * @param hostname The hostname of the server
	 * @param port     The port number to connect to
	 * @param login    The login credentials to use
	 */
	public Host(String protocol, String hostname, int port) {
		this(protocol, hostname, port, null);
	}

	public Host(String protocol, String hostname, int port, String defaultpath) {
		this.setProtocol(protocol);
		this.setPort(port);
		this.setHostname(hostname);
		this.setNickname(nickname);
		this.setDefaultPath(defaultpath);
		this.setCredentials(null, null);
		
		log.debug(this.toString());
	}

	// ----------------------------------------------------------

	public void setDefaultPath(String defaultpath) {
		this.defaultpath = defaultpath;
	}

	public String getDefaultPath() {
		if(this.defaultpath == null || this.defaultpath.equals("")) {
			return Path.HOME;
		}
		return this.defaultpath;
	}

	public boolean hasReasonableDefaultPath() {
		return this.defaultpath != null && !this.defaultpath.equals("") && !this.defaultpath.equals(Path.HOME);
	}

	protected static String getDefaultProtocol(int port) {
		switch(port) {
			case Session.FTP_PORT:
				return Session.FTP;
			case Session.SSH_PORT:
				return Session.SFTP;
			default:
				throw new IllegalArgumentException("Cannot find protocol for port number "+port);
		}

	}

	private static int getDefaultPort(String protocol) {
		if(protocol.equals(Session.FTP)) {
			return Session.FTP_PORT;
		}
		else if(protocol.equals(Session.SFTP)) {
			return Session.SSH_PORT;
		}
		throw new IllegalArgumentException("Cannot find port number for protocol "+protocol);
	}

	// ----------------------------------------------------------
	// Accessor methods
	// ----------------------------------------------------------

	public void setCredentials(Login login) {
		this.login = login;
	}
	
	public void setCredentials(String username, String password) {
		this.setCredentials(username, 
							password, 
							Preferences.instance().getProperty("connection.login.useKeychain").equals("true")
							);
	}
		
	public void setCredentials(String username, String password, boolean addToKeychain) {
		this.setCredentials(new Login(this, username, password, addToKeychain));
	}
	
	public Login getCredentials() {
		return this.login;
	}

	/**
	 * @param protocol The protocol to use or null to use the default protocol for this port number
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol != null ? protocol : Preferences.instance().getProperty("connection.protocol.default");
	}

	public String getProtocol() {
		return this.protocol;
	}

	/**
	 * @return The remote host identification such as the response to the SYST command in FTP
	 */
	public String getIdentification() {
		return this.identification;
	}

	public void setIdentification(String id) {
		this.identification = id;
	}

	public String getNickname() {
		return this.nickname != null ? this.nickname : this.getHostname()+" ("+this.getProtocol().toUpperCase()+")";
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @param port The port number to connect to or -1 to use the default port for this protocol
	 */
	public void setPort(int port) {
		this.port = port != -1 ? port : this.getDefaultPort(this.getProtocol());
	}

	public int getPort() {
		return this.port;
	}

	//ssh specific
	public void setHostKeyVerificationController(HostKeyVerification h) {
		this.hostKeyVerification = h;
	}

	public HostKeyVerification getHostKeyVerificationController() {
		return this.hostKeyVerification;
	}

	/**
	 * @return The IP address of the remote host if available
	 */
	public String getIp() throws java.net.UnknownHostException {
		try {
			return java.net.InetAddress.getByName(hostname).toString();
		}
		catch(java.net.UnknownHostException e) {
			throw new java.net.UnknownHostException("Hostname cannot be resolved");
		}
	}

	public String toString() {
		return this.getURL();
	}

	/**
	 * protocol://user@host:port
	 *
	 * @return The URL of the remote host including user login hostname and port
	 */
	public String getURL() {
		return this.getProtocol()+"://"+this.getCredentials().getUsername()+"@"+this.getHostname()+":"+this.getPort();//+"/"+this.getDefaultPath();
	}

	public boolean equals(Object other) {
		return this.toString().equals(other.toString());
	}
}
