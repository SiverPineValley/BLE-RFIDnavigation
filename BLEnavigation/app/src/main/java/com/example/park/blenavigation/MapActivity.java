package com.example.park.blenavigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

import com.example.park.blenavigation.shortestPath;

import static android.speech.tts.TextToSpeech.ERROR;

/**************************************************************************************************
 #  IT 집중교육 2
 #  Prof. 노병희
 #  Team : 7조
 #  Author : 박종인
 #  Explanation : 본 코드는 시각 장애인용 실내 내비게이션 안드로이드 애플리케이션에서
 #                실제 맵에 대한 정보를 보여주는 코드이다.
 ****************************************************************************************************/

public class MapActivity extends AppCompatActivity {

    // 음성인식 및 음성 안내
    private TextToSpeech tts;

    // 방향 센서 변수
    public SensorManager sensorManager;
    public Sensor sensor;

    shortestPath sp = new shortestPath();                           // Shortest Object - 최단 경로를 구한다.
    MyView myview;                                                  // MyView Object - 맵 위에 경로를 그려준다.
    int departure, destination, location;                           // 각각 출발 위치, 도착 위치, 현재 위치의 좌표 값을 기진다.
    int current;                                                    // 현재 위치 노드 번호 값
    int bcurrent = -1;                                              // 이전 위치 노드 번호 값
    int next;                                                       // 다음 위치 노드 번호 값
    int cDirec;                                                     // 현재 방향       0: 동, 1: 서, 2: 남, 3: 북
    int nDirec;                                                     // 진행해야할 방향  0: 동, 1: 서, 2: 남, 3: 북
    int state = 0;                                                  // 상태를 나타낸다. 상태가 1일 때만 안내를 시작한다. 처음부터 안내가 시작되는 것을 방지.
    public int[] path;                                              // 최단 경로
    private int[] rfidNum = { 24, 3, 6, 8, 10, 12, 14, 18, 21 };    // 비콘 위치에 따른 실제 노드 번호 정보
    private int[][] pointLocation = { {198, 295}, {198, 184}, {198,123}, {111,123}, {165,65}, {225,65}, {268,123}, {330,123}, {360,123} }; // First Point Location

    // 실제 노드 좌표 값
    private int[][] rfidLocation = { {198, 295}, {198, 265}, {198, 223}, {198, 184}, {215, 184}, {198, 152}, {198, 123}, {111, 156} ,{111, 123} ,{111, 98} ,{198, 65} ,{212, 65},
                                    {225,65}, {233, 123}, {268,123}, {268, 98}, {268, 156}, {300, 123}, {330, 123}, {330, 156}, {345, 123}, {360,123}, {360, 98} ,{360, 156}, {198, 295} };

    // DP 값을 받아오기 위한 변수
    DisplayMetrics dm = new DisplayMetrics();

    // 이벤트 핸들러 객체
    public Handler handler;

    // Main
    public MapActivity() {
        handler = new Handler();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 이전 Activity에서 출발지와 도착지 정보를 가져온다.
        Intent intent = getIntent();
        departure = intent.getExtras().getInt("departure");
        departure = rfidNum[departure];
        if(departure == 0) departure =24;
        current = departure;
        bcurrent = current;
        destination = intent.getExtras().getInt("destination");

        // MapActivity의 화면을 activity_map으로 설정
        setContentView(R.layout.activity_map);

        // Map 위에 Image를 그려 줄 MyView 변수에 대한 설정
        ImageView im=(ImageView) findViewById(R.id.paa);
        myview = (MyView)findViewById(R.id.myView);

        // 화면에 맞는 DP 값을 불러오는 설정
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // 최초 최단 경로를 구한다.
        path = sp.Dijkstra(sp.graph, departure, destination);

        //Sensor Manager 변수
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // 방향 센서 값에 대한 Listener 변수 및 이벤트 핸들러이다.
        SensorEventListener mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] v = event.values;

