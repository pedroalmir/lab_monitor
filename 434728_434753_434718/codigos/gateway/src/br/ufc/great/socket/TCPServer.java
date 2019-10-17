package br.ufc.great.socket;

import java.net.*;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest.ClientRequestType;
import br.ufc.great.protoc.LabMonitorProtos.SensorsData;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.Builder;
import br.ufc.great.services.MyFirebaseService;
import br.ufc.great.services.ProtocolBufferService;

import java.io.*;

/**
 * @author PedroAlmir
 */
public class TCPServer {
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		ServerSocket listenSocket = null;
		int serverPort = 7896; // the server port
		try {
			listenSocket = new ServerSocket(serverPort);
			while (true) {
				Socket clientSocket = listenSocket.accept();
				Connection c = new Connection(clientSocket);
				c.start();
			}
		} catch (IOException e) {
			System.err.println("Listen socket: " + e.getMessage());
		}
	}
}

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
