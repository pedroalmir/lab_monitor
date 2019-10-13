/**
 * 
 */
package br.ufc.great.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest;
import br.ufc.great.protoc.LabMonitorProtos.SensorsData;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest.ClientRequestType;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.Builder;
import br.ufc.great.services.MyFirebaseService;
import br.ufc.great.services.ProtocolBufferService;

/**
 * MainDesktopController
 * 
 * @author PedroAlmir
 */
public class MainDesktopController {
	
	public static boolean hasAnswer = false;
	
	public final static int TCP_PORT = 7896;
	public final static int UDP_OUT_PORT = 4445;
	public final static int UDP_IN_PORT = 54809;
	
	
	/**
	 * Steps:
	 * 1. Start gateway
	 * 2. Send broadcast message
	 * 	2.1. Wait for a response
	 *  2.2. Get firebase URL
	 * 3. Start TCP Server
	 * 
	 * To monitor in wireshark
	 * 
	 * (udp && ip.dst == 255.255.255.255 && not dhcp) || (udp && ip.src == 172.90.10.113 && not dhcp)
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		broadcast("LabMonitor: something listening here?", InetAddress.getByName("255.255.255.255"));
		new BroadcastReceiver().start();
		while (!MainDesktopController.hasAnswer) { Thread.sleep(1000); /* Wait for a response */ }
		
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(TCP_PORT);
			while (true) {
				System.out.println("Receiving client connections...");
				Socket clientSocket = listenSocket.accept();
				Connection c = new Connection(clientSocket);
				c.start();
			}
		} catch (IOException e) {
			System.err.println("Listen socket: " + e.getMessage());
		}
	}
	
	public static void broadcast(String broadcastMessage, InetAddress address) throws IOException {
		DatagramSocket socket = null;
		
		socket = new DatagramSocket(UDP_IN_PORT);
		socket.setBroadcast(true);

		byte[] buffer = broadcastMessage.getBytes();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, UDP_OUT_PORT);
		socket.send(packet);
		socket.close();
	}
}


/**
 * BroadcastReceiver
 * 
 * @author PedroAlmir
 */
class BroadcastReceiver extends Thread {
	public void run() {
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(MainDesktopController.UDP_IN_PORT);
			System.out.println("Broadcast receiver listening...");
			byte[] receiveData = new byte[1024];
			
			while (!MainDesktopController.hasAnswer) {
				DatagramPacket request = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(request);

				String sentence = new String(request.getData(), 0, request.getLength());
				System.out.println("IP: " + request.getAddress().getHostAddress() + ", Port: " + request.getPort());
				System.out.println("Message: " + sentence);
				
				MainDesktopController.hasAnswer = true;
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) serverSocket.close();
		}
	}
}

/**
 * Connection
 * 
 * @author PedroAlmir
 */
class Connection extends Thread {
	
	DataInputStream inputStream;
	DataOutputStream outputStream;
	Socket clientSocket;

	public Connection(Socket aClientSocket) {
		try {
			clientSocket = aClientSocket;
			inputStream = new DataInputStream(clientSocket.getInputStream());
			outputStream = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Connection: " + e.getMessage());
		}
	}

	public void run() {
		while(clientSocket.isConnected()){
			try {
				Builder responseBuilder = LabMonitorProtos.ServerResponse.newBuilder();
				ClientRequest clientRequest = ClientRequest.parseDelimitedFrom(inputStream);
				
				ClientRequestType reqType = clientRequest.getReqType();
				if(reqType.getNumber() == ClientRequestType.GET_SENSORS_DATA_VALUE){
					SensorsData message = ProtocolBufferService.createSensorsDataMessage(MyFirebaseService.getLastSensorsData());
					responseBuilder.setRespType(ServerResponse.ServerResponseType.GET_SENSORS_DATA);
					responseBuilder.setRespStatus(ServerResponse.ServerResponseStatus.OK);
					responseBuilder.setSensorsData(message);
					
				}else if(reqType.getNumber() == ClientRequestType.SET_ACTUATORS_VALUE_VALUE){
					String json = ProtocolBufferService.decodeActuatorActionToJson(clientRequest.getActuatorsValue());
					if(json != null) MyFirebaseService.updateActuators(json);
					responseBuilder.setRespType(ServerResponse.ServerResponseType.SET_ACTUATORS_VALUE);
					responseBuilder.setRespStatus(ServerResponse.ServerResponseStatus.OK);
				}
				
				ServerResponse serverResponse = responseBuilder.build();
				serverResponse.writeDelimitedTo(outputStream);
			} catch (EOFException e) {
				System.err.println("EOF:" + e.getMessage());
				break;
			} catch (IOException e) {
				System.err.println("Readline:" + e.getMessage());
				break;
			} catch (NullPointerException npEx){
				try {
					clientSocket.close();
					break;
				} catch (IOException e) { }
			}
		}
	}
}
