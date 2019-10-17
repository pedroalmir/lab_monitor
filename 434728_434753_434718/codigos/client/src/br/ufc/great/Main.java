package br.ufc.great;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import br.ufc.great.protoc.LabMonitorProtos;
import br.ufc.great.protoc.LabMonitorProtos.Actuators;
import br.ufc.great.protoc.LabMonitorProtos.ClientRequest;
import br.ufc.great.protoc.LabMonitorProtos.Sensor;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.ServerResponseStatus;
import br.ufc.great.protoc.LabMonitorProtos.ServerResponse.ServerResponseType;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Main class of Client TCP with Protocol Buffer
 * 
 * @course Sistemas Distribuídos e Redes de Computadores
 * @professor Paulo Antonio Leal Rego
 * @author Pedro Almir Martins de Oliveira
 * @date 13/10/2019
 * 
 * How to use this program:
 * java -jar client-sd-1.0.0.jar sensors -ip 127.0.0.1 -port 7896
 * java -jar client-sd-1.0.0.jar actuator -ip 127.0.0.1 -port 7896 -ledColor red -ledStatus on -buzzerFreq 220 -buzzerDuration 500 -buzzerTimes 5 -buzzerStatus pending
 *    
 * @Bugs report to pedro.oliveira@ifma.edu.br
 */
public class Main {
	public static void main(String args[]) {
		/* Args util */
		ArgumentParser parser = ArgumentParsers.newArgumentParser("client_sd");
		Subparsers subparsers = parser.addSubparsers().dest("mode");
		
		Subparser sensorsParser = subparsers.addParser("sensors").help("Sensors Mode");
		sensorsParser.addArgument("-ip").required(true).setDefault("127.0.0.1").help("Gateway IP");
		sensorsParser.addArgument("-port").type(Integer.class).required(true).setDefault(7896).help("Gateway Port");
		
		Subparser actuatorParser = subparsers.addParser("actuator").help("Actuator Mode");
		actuatorParser.addArgument("-ip").required(true).setDefault("127.0.0.1").help("Server IP");
		actuatorParser.addArgument("-port").type(Integer.class).required(true).setDefault(7896).help("Gateway Port");
		
		actuatorParser.addArgument("-ledColor").type(String.class).setDefault("red").required(false).help("Led color to activate [red, yellow, green]");
		actuatorParser.addArgument("-ledStatus").type(String.class).setDefault("on").required(false).help("Led status [on, off]");
		
		actuatorParser.addArgument("-buzzerFreq").type(Integer.class).setDefault(220).required(false).help("Buzzer beep frequency");
		actuatorParser.addArgument("-buzzerDuration").type(Integer.class).setDefault(500).required(false).help("Buzzer beep duration");
		actuatorParser.addArgument("-buzzerTimes").type(Integer.class).setDefault(3).required(false).help("Buzzer beep times");
		actuatorParser.addArgument("-buzzerStatus").type(String.class).setDefault("pending").required(false).help("Buzzer status [pending, done]");
		
		Socket socket = null;
		try {
            Namespace namespace = parser.parseArgs(args); checkArgs(namespace);
            String gatewayIP = namespace.getString("ip");
            Integer gatewayPort = namespace.getInt("port");
            
            socket = new Socket(gatewayIP, gatewayPort);
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            
            if(namespace.getString("mode").equalsIgnoreCase("sensors")){
            	ClientRequest request = LabMonitorProtos.ClientRequest.newBuilder().setReqType(ClientRequest.ClientRequestType.GET_SENSORS_DATA).build();
    			request.writeDelimitedTo(outputStream);
    			
    			ServerResponse serverResponse = LabMonitorProtos.ServerResponse.parseDelimitedFrom(inputStream);
    			if(serverResponse.getRespStatus().getNumber() == ServerResponseStatus.OK_VALUE && serverResponse.getRespType().getNumber() == ServerResponseType.GET_SENSORS_DATA_VALUE){
    				System.out.println("Server Response (Last updated in " + serverResponse.getSensorsData().getLastUpdated() + "):");
    				for(Sensor sensor : serverResponse.getSensorsData().getDataList()){
    					System.out.println("|-- " + sensor.getName() + ": " + sensor.getValue());
    				}
    			}
            }else if(namespace.getString("mode").equalsIgnoreCase("actuator")){
            	//Considering the values of sensors, user can actuate in environment...
    			Actuators.Builder builder = LabMonitorProtos.Actuators.newBuilder();
    			builder.setBuzzer(LabMonitorProtos.Buzzer.newBuilder()
    					.setFreq(namespace.getInt("buzzerFreq"))
    					.setDuration(namespace.getInt("buzzerDuration"))
    					.setTimes(namespace.getInt("buzzerTimes"))
    					.setStatus(namespace.getString("buzzerStatus"))
    					.build());
    			
    			builder.setLed(LabMonitorProtos.Led.newBuilder()
    					.setColor(namespace.getString("ledColor"))
    					.setStatus(namespace.getString("ledStatus"))
    					.build());
    			
    			ClientRequest request = LabMonitorProtos.ClientRequest
    				.newBuilder().setReqType(ClientRequest.ClientRequestType.SET_ACTUATORS_VALUE)
    				.setActuatorsValue(builder.build()).build();
    			request.writeDelimitedTo(outputStream);
    			
    			ServerResponse serverResponse = LabMonitorProtos.ServerResponse.parseDelimitedFrom(inputStream);
    			if(serverResponse.getRespStatus().getNumber() == ServerResponseStatus.OK_VALUE && serverResponse.getRespType().getNumber() == ServerResponseType.SET_ACTUATORS_VALUE_VALUE){
    				System.out.println("Actuators successfully fired!");
    			}
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			if (socket != null) try {
				socket.close();
			} catch (IOException e) {
				System.out.println("close:" + e.getMessage());
			}
		}
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	private static void checkArgs(Namespace args) throws Exception{
        if(!args.getString("ip").matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")){
            throw new Exception("IP must be formatted as [0-255].[0-255].[0-255].[0-255]");
        }
        if(args.getInt("port") < 0 || args.getInt("port") > 65535){
        	throw new Exception("Port value must be in the range of 0 - 65535");
        }
    }
}
