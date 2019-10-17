package br.ufc.great.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest;
import br.ufc.great.protoc.LabMonitorProtos.SensorsData;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest.ClientRequestType;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.Builder;
import br.ufc.great.services.MyFirebaseService;
import br.ufc.great.services.ProtocolBufferService;

/**
 * Servlet implementation class MainController
 */
public class MainController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MainController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Builder responseBuilder = LabMonitorProtos.ServerResponse.newBuilder();
		ClientRequest clientRequest = ClientRequest.parseDelimitedFrom(request.getInputStream());
		
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
		
		response.setContentType("application/octet-stream");
		ServerResponse serverResponse = responseBuilder.build();
		serverResponse.writeDelimitedTo(response.getOutputStream());
	}
}
