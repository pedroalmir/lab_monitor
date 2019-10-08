#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

#define LED_RED 0
#define LED_YELLOW 2
#define LED_GREEN 15
#define LDR_SENSOR 34

LiquidCrystal_I2C lcd(0x27,16,2);  // Set the LCD address to 0x27 for a 16 chars and 2 line display

/**
 * LabMonitor Hardware v1 20191007
 * Pinout:
 *       Used -|3.3V       GND|- Used
 *            -|EN      GPIO23|- 
 *            -|GPIO36  GPIO22|- 
 *            -|GPIO39     TX0|-
 * LDR_SENSOR -|GPIO34     RX0|-
 *            -|GPIO35  GPIO21|- 
 *            -|GPIO32     GND|- Used
 *            -|GPIO33  GPIO19|- 
 *            -|GPIO25  GPIO18|- 
 *            -|GPOI26  GPIO05|- I2C SCL
 *            -|GPIO27  GPIO17|-
 *            -|GPIO14  GPIO16|-
 *            -|GPIO12  GPIO04|- I2C SDA
 *    I2C GND -|GND     GPIO00|- LED_RED
 *            -|GPIO13  GPIO02|- LED_YELLOW
 *            -|GPIO09  GPIO15|- LED_GREEN
 *            -|GPIO10  GPIO08|-
 *            -|GPIO11  GPIO07|-
 * I2C VCC(5v)-|Vin(5v) GPIO06|-
 *  
 *  >> Please, don't change it!!!
 * 
 * Setup function
**/
void setup() {
  Serial.begin(9600); // Initialize serial communications
  
  /* Initializing the pins as in/output pins */
  pinMode(LDR_SENSOR, INPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);

  /* Initialize the lcd (SDA, SCL) */
  lcd.begin(4, 5);    
  lcd.setCursor(0,0);
  lcd.backlight();
}

void loop() {
  int value = analogRead(LDR_SENSOR);
  //String msg = "LDR: " + value;
  //lcd.clear();
  //lcd.print(msg);
  
  Serial.print("LDR: ");
  Serial.print(value);
  Serial.print(", Lumen: ");
  Serial.println(getLumenValue());
  delay(2000);
}

int getLumenValue(){
  /* http://eletronicaapolo.com.br/novidades/o-guia-completo-das-lanternas/ */
  int value = analogRead(LDR_SENSOR) * -1;
  value = map(value, -4095, 0, 1, 1500);
  return value;
}
