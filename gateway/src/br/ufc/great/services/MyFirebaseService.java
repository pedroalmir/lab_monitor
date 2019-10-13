/**
 * 
 */
package br.ufc.great.services;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import com.google.gson.Gson;

import net.thegreshams.firebase4j.error.FirebaseException;
import net.thegreshams.firebase4j.service.Firebase;

/**
 * @author PedroAlmir
 */
public class MyFirebaseService {
	/**
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static LinkedHashMap<String, String> getLastSensorsData(){
		try {
			Firebase firebase = new Firebase("https://greatlabmonitor.firebaseio.com");
			firebase = firebase.addQuery("orderBy", "\"$key\"");
			firebase = firebase.addQuery("limitToLast", "1");
			
			String response = firebase.getAsString("envs/great/lab10/registers");
			LinkedHashMap result = (LinkedHashMap) new Gson().fromJson(response, Object.class);
			return (LinkedHashMap<String, String>) result.get(result.keySet().iterator().next());
		} catch (FirebaseException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static LinkedHashMap<String, String> getActuatorStatus(){
		try {
			Firebase firebase = new Firebase("https://greatlabmonitor.firebaseio.com");
			String response = firebase.getAsString("envs/great/lab10/info/actuators");
			return (LinkedHashMap) new Gson().fromJson(response, Object.class);
		} catch (FirebaseException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Update LED actuator value
	 * 
	 * @param color [red, yellow, green]
	 * @param status [on, off]
	 */
	public static void updateLedActuator(String color, String status){
		try {
			Firebase firebase = new Firebase("https://greatlabmonitor.firebaseio.com");
			firebase.putAsString("envs/great/lab10/info/actuators/led", "{\"status\":\"" + status + "\", \"color\":\"" + color + "\"}");
		} catch (FirebaseException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update Buzzer Actuator
	 * 
	 * @param freq
	 * @param duration
	 * @param times
	 * @param status [pending, done]
	 */
	public static void updateBuzzerActuator(int freq, int duration, int times, String status){
		try {
			Firebase firebase = new Firebase("https://greatlabmonitor.firebaseio.com");
			firebase.putAsString("envs/great/lab10/info/actuators/buzzer", "{\"freq\":" + freq + ", \"duration\":" + duration + ", \"times\":" + times + ", \"status\":\"" + status + "\"}");
		} catch (FirebaseException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param json
	 */
	public static void updateActuators(String json){
		try {
			Firebase firebase = new Firebase("https://greatlabmonitor.firebaseio.com");
			firebase.patchAsString("envs/great/lab10/info/actuators", json);
		} catch (FirebaseException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws FirebaseException, UnsupportedEncodingException {
		System.out.println(MyFirebaseService.getLastSensorsData());
		
		MyFirebaseService.updateLedActuator("yellow", "on");
		MyFirebaseService.updateBuzzerActuator(220, 500, 3, "done");
		
		System.out.println(MyFirebaseService.getActuatorStatus());
	}
}
