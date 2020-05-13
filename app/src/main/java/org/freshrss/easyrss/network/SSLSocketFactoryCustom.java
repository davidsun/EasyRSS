package org.freshrss.easyrss.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class SSLSocketFactoryCustom extends SSLSocketFactory {

	private final SSLSocketFactory sslSocketFactory;
	
	public SSLSocketFactoryCustom() {
		this.sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return this.sslSocketFactory.createSocket(s, host, port, autoClose);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return this.sslSocketFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return this.sslSocketFactory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		SSLSocket sslSocket = (SSLSocket)this.sslSocketFactory.createSocket(host, port);
		sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
		return sslSocket;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		SSLSocket sslSocket = (SSLSocket)this.sslSocketFactory.createSocket(host, port);
		sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
		return sslSocket;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
		SSLSocket sslSocket = (SSLSocket)this.sslSocketFactory.createSocket(host, port, localHost, localPort);
		sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
		return sslSocket;
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		SSLSocket sslSocket = (SSLSocket)this.sslSocketFactory.createSocket(address, port, localAddress, localPort);
		sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
		return sslSocket;
	}

}
