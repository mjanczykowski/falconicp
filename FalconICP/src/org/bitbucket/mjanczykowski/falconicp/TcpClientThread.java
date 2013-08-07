package org.bitbucket.mjanczykowski.falconicp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.bitbucket.mjanczykowski.falconicp.FalconICP.IncomingHandler;

import android.os.Message;
import android.util.Log;

public class TcpClientThread extends Thread {
	
	/** Indicates whether the thread should be alive. */
	private boolean connected = false;
	
	private String serverIP;
	private int serverPort;
	private int timeout;
	
	/* Connection */
	private Socket socket;
	private PrintWriter out;
	private InputStream in;
	
	/* Application */
	private IncomingHandler handler;
	private byte[][] dedLines;

	public TcpClientThread(String serverIP, int serverPort, int timeout, IncomingHandler handler) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.handler = handler;
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		Log.i("tcp thread", "started");
				
		try {
			Log.i("tcp thread", "Connecting to server: " + serverIP + ", " + serverPort);
			
			socket = new Socket();
			socket.connect(new InetSocketAddress(serverIP, serverPort), timeout);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = socket.getInputStream();
			
			Log.i("tcp thread", "connected");
			
			sendHandlerMessage(IncomingHandler.CONNECTED, String.format("%s:%d", serverIP, serverPort));
			
			connected = true;
			
			byte[] buffer = new byte[130];
			
			while(connected) {
				int fromServer;
				
				if((fromServer = in.read(buffer)) > 0) {
					
					if(dedLines != null) {
						synchronized(dedLines) {
							if(fromServer == 130) {
								for(int i = 0; i < 130; i++)
								{
									dedLines[i/26][i%26] = buffer[i];
								}
							}
						}
					}
					
					sendHandlerMessage(IncomingHandler.DATA, "");
				}
				else {
					Log.v("tcpConnection", "running = false");
					connected = false;
				}
			}
			
			synchronized(this) {
				out.close();
				in.close();
				socket.close();
			}
			sendHandlerMessage(IncomingHandler.DISCONNECTED, "");
			
		} catch (UnknownHostException e) {
			Log.d("tcp thread", "UnknownHostException " + e.getMessage());
			sendHandlerMessage(IncomingHandler.CONNECTION_ERROR, e.getMessage());
		} catch (SocketException e) {
			Log.d("tcp thread", "SocketException " + e.getMessage());
			String msg = e.getMessage();
			if(msg.equals("Bad file number")) {
				msg = "Interrupted";
			}
			sendHandlerMessage(IncomingHandler.CONNECTION_ERROR, msg);
		} catch (IOException e) {
			Log.d("tcp thread", "IOException " + e.getMessage());
			sendHandlerMessage(IncomingHandler.CONNECTION_ERROR, e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.d("tcp thread", "IllegalArgumentException " + e.getMessage());
			sendHandlerMessage(IncomingHandler.CONNECTION_ERROR, "Invalid IP or port number.");
		}
		
		Log.i("tcp thread", "closed");
	}
	
	/**
     * Closes TCP thread by closing socket and settings running to false 
     */
	public void closeThread() {
		synchronized(this) {
			if(socket != null)
			{
				try {
					if(out != null) {
						out.write("BYE");
						out.flush();
					}
					socket.close();
				} catch (IOException e) {
					Log.d("tcp thread", "IOException " + e.getMessage());
				}
			}
		}
		connected = false;
	}
	
	/**
	 * Sets reference to shared dedLines array
	 * @param dedLines Shared dedLines array
	 */
	public void setDedLines(byte[][] dedLines) {
		this.dedLines = dedLines;
	}
	
	/**
	 * Sends Falcon keystroke callback to server.
	 * @param callback Falcon keystroke callback
	 */
	public void sendCallback(String callback) {
		synchronized(this) {
			if(connected && out != null) {
				out.write(callback);
				out.flush();
			}
		}
	}
	
	/**
	 * Sends message to main activity handler.
	 * @param what Category of message from IncomingHandler static values
	 * @param txt Additional information to display
	 */
	private void sendHandlerMessage(int what, String txt) {
		Message msg = new Message();
		msg.what = what;
		msg.obj = txt;
		handler.sendMessage(msg);	
	}
}
