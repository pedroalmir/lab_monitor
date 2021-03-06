#include <Ultrasonic.h>

#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

#include <WiFi.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <FirebaseESP32.h>
#include <ArduinoJson.h>

#include <DNSServer.h>    // Local DNS Server used for redirecting all requests to the configuration portal
#include <WebServer.h>    // Local WebServer used to serve the configuration portal
#include <WiFiManager.h>  // WiFi Configuration Manager

#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>

#define LDR_SENSOR  39
#define MQ7_SENSOR  34
#define LED_RED     32
#define LED_YELLOW  33
#define LED_GREEN   25

#define DHTPIN      27
#define DHTTYPE     DHT22

#define BUZZER_PIN  22
#define KY38_PIN    21
#define I2C_SCL     5
#define I2C_SDA     4
#define TRIGGER_PIN 0
#define ECHO_PIN    15

#define EULER 2.718281828459045235360287471352

#define WIFI_SSID
#define WIFI_PWD

#define FIREBASE_HOST
#define FIREBASE_AUTH

DHT dht(DHTPIN, DHTTYPE);                     // Create DHT sensor considering port and type
LiquidCrystal_I2C lcd(0x27,16,2);             // Set the LCD address to 0x27 for a 16 chars and 2 line display
Ultrasonic ultrasonic(TRIGGER_PIN, ECHO_PIN); // Create Ultrasonic considering Trigger and Echo pins
FirebaseData firebaseData;

WiFiUDP Udp;
unsigned int localUdpPort = 4445;                     // local port to listen on
char incomingPacket[255];                             // buffer for incoming packets

/**
 * LabMonitor Hardware v1 20191007
 * Pinout:
 *       Used -|3.3V       GND|- Used
 *            -|EN      GPIO23|- 
 *            -|GPIO36  GPIO22|- BUZZER_PIN
 * LDR_SENSOR -|GPIO39     TX0|-
 * MQ7_SENSOR -|GPIO34     RX0|-
 *            -|GPIO35  GPIO21|- KY38_PIN 
 *    LED_RED -|GPIO32     GND|- Used
 * LED_YELLOW -|GPIO33  GPIO19|- 
 *  LED_GREEN -|GPIO25  GPIO18|- 
 *            -|GPOI26  GPIO05|- I2C SCL
 *     DHTPIN -|GPIO27  GPIO17|-
 *            -|GPIO14  GPIO16|-
 *            -|GPIO12  GPIO04|- I2C SDA
 *    I2C GND -|GND     GPIO00|- TRIGGER_PIN
 *            -|GPIO13  GPIO02|- 
 *            -|GPIO09  GPIO15|- ECHO_PIN
 *            -|GPIO10  GPIO08|-
 *            -|GPIO11  GPIO07|-
 * I2C VCC(5v)-|Vin(5v) GPIO06|-
 *  
 *  >> Please, don't change it!!!
 * 
 * Setup function
 * 
 * Useful links:
 * https://randomnerdtutorials.com/esp32-dht11-dht22-temperature-humidity-sensor-arduino-ide/
 * https://github.com/evert-arias/EasyBuzzer
 * https://www.fernandok.com/2017/12/sensor-ultrassonico-com-esp32.html
 * https://www.arduinoecia.com.br/sensor-de-som-ky-038-microfone-arduino/
 * 
**/
void setup() {
  /* Initialize serial communications */
  Serial.begin(9600);
  
  /* Initializing the pins as in/output pins */
  pinMode(LDR_SENSOR, INPUT);
  pinMode(KY38_PIN, INPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);

  /* Initializing the lcd (SDA, SCL) */
  lcd.begin(I2C_SDA, I2C_SCL);    
  lcd.setCursor(0,0);
  lcd.backlight();
  lcd.clear();
  lcd.print("GREat LabMonitor");

  connectWifi();
  connectFirebase();

  Firebase.setString(firebaseData, "envs/great/lab10/info/status", "Normal");
  Firebase.setJSON(firebaseData, "envs/great/lab10/info/sensors", "{\"online\": [\"temperature\", \"humidity\", \"luminosity\", \"co2\"], \"offline\": [\"distance\", \"noise\"]}");

  Udp.begin(localUdpPort);
  Serial.printf("Now listening at IP %s, UDP port %d\n", WiFi.localIP().toString().c_str(), localUdpPort);

  IPAddress ip(255, 255, 255, 255);
  unsigned int port = 54809;
  
  // Send back a reply, to the IP address and port we got the packet from
  Udp.beginPacket(ip, port);
  Udp.print("Hi. I am here! Saving data in: greatlabmonitor.firebaseio.com");
  Udp.endPacket();
  
  /* Initializing the DHT sensor */
  dht.begin();
  
  delay(5000);
  beep(220, 200, 3);
  Serial.println(F("GREat Lab Monitor Hardware: setup completed successfully!"));
}

