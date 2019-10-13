/**
 * 
 */
package br.ufc.great.services;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.Actuators;
import br.ufc.great.protoc.LabMonitorProtos.SensorsData;
import br.ufc.great.protoc.LabMonitorProtos.SensorsData.Builder;

/**
 * @author PedroAlmir
 * SensorsDataProtos.SensorsData message = 
 */
public class ProtocolBufferService {
	
	/**
	 * Create SensorsData message
	 * @param map
	 * @return SensorsData object
	 */
	public static SensorsData createSensorsDataMessage(LinkedHashMap<String, String> map){
		Builder builder = LabMonitorProtos.SensorsData.newBuilder()
			.setLastUpdated(map.get("date"));
		
		for(String key : map.keySet()){
			if(key.equals("date")) continue;
			
			LabMonitorProtos.Sensor sensor = 
				LabMonitorProtos.Sensor.newBuilder()
					.setName(key)
					.setValue(map.get(key))
					.build();
			
			builder.addData(sensor);
		}
		
		return builder.build();
	}
	
	/**
	 * Decode ActuatorAction to JSON
	 * @param message
	 * @return JSON string or null if message is empty
	 */
	public static String decodeActuatorActionToJson(byte[] message){
		try {
			Actuators actuators = LabMonitorProtos.Actuators.parseFrom(message);
			
			StringBuffer buffer = new StringBuffer("{");
			if(actuators.getBuzzer() != null && !actuators.getBuzzer().getStatus().isEmpty()){
				String buzzerJson = "\"buzzer\": {\"freq\":" + actuators.getBuzzer().getFreq() 
							 + ", \"duration\":" + actuators.getBuzzer().getDuration() 
							    + ", \"times\":" + actuators.getBuzzer().getTimes() 
							 + ", \"status\":\"" + actuators.getBuzzer().getStatus() + "\"}";
				buffer.append(buzzerJson);
			}
			
			if(actuators.getLed() != null && !actuators.getLed().getStatus().isEmpty()){
				String ledJson = "\"led\": {\"status\":\"" + actuators.getLed().getStatus() 
									  + "\", \"color\":\"" + actuators.getLed().getColor() + "\"}";
				
				if(actuators.getBuzzer() != null && !actuators.getBuzzer().getStatus().isEmpty()) buffer.append(", ");
				buffer.append(ledJson);
			}
			
			if(buffer.length() == 1) return null;
				
			buffer.append("}");
			return buffer.toString();
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Decode ActuatorAction to JSON
	 * 
	 * @param actuators
	 * @return JSON string or null if message is empty
	 */
	public static String decodeActuatorActionToJson(Actuators actuators){
		StringBuffer buffer = new StringBuffer("{");
		if(actuators.getBuzzer() != null && !actuators.getBuzzer().getStatus().isEmpty()){
			String buzzerJson = "\"buzzer\": {\"freq\":" + actuators.getBuzzer().getFreq() 
						 + ", \"duration\":" + actuators.getBuzzer().getDuration() 
						    + ", \"times\":" + actuators.getBuzzer().getTimes() 
						 + ", \"status\":\"" + actuators.getBuzzer().getStatus() + "\"}";
			buffer.append(buzzerJson);
		}
		
		if(actuators.getLed() != null && !actuators.getLed().getStatus().isEmpty()){
			String ledJson = "\"led\": {\"status\":\"" + actuators.getLed().getStatus() 
								  + "\", \"color\":\"" + actuators.getLed().getColor() + "\"}";
			
			if(actuators.getBuzzer() != null && !actuators.getBuzzer().getStatus().isEmpty()) buffer.append(", ");
			buffer.append(ledJson);
		}
		
		if(buffer.length() == 1) return null;
			
		buffer.append("}");
		return buffer.toString();
	}
	
	public static void main(String[] args) throws IOException {
		//Client code
		Actuators.Builder builder = LabMonitorProtos.Actuators.newBuilder();
		builder.setBuzzer(LabMonitorProtos.Buzzer.newBuilder().setFreq(320).setDuration(500).setTimes(3).setStatus("done").build());
		builder.setLed(LabMonitorProtos.Led.newBuilder().setColor("red").setStatus("off").build());
		Actuators actuatorsMessage = builder.build();
		byte[] byteArray = actuatorsMessage.toByteArray();
		// Sending by network...
		
		String json = ProtocolBufferService.decodeActuatorActionToJson(byteArray);
		System.out.println(json);
		if(json != null){
			MyFirebaseService.updateActuators(json);
		}
		
		// Write the new address book back to disk.
	    //FileOutputStream output = new FileOutputStream("data");
		//SensorsData sensorsData = ProtocolBufferService.createSensorsDataMessage(FirebaseService.getLastSensorsData());
		//sensorsData.writeTo(output);
	}
}
