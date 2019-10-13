package br.ufc.great.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.Actuators;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest;
import br.ufc.great.protoc.LabMonitorProtos.Sensor;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.ServerResponseStatus;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.ServerResponseType;

public class TCPClient {
	public static void main(String args[]) {
		int serverPort = 7896;
		String serverIp = "localhost";
		
		Socket socket = null;
		try {
			socket = new Socket(serverIp, serverPort);
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			
			ClientRequest request = LabMonitorProtos.ClientRequest.newBuilder().setReqType(ClientRequest.ClientRequestType.GET_SENSORS_DATA).build();
			request.writeDelimitedTo(outputStream);
			
			ServerResponse serverResponse = LabMonitorProtos.ServerResponse.parseDelimitedFrom(inputStream);
			if(serverResponse.getRespStatus().getNumber() == ServerResponseStatus.OK_VALUE && serverResponse.getRespType().getNumber() == ServerResponseType.GET_SENSORS_DATA_VALUE){
				System.out.println("Server Response (Last updated in " + serverResponse.getSensorsData().getLastUpdated() + "):");
				for(Sensor sensor : serverResponse.getSensorsData().getDataList()){
					System.out.println("|-- " + sensor.getName() + ": " + sensor.getValue());
				}
			}
			
			//Considering the values of sensors, user can actuate in environment...
			Actuators.Builder builder = LabMonitorProtos.Actuators.newBuilder();
			builder.setBuzzer(LabMonitorProtos.Buzzer.newBuilder().setFreq(220).setDuration(1000).setTimes(5).setStatus("pending").build());
			builder.setLed(LabMonitorProtos.Led.newBuilder().setColor("green").setStatus("on").build());
			request = LabMonitorProtos.ClientRequest
				.newBuilder().setReqType(ClientRequest.ClientRequestType.SET_ACTUATORS_VALUE)
				.setActuatorsValue(builder.build()).build();
			request.writeDelimitedTo(outputStream);
			
			serverResponse = LabMonitorProtos.ServerResponse.parseDelimitedFrom(inputStream);
			if(serverResponse.getRespStatus().getNumber() == ServerResponseStatus.OK_VALUE && serverResponse.getRespType().getNumber() == ServerResponseType.SET_ACTUATORS_VALUE_VALUE){
				System.out.println("Actuators successfully fired!");
			}
		} catch (UnknownHostException e) {
			System.out.println("Socket:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("readline:" + e.getMessage());
		} finally {
			if (socket != null) try {
				socket.close();
			} catch (IOException e) {
				System.out.println("close:" + e.getMessage());
			}
		}
	}
}
