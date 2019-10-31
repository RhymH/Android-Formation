
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include <LCD.h>

#include <SoftwareSerial.h>
#include <Keypad.h>

const byte ROWS = 4; 
const byte COLS = 3; 

char hexaKeys[ROWS][COLS] = {
  {'1', '2', '3'},
  {'4', '5', '6'},
  {'7', '8', '9'},
  {'*', '0', '#'}
};

byte rowPins[ROWS] = {8, 7, 6, 5}; 
byte colPins[COLS] = {4, 3, 2}; 

Keypad customKeypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);

#define I2C_ADDR 0x3F
#define RS_PIN 0
#define RW_PIN 1
#define EN_PIN 2
#define BACKLIGHT_PIN 3
#define D4_PIN 4
#define D5_PIN 5
#define D6_PIN 6
#define D7_PIN 7


#define BTPIN_STATE 12

LiquidCrystal_I2C  lcd(I2C_ADDR,EN_PIN,RW_PIN,RS_PIN,D4_PIN,D5_PIN,D6_PIN,D7_PIN);

SoftwareSerial myBTSerialLink(10, 11);

String msg;

String keypadMsg = "";

int BTConnectState = LOW;

class CmdClass
{

  public:
    String action;
    String parameters[10];
    int parametersLength;
    
    CmdClass(String action) {
      this->action = action;
      this->parametersLength = 0;
    }
    void addParameter(String param) {
      this->parameters[this->parametersLength] = param;
      this->parametersLength++;
    }
};

void setup()
{
  // activate LCD module
  lcd.begin(20, 4); // for 16 x 2 LCD module
  lcd.setBacklightPin(3,POSITIVE);
  lcd.setBacklight(HIGH);

  Serial.begin(115200);
  Serial.println("\nBluetooth Scanner");
  
  myBTSerialLink.print("AT+RESET");
  
  myBTSerialLink.begin(9600);

  clearAndPrintLine(0,"BT Connection");

  Serial.println("HC05 is ready");
  Serial.println("Connect the HC05 to an Android device to continue");


  while ( BTConnectState == LOW ) {
    BTConnectState = digitalRead(BTPIN_STATE);
    delay(100);
  }
  Serial.println("HC05 is now connected");
  
  clearAndPrintLine(0,"BT Connected");
}

void loop()
{
  // Manage Bluetooth incoming Message :
  if (myBTSerialLink.available()) {
    byte data = myBTSerialLink.read();
    char dataChar = char(data);
    if ( dataChar != ';') {
      msg += dataChar;
    } else {
      manageCmd(msg);
      msg = "";
    }
  }

  // Manage Incoming Serial Message :
  if( Serial.available() ) {
    String incoming = Serial.readString();
    myBTSerialLink.print(incoming);
  }

  // Manage KeyPad:
  char customKey = customKeypad.getKey();
  if (customKey){
    Serial.println(customKey);
    if ( customKey == '#') {
      Serial.println("Send customKey result:"+keypadMsg);
      Serial.println(keypadMsg);
      myBTSerialLink.print(keypadMsg + ";");
      keypadMsg = "";
    } else {
      Serial.println("increment customKey");
      keypadMsg.concat(customKey);
    }
  }

  // Display Connection state Changes:
  int currentBTState = digitalRead(BTPIN_STATE);
  if ( currentBTState != BTConnectState ) {
    BTConnectState = currentBTState;
    if ( BTConnectState == LOW ) {
      Serial.println("HC05 is now disconnected");
      clearAndPrintLine(0,"BT Disconnected");
    } else {
      Serial.println("HC05 is now connected");
      clearAndPrintLine(0,"BT Connected");
    }
    Serial.println("");
  }
  
  //delay(1000);
}

void clearAndPrintLine(int line,String str) {
  lcd.setCursor(0,line);
  lcd.print("                     ");
  lcd.setCursor(0,line);
  lcd.print(str);
}

#define  CMD_CLEAR_SCREEN   "CLRSCR"
#define  CMD_PRINT          "PRINT"

void manageCmd(String cmdStrg) {
  CmdClass *cmd = splitStringToArray(cmdStrg);

  Serial.println("Action="+cmd->action);
  clearAndPrintLine(1,"Action="+cmd->action);
  /*
  if ( cmd->action.equals(CMD_CLEAR_SCREEN) ) {
    Serial.println("COMMANDE IS CLEAR SCREEN");
    lcd.clear();
  } else if ( cmd->action.equals(CMD_PRINT) ) {
    Serial.println("COMMANDE IS PRINT");
    int x = cmd->parameters[0].toInt();
    int y = cmd->parameters[1].toInt();
    Serial.println("Print: "+cmd->parameters[2]);
    lcd.setCursor(x,y); 
    lcd.print(cmd->parameters[2]);
  }
  */
}

// "print:1,2,coucou;"
CmdClass * splitStringToArray(String cmd) {
  int cmdEndPos = cmd.indexOf(':')+1;

  if ( cmd.substring(cmd.length()-1) == ";" ) {
    cmd =  cmd.substring(0, cmd.length()-1);
  }

  String action = cmd.substring(0, cmdEndPos-1);
  
  CmdClass *newCmdInfos = new CmdClass(action);
  
  int nbParam = 1;
  int delimParamPos;
  String parameters = cmd.substring(cmdEndPos);
  while( (delimParamPos = parameters.indexOf(',')) > 0 ) {
    String newParam = parameters.substring(0, delimParamPos);
    newCmdInfos->addParameter( newParam );
    parameters = parameters.substring(delimParamPos+1);
    nbParam++;
  }
  // Store last parameter
  newCmdInfos->addParameter( parameters );

  return newCmdInfos;
}

void Log(char *str,int value) {
  char buf[50];
  sprintf(buf, str, value);
  Serial.println(buf);
}


