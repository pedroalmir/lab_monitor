/**
 * 
 */
package br.ufc.great.test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest;
import br.ufc.great.protoc.LabMonitorProtos.Sensor;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.ServerResponseStatus;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.ServerResponseType;

/**
 * @author PedroAlmir
 *
 */
public class ServerPostTest {
	/**
	 * @param args
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
        String url = "http://localhost:8080/gateway/labmonitor";

        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

        //Add request header
        httpClient.setRequestMethod("POST");
        httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");
        httpClient.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        
        // Send post request
        httpClient.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpClient.getOutputStream());
        
        ClientRequest request = LabMonitorProtos.ClientRequest.newBuilder().setReqType(ClientRequest.ClientRequestType.GET_SENSORS_DATA).build();
		request.writeDelimitedTo(dataOutputStream);
		
		ServerResponse serverResponse = LabMonitorProtos.ServerResponse.parseDelimitedFrom(httpClient.getInputStream());
		if(serverResponse.getRespStatus().getNumber() == ServerResponseStatus.OK_VALUE && serverResponse.getRespType().getNumber() == ServerResponseType.GET_SENSORS_DATA_VALUE){
			System.out.println("Server Response (Last updated in " + serverResponse.getSensorsData().getLastUpdated() + "):");
			for(Sensor sensor : serverResponse.getSensorsData().getDataList()){
				System.out.println("|-- " + sensor.getName() + ": " + sensor.getValue());
			}
		}
	}
}