/**
 * Save sensor raw data in Firebase database
 * @param date DD/mm/YYYY HH:MM:SS
 * @param temperature from DHT22
 * @param humidity from DHT22
 * @param co2 from MQ7 sensor
 * @param lumens from LDR sensor
 * @param distance from ultrasonic
 * @param noise from KY-038 sensor
 **/
void saveSensorData(String date, String temperature, String humidity, String co2, String lumens, String distance, String noise){
  String json = String("{\"date\":\"" + date + "\",\"temperature\":\"" + temperature + "\",\"humidity\":\"" + humidity + "\",\"co2\":\"" + co2 + "\",\"lumens\":\"" + lumens + "\",\"distance\":\"" + distance + "\",\"noise\":\"" + noise + "\"}");
  Serial.println(json);
  Firebase.pushJSON(firebaseData, "envs/great/lab10/registers", json);
}

/**
 * Connect to WiFi method.
 * In this case, we have used a hard coded network
 * to improve the performance and avoid problems in
 * presentation.
 */
void connectWifi(){
  WiFi.begin(WIFI_SSID, WIFI_PWD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);Serial.println("Connecting to WiFi...");
    printMessageLCD("GREat LabMonitor", "Connecting...");
  }
  Serial.println("Connected to the WiFi network");
  printMessageLCD("GREat LabMonitor", "Connected!!!");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

/**
 * Firebase connection methods:
 * 
 * - Connect to firebase database;
 * - Close connection with firebase (not used yet);
 * - Reconnect to firebase database (not used yet);
 */
void connectFirebase(){
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  //Firebase.setMaxRetry(firebaseData, 3);
}

void endFirebaseConnection(){
  //Quit Firebase and release all resources
  Firebase.end(firebaseData);
}

void reconnectFirebaseConnection(){
   endFirebaseConnection();
   connectFirebase();
}

/**
 * Get data from Firebase in string format
 */
String getFirebaseStringData(String tag){
  Firebase.getString(firebaseData, tag);
  return firebaseData.stringData();
}

/**
 * Get data from Firebase in json format
 */
String getFirebaseJsonData(String tag){
  Firebase.getJSON(firebaseData, tag);
  delay(1000);
  return firebaseData.jsonData();
}

/**
 * Get actual date using NTP server
 * @return actual date as string
 */
String getActualDate(){
  char* servidorNTP = "a.st1.ntp.br";
  int fusoHorario = -10800;
  
  WiFiUDP ntpUDP;
  NTPClient timeClient(ntpUDP, servidorNTP, fusoHorario);
  
  timeClient.begin();
  String date = "";
  while(true){
    timeClient.update();
    int year = timeClient.getYear();
    //Serial.println(year);
    date = timeClient.getFullFormattedTime();

    if(year >= 2019) break;
    delay(500);
  }
     
  timeClient.end();
  return date;
}

/**
 * Print welcome message in LCD Display
 */
void printMessageLCD(String firstLineMsg, String secondLineMsg){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print(firstLineMsg);
  lcd.setCursor(0,1);
  lcd.print(secondLineMsg);
  //lcd.noBacklight();
}

/**
 * Get distance using ultrasonic sensor
 * @return distance_value as a String
 **/
String getDistance(){
  long microsec = ultrasonic.timing();
  return String(ultrasonic.convert(microsec, Ultrasonic::CM), 2);
}


/**
 * Get environment noise from KY-038 sensor
 * @return ky038_value as a String
 **/
String getKY38Value(){
  return String(analogRead(KY38_PIN), 2);
}

/**
 * Get environment CO2 concentration from MQ7 sensor
 * @return mq7_value as a String
 **/
String getMQ7Value(){
  float sensorValue = analogRead(MQ7_SENSOR);
  return String(sensorValue, 2);
}

/**
 * Get environment temperature from DHT22
 * @return temperature as a String
 **/
String getTemperature(){
  return String(dht.readTemperature(), 2);
}

/**
 * Get environment humidity from DHT22
 * @return humidity as a String
 **/
String getHumidity(){
  return String(dht.readHumidity(), 2);
}

/**
 * Get lumens value from LDR sensor 
 * @return lumens as integer
 **/
