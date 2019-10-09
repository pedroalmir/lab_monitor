#include <Ultrasonic.h>

#include <Config.h>
#include <EasyBuzzer.h>

#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

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

DHT dht(DHTPIN, DHTTYPE);                     // Create DHT sensor considering port and type
LiquidCrystal_I2C lcd(0x27,16,2);             // Set the LCD address to 0x27 for a 16 chars and 2 line display
Ultrasonic ultrasonic(TRIGGER_PIN, ECHO_PIN); // Create Ultrasonic considering Trigger and Echo pins

float RS_gas = 0;
float ratio = 0;
float sensorValue = 0;
float sensor_volt = 0;
float R0 = -200.0;

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
 * https://randomnerdtutorials.com/esp32-dht11-dht22-temperature-humidity-sensor-arduino-ide/
 * https://github.com/evert-arias/EasyBuzzer
 * https://www.fernandok.com/2017/12/sensor-ultrassonico-com-esp32.html
 * https://www.arduinoecia.com.br/sensor-de-som-ky-038-microfone-arduino/
**/

void done() {
  Serial.println("Bee!");
}

void setup() {
  Serial.begin(9600); // Initialize serial communications
  
  /* Initializing the pins as in/output pins */
  pinMode(LDR_SENSOR, INPUT);
  pinMode(KY38_PIN, INPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);

  /* Initialize the lcd (SDA, SCL) */
  lcd.begin(I2C_SDA, I2C_SCL);    
  lcd.setCursor(0,0);
  lcd.backlight();
  lcd.clear();
  lcd.print("Lab. Monitor v1");

  EasyBuzzer.setPin(BUZZER_PIN);
  /* Start a beeping sequence. */
  EasyBuzzer.singleBeep(1000, 500, done);

  dht.begin();
  delay(5000);
  EasyBuzzer.stopBeep();
}

void loop() {
  long microsec = ultrasonic.timing();
  
  /* Always call this function in the loop for EasyBuzzer to work. */
  EasyBuzzer.update();

  Serial.print("KY-38 Value: ");
  Serial.print(analogRead(KY38_PIN));
  
  sensorValue = analogRead(MQ7_SENSOR);
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

  delay(3000);
}

String getTemperature(){
  return String(dht.readTemperature(), 2);
}


String getHumidity(){
  return String(dht.readHumidity(), 2);
}

int getLumenValue(){
  /* http://eletronicaapolo.com.br/novidades/o-guia-completo-das-lanternas/ */
  int value = analogRead(LDR_SENSOR) * -1;
  value = map(value, -4095, 0, 1, 1000);
  return value;
}

void beep(unsigned char delayms) { //creating function
  ledcWrite(BUZZER_PIN, 20); //Setting pin to high
  delay(delayms);             //Delaying
  ledcWrite(BUZZER_PIN ,0);  //Setting pin to LOW
  delay(delayms);             //Delaying 
}
