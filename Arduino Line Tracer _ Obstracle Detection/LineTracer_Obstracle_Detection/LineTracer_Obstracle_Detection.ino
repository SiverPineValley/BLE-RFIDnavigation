int sensor = 9;        // 라인트레이서 센서을 9번 핀에 연결
int Buzzer = 7;        // Buzzer를 7번 핀에 연결
int distance =0;
int buzzer = 2;
int echoPin = 12;
int trigPin = 13;

void setup(){
  Serial.begin(9600);
  pinMode(Buzzer, OUTPUT);  // Buzzer는 '출력' 
  pinMode(sensor, INPUT);   // 라인트레이서 센서는 '입력'
  pinMode(buzzer, OUTPUT);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
}
void loop(){
    int val = digitalRead(sensor);  // 센서 값을 읽어와서
    Serial.println(val);
    if (val == HIGH){       // 검정색 주행선 위에 있으면
       noTone(7);           // Buzzer에서 소리가 나지 않는다
       delay(100);
    } else{                 // 검정색 주행선을 벗어나면
       tone(7,220);         // Buzzer에서 소리가 난다
       delay(100);
    }
    
    float duration,distance;

    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    
    duration = pulseIn(echoPin, HIGH);
    distance = ((float)(340*duration)/10000)/2;

    delay(500);

    if (distance < 30) {
        tone(Buzzer,1000,500);
        delay(200);
        noTone[Buzzer];
        digitalWrite(Buzzer,LOW);
        delay(distance);
    }
}