int getLumenValue(){
  /* http://eletronicaapolo.com.br/novidades/o-guia-completo-das-lanternas/ */
  int value = analogRead(LDR_SENSOR) * -1;
  value = map(value, -4095, 0, 1, 850);
  return value;
}

/**
 * My beep function 
 * 
 * @param frequency
 * @param duration
 * @param times
 **/
void beep(int freq, int duration, int times){
  for(int i = 0; i < times; i++){
    int channel = 0, resolution = 8;
    ledcSetup(channel, freq, resolution);
    ledcAttachPin(BUZZER_PIN, channel);
    ledcWriteTone(channel, freq);
    delay(duration);
    ledcDetachPin(BUZZER_PIN);
    delay(500);
  }
}

/**
 * LEDs control function
 * @param color [red, yellow, green]
 * @param status [on, off]
 **/
void ledsControl(String color, String lStatus){
  if(String(lStatus) == "off"){
    digitalWrite(LED_RED, LOW);
    digitalWrite(LED_YELLOW, LOW);
    digitalWrite(LED_GREEN, LOW);
  }else if(String(lStatus) == "on"){
    if(String(color) == "red"){
      digitalWrite(LED_RED, HIGH);
      digitalWrite(LED_YELLOW, LOW);
      digitalWrite(LED_GREEN, LOW);
    }else if(String(color) == "yellow"){
      digitalWrite(LED_RED, LOW);
      digitalWrite(LED_YELLOW, HIGH);
      digitalWrite(LED_GREEN, LOW);
    } else if(String(color) == "green"){
      digitalWrite(LED_RED, LOW);
      digitalWrite(LED_YELLOW, LOW);
      digitalWrite(LED_GREEN, HIGH);
    }
  }
}

void printHelpFunc(){
  long microsec = ultrasonic.timing();
  
  Serial.print("KY-38 Value: ");
  int noise = analogRead(KY38_PIN);
  Serial.print(noise);
  
  float sensorValue = analogRead(MQ7_SENSOR);
  Serial.print(" MQ7 Value: ");
  Serial.print(sensorValue);
  
  // Get lumens value
  int value = analogRead(LDR_SENSOR);
  Serial.print(F(" Lumens: "));
  Serial.print(getLumenValue());
  Serial.print(F("lm "));

  Serial.print(F("Humidity: "));
  Serial.print(dht.readHumidity());
  Serial.print(F("%  Temperature: "));
  Serial.print(dht.readTemperature());
  Serial.print(F("°C "));

  Serial.print(F("Distance: "));
  Serial.print(ultrasonic.convert(microsec, Ultrasonic::CM));
  Serial.println(F("cm"));
}

void processActuators(){
  String json = getFirebaseJsonData("envs/great/lab10/info/actuators");
  //Serial.println(json);
  
  StaticJsonBuffer<300> jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(json);

  const char* color = root["led"]["color"];
  const char* lStatus = root["led"]["status"];
  ledsControl(String(color), String(lStatus));
  
  const char* bStatus = root["buzzer"]["status"];
  if(String(bStatus) == "pending"){
    int duration = root["buzzer"]["duration"];
    int freq = root["buzzer"]["freq"];
    int times = root["buzzer"]["times"];
    
    beep(freq, duration, times);
    Firebase.setString(firebaseData, "envs/great/lab10/info/actuators/buzzer/status", "done");
  }
}

/**
 * Main loop.
 */
int cont = 60;
void loop() {
  if(cont == 0) cont = 60;
  
  processActuators();
  
  /* Checking broadcast messages... */
  int packetSize = Udp.parsePacket();
  if (packetSize){
    // Receive incoming UDP packets
    Serial.printf("Received %d bytes from %s, port %d\n", packetSize, Udp.remoteIP().toString().c_str(), Udp.remotePort());
    int len = Udp.read(incomingPacket, 255);
    if (len > 0) incomingPacket[len] = 0;
    Serial.printf("UDP packet contents: %s\n", incomingPacket);

    // Send back a reply, to the IP address and port we got the packet from
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.print("Hi. I am here! Saving data in: greatlabmonitor.firebaseio.com");
    Udp.endPacket();
  }

  if(cont == 60){
    printMessageLCD("GREat LabMonitor", "Monitoring...");
    saveSensorData(getActualDate(), getTemperature(), getHumidity(), getMQ7Value(), String(getLumenValue()), getDistance(), getKY38Value());
  }

  cont--;
  delay(1000);
}
