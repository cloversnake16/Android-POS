package no.susoft.mobile.pos.display.initThreads.initWFThread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/*
 * Created by Sami on 10/27/2016.
 */

public class ClientInit extends Thread{
	private static final int SERVER_PORT = 4747;
	private InetAddress mServerAddr;
	
	public ClientInit(InetAddress serverAddr){
		mServerAddr = serverAddr;
	}
	
	@Override
	public void run() {
		while (true) {
			Socket socket = new Socket();
			try {
				socket.bind(null);
				socket.connect(new InetSocketAddress(mServerAddr, SERVER_PORT));
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (!socket.isConnected()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}
}