                switch(event.sensor.getType()) {
                    case Sensor.TYPE_ORIENTATION:
                        if( ( v[0] >= 340 ) || ( v[0] < 60 ) ) cDirec = 0;
                        else if( ( v[0] >= 60 ) && ( v[0] < 155 ) ) cDirec = 2;
                        else if( ( v[0] >= 155 ) && ( v[0] < 260 ) ) cDirec = 1;
                        else cDirec = 3;

                        //x 축 : 북 0도, 동쪽 90도
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        //방향센서 리스너를 등록한다.
        sensorManager.registerListener(mListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);

        // TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    // 읽는 속도
                    tts.setSpeechRate(1.0f);
                }
            }
        });

        tts.speak("음성 내비게이션을 시작합니다.",TextToSpeech.QUEUE_ADD,null);

        // 최초 경로를 맵에 그려준다.
        myview.drawNewPath(path, (int)dm.density);

        // 음성 안내 Thread를 실행시킨다.
        navigation navi = new navigation();
        navi.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    // 모비우스에 요청하는 부분
    class RetrieveRequest extends Thread{
        private final Logger LOG = Logger.getLogger(MainActivity.RetrieveRequest.class.getName());

        private MainActivity.IReceived receiver;
        int responseCode=0;
        // ae_name과 containter_name 설정해주는 부분.
        private String ae_name = "edu4";
        private String container_name = "rfid";

        // 생성자. class를 만들 때 직접 ae와 container를 설정해 줄 수 있다.
        public RetrieveRequest(String aeName, String containerName) {
            this.ae_name = aeName;
            this.container_name = containerName;
        }

        // 생성자
        public RetrieveRequest() {

        }

        //Thread 사용 할 때 핸들링하기 위함
        public void setReceiver(MainActivity.IReceived handler) {
            this.receiver = handler;
        }

        @Override
        public void run() {
            try {
                String sb = MainActivity.MobiusConfig.MOBIUS_ROOT_URL + "/" +
                        ae_name + "/" +
                        container_name + "/" +
                        "latest";
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection(); // HTTP 연결
                conn.setRequestMethod("GET");                                       // GET 방식으로 요청을 보냄
                conn.setDoInput(true);                                              // true: 서버로부터 응답을 받겠다는 의미. false: 응답을 받지 않음
                conn.setDoOutput(false);                                            // true로 설정하면 내부적으로 POST 방식으로 변경됨

                // 정해진 포맷에 맞춰 설정
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "ae_test"); //"S20170717074825768bp2l");
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                responseCode = conn.getResponseCode();

                String strResp = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String strLine = "";
                while ((strLine = in.readLine()) != null) {     // 한 줄씩 데이터를 읽음
                    strResp += strLine;                         // 한 줄씩 읽은 데이터를 하나의 문자열로 합침
                }

                if ( strResp != "" ) {
                    receiver.getResponseBody(strResp);
                }

                if (receiver != null) {
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();
            } catch(Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }

    }

    // 실제 음성 안내에 대한 Thread 클래스
    class navigation extends Thread {

        // 음성 내비게이션을 위한 코드
        @Override
        public void run() {
            try {
                while (rfidLocation[current] != rfidLocation[destination]) {

                    int[] npath;

                    try {
                        Thread.sleep(1000);
                        getLocation();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    current = location;

                    if (bcurrent != current) {

                        npath = sp.Dijkstra(sp.graph, current, destination);

                        boolean isFind = true;
                        int i = 0;

                        while (isFind) {
                            if (npath[i] == current) isFind = false;
                            else {
                                i++;
                            }
                        }
                        next = npath[i - 1];
                        if(state == 1) naviComment();
                        if(state == 0) state = 1;
                        myview.drawNewPath(npath, (int) dm.density);
                        bcurrent = current;

                    }
                }
            }
            catch(Exception e) {
            }
            tts.speak("목적지에 도착했습니다. 음성 안내를 종료합니다.",TextToSpeech.QUEUE_ADD,null);
            myview.rewind();

        }

        // 현재 rfid 위치를 받아옵니다.
        public void getLocation() {
            MapActivity.RetrieveRequest req = new MapActivity.RetrieveRequest();
            req.setReceiver(new MainActivity.IReceived() {
                public void getResponseBody(final String msg) {
                    final String loc = dbParsing(msg);
                    location = Integer.parseInt(loc);
                    if (location == 0) location = 24;

                }
            });
            req.start();
        }

        // 현재 위치에 대한 comment를 한다.
        public void naviComment() {
            // 0: 동, 1: 서, 2: 남, 3: 북
            nDirec = getNextDirec();

            if( ( cDirec == 0 ) && ( nDirec == 1 ) ) tts.speak("뒤쪽 입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 0 ) && ( nDirec == 2 ) ) tts.speak("오른쪽 입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 0 ) && ( nDirec == 3 ) ) tts.speak("왼쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 1 ) && ( nDirec == 0 ) ) tts.speak("뒤쪽 입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 1 ) && ( nDirec == 2 ) ) tts.speak("왼쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 1 ) && ( nDirec == 3 ) ) tts.speak("오른쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 2 ) && ( nDirec == 0 ) ) tts.speak("왼쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 2 ) && ( nDirec == 1 ) ) tts.speak("오른쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 2 ) && ( nDirec == 3 ) ) tts.speak("뒤쪽 입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 3 ) && ( nDirec == 0 ) ) tts.speak("오른쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 3 ) && ( nDirec == 1 ) ) tts.speak("왼쪽 방향입니다.",TextToSpeech.QUEUE_ADD, null);
            else if( ( cDirec == 3 ) && ( nDirec == 2 ) ) tts.speak("뒤쪽 입니다.",TextToSpeech.QUEUE_ADD, null);
            else tts.speak("직진하세요.",TextToSpeech.QUEUE_ADD, null);
        }

        // 다음으로 진행해야 할 방향에 대한 정보를 가져온다.
        public int getNextDirec() {
            int direc;

            if( (( current == 0 ) && ( next == 1 )) || (( current == 1 ) && ( next == 2 )) || (( current == 2 ) && ( next == 3 )) || (( current == 3 ) && ( next == 5 )) || (( current == 5 ) && ( next == 6 )) || (( current == 6 ) && ( next == 10 )) || (( current == 8 ) && ( next == 9 )) || (( current == 14 ) && ( next == 15 )) || (( current == 21 ) && ( next == 22 )) || (( current == 7 ) && ( next == 8 )) || (( current == 16 ) && ( next == 14 )) || (( current == 19 ) && ( next == 18 )) || (( current == 23 ) && ( next == 21 )) ) direc = 0; // 동
            else if( (( current == 10 ) && ( next == 6 )) || (( current == 6 ) && ( next == 5 )) || (( current == 5 ) && ( next == 3 )) || (( current == 3 ) && ( next == 2 )) || (( current == 2 ) && ( next == 1 )) || (( current == 1 ) && ( next == 0 )) || (( current == 9 ) && ( next == 8 )) || (( current == 8 ) && ( next == 7 )) || (( current == 15 ) && ( next == 14 )) || (( current == 14 ) && ( next == 16 )) || (( current == 18 ) && ( next == 19 )) || (( current == 22 ) && ( next == 21 )) || (( current == 21 ) && ( next == 23 )) ) direc = 1; // 서
            else if( (( current == 3 ) && ( next == 4 )) || (( current == 8 ) && ( next == 6 )) || (( current == 6 ) && ( next == 13 )) || (( current == 13 ) && ( next == 14 )) || (( current == 14 ) && ( next == 17 )) || (( current == 17 ) && ( next == 18 )) || (( current == 18 ) && ( next == 20 )) || (( current == 20 ) && ( next == 21 )) ) direc = 2; // 남
            else direc = 3; // 북

            return direc;
        }

    }

    // DB에서 온 데이터 Parsing. con 값을 String으로 return한다.
    public String dbParsing(String msg) {

        int start = msg.indexOf("<con>") + 5;
        int end = msg.indexOf("</con>");

        msg = msg.substring(start,end);

        return msg;
    }

    // Mobius ip 설정. 핸드폰과 노트북의 연결 AP가 같아야 하며, 노트북의 AP에 연결된 ip 주소로 설정해주어야 한다.
    public class MobiusConfig {
        public final static String MOBIUS_ROOT_URL = "http://192.168.30.61:7579/Mobius";
    }

    // 건드리지 않는 것을 추천.
    public interface IReceived {
        void getResponseBody(String msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

}