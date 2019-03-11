package com.example.park.blenavigation;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

/**************************************************************************************************
 #  IT 집중교육 2
 #  Prof. 노병희
 #  Team : 7조
 #  Author : 박종인
 #  Explanation : 본 코드는 시각 장애인용 실내 내비게이션 안드로이드 애플리케이션에서
 #                앱을 실행시키고 나서 Main Activity에 대한 소스 코드이다.
****************************************************************************************************/

public class MainActivity extends Activity implements Button.OnClickListener {

    private static final int REQUEST_CODE = 1234;   // Google API 요청 코드
    private TextToSpeech tts;                       // TTS API를 사용하기 위한 변수

    // state 변수 정리
    // -1: 현재 위치 지정 전
    // 0: 안내 시작하고 현재 위치를 받아온 상태
    // 1: 목적 위치까지 지정됨.
    private int state = -1;

    private String departure;       // 출발 위치
    private String currentLocation; // 현재 위치
    private String destination;     // 도착 위치

    public Button btnRetrieve;      // 버튼 멤버 변수 선언
    public Button btnSoundRecog;    // 음성 인식 버튼 멤버 변수
    public Button btnPutTemp;       // 온도를 DB에 넣어주기 위한 버튼 멤버 변수
    public EditText textTemperature;// 온도를 받아오기 위한 텍스트 멤버 변수
    public TextView textViewData;   // 텍스트 뷰 멤버 변수 선언
    public TextView speehData;      // 스피치 텍스트 변수
    public Handler handler;         // 이벤트 핸들러 객체
    ArrayList<String> matches_text;

    // Main
    public MainActivity() {
        handler = new Handler();
    }

    // 초기 변수 설정
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                             // MainActivity의 화면을 activity_main으로 설정

        btnRetrieve = (Button) findViewById(R.id.btnRetrieve);              // Mobius에서 값 가져올 때 사용하는 버튼
        btnSoundRecog = (Button) findViewById(R.id.btnSoundRecog);          // 음성인식 실행 버튼
        btnPutTemp = (Button) findViewById(R.id.btnPutTemp);                // Mobius에 값을 넣어줄 때 사용하는 버튼
        textTemperature = (EditText) findViewById(R.id.textTemperature);    // Mobius에 넘겨준 값 표시해주는 text
        textViewData = (TextView) findViewById(R.id.textViewData);          // Mobius에서 받아온 값 표시해주는 text
        speehData = (TextView)findViewById(R.id.speech);                    // 음성인식 관련 텍스트 뷰

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

        // 각 버튼 리스터 초기화
        btnRetrieve.setOnClickListener(this);
        btnSoundRecog.setOnClickListener(this);
        btnPutTemp.setOnClickListener(this);

        // 앱을 키면 자동으로 음성 안내 UI를 실행한다.
        soundRecog();
    }

    // 버튼 클릭했을 때의 리스너
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnRetrieve: {            // Mobius에 값 넘겨줄 때
                RetrieveRequest req = new RetrieveRequest();
                textViewData.setText("");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        final String tmsg = dbParsing(msg);
                        handler.post(new Runnable() {
                            public void run() {
                                textViewData.setText("최근 위치 조회\r\n\r\n" + tmsg);
                            }
                        });
                    }
                });
                req.start();
                break;

            } case R.id.btnSoundRecog: {         // 음성인식 버튼
                soundRecog();
                break;

            } case R.id.btnPutTemp: {            // Mobius에 값 넘겨줄 때 버튼
                String temp = "";
                temp = textTemperature.getText().toString();
                ControlRequest req = new ControlRequest(temp);
                req.setReceiver(new IReceived() {
                    @Override
                    public void getResponseBody(final String msg) {
                        final String tmsg = dbParsing(msg);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textViewData.setText("현재 위치 제어\r\n\r\n" + tmsg);
                            }
                        });
                    }
                });
                req.start();
                break;
            }
        }
    }

    // 모비우스에 요청하는 부분
    class RetrieveRequest extends Thread{
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());

        private IReceived receiver;
        int responseCode=0;
        // ae_name과 containter_name 설정해주는 부분.
        private String ae_name = "edu4";
        private String container_name = "ble";

        // 생성자. class를 만들 때 직접 ae와 container를 설정해 줄 수 있다.
        public RetrieveRequest(String aeName, String containerName) {
            this.ae_name = aeName;
            this.container_name = containerName;
        }

        // 생성자
        public RetrieveRequest() {

        }

        //Thread 사용 할 때 핸들링하기 위함
        public void setReceiver(IReceived handler) {
            this.receiver = handler;
        }

        @Override
        public void run() {
            try {
                // 보낼 주소를 조립한다.
                String sb = MobiusConfig.MOBIUS_ROOT_URL + "/" +
                        ae_name + "/" +
                        container_name + "/" +
                        "latest";
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection(); // HTTP 연결
                conn.setRequestMethod("GET");                                       // GET 방식으로 요청을 보냄
                conn.setDoInput(true);                                              // true: 서버로부터 응답을 받겠다는 의미. false: 응답을 받지 않음
                conn.setDoOutput(false);                                            // true로 설정하면 내부적으로 POST 방식으로 변경됨

                // 헤더값 설정
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

    // 모비우스에 데이터를 넣는 부분
    class ControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;

        private String ae_name = "edu4";
        private String container_name = "ble";

        public ContentInstanceObject instance;  // XML 값을 가지게 됨

        public ControlRequest(String comm) {
            instance = new ContentInstanceObject();
            instance.setAeName(ae_name);
            instance.setContainerName(container_name);
            instance.setContent(comm);
        }

        public void setReceiver(IReceived handler) {
            this.receiver = handler;
        }

        public void run() {
            try{
                // 주소를 조립한다.
                String sb = MobiusConfig.MOBIUS_ROOT_URL + "/" +
                        ae_name + "/" +
                        container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);                       // true: 서버로부터 응답을 받겠다는 의미. false: 받지 않음
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);     //리다이렉션 자동으로 못하게 함

                // 헤더값 설정
                conn.setRequestProperty("Accept","application/xml");
                conn.setRequestProperty("Content-Type","application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("X-M2M-RI","123sdfgd45");
                conn.setRequestProperty("X-M2M-Origin","S20170717074825768bp2l");

                String reqContent = instance.makeBodyXML(); // 보낼 XML 값들을 조합함
                conn.setRequestProperty("Content-Length",String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine;
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }
                if(receiver != null) {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    // 음성인식 이벤트 핸들링 코드
    // 음성 안내 UI의 변화를 감지하면 이 코드가 실행된다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // state가 -1일 때, 안내를 인식하여 음성안내 시작 단계로 들어선다.
            if((matches_text.get(0).contains("안내"))&&(state == -1)) {
                tts.speak("음성 내비게이션을 시작하셨습니다.",TextToSpeech.QUEUE_ADD,null);
                state = 0;
                RetrieveRequest req = new RetrieveRequest("edu4","ble");
                textViewData.setText("");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        final String tmsg = dbParsing(msg);
                        departure = tmsg;
                        currentLocation = tmsg;
                        handler.post(new Runnable() {
                            public void run() {
                                textViewData.setText("현재위치\r\n\r\n" + tmsg);
                            }
                        });
                    }
                });
                req.start();
                speehData.setText("목적지를 정해 주세요.");
                tts.speak("목적지를 정해 주세요.",TextToSpeech.QUEUE_ADD,null);
                soundRecog();
            }
            // state가 0일 때는 도착하고 싶은 위치를 정해준다.
            else if((matches_text.get(0).contains("밖")||matches_text.get(0).contains("바깥")||matches_text.get(0).contains("외부")||matches_text.get(0).contains("건물"))&&(state == 0)) {
                state = 1;
                speehData.setText("건물 밖 으로 안내를 시작합니다.");
                tts.speak("건물 밖 으로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "0";
            }
            else if(matches_text.get(0).contains("남자")&&(state == 0)) {
                state = 1;
                speehData.setText("남자화장실로 안내를 시작합니다.");
                tts.speak("남자화장실로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "10";
            }
            else if ((matches_text.get(0).contains("여자")||matches_text.get(0).contains("녀자"))&&(state == 0)) {
                state = 1;
                speehData.setText("여자화장실로 안내를 시작합니다.");
                tts.speak("여자화장실로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "12";
            }
            else if (((matches_text.get(0).contains("101")||(matches_text.get(0).contains("백일")))&&(state == 0))) {
                state = 1;
                speehData.setText("101호로 안내를 시작합니다.");
                tts.speak("101호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "9";
            }
            else if (((matches_text.get(0).contains("106")||(matches_text.get(0).contains("백육")))&&(state == 0))) {
                state = 1;
                speehData.setText("106호로 안내를 시작합니다.");
                tts.speak("106호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "7";
            }
            else if ((matches_text.get(0).contains("103")||(matches_text.get(0).contains("백삼")))&&(state == 0)) {
                state = 1;
                speehData.setText("103호로 안내를 시작합니다.");
                tts.speak("103호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "15";
            }
            else if ((matches_text.get(0).contains("108")||(matches_text.get(0).contains("백팔")))&&(state == 0)) {
                state = 1;
                speehData.setText("108호로 안내를 시작합니다.");
                tts.speak("108호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "16";
            }
            else if ((matches_text.get(0).contains("109")||(matches_text.get(0).contains("백구")))&&(state == 0)) {
                state = 1;
                speehData.setText("109호로 안내를 시작합니다.");
                tts.speak("109호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "19";
            }
            else if ((matches_text.get(0).contains("105")||(matches_text.get(0).contains("백오")))&&(state == 0)) {
                state = 1;
                speehData.setText("105호로 안내를 시작합니다.");
                tts.speak("105호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "22";
            }
            else if ((matches_text.get(0).contains("110")||(matches_text.get(0).contains("백십")))&&(state == 0)) {
                state = 1;
                speehData.setText("110호로 안내를 시작합니다.");
                tts.speak("110호로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "23";
            }
            else if (matches_text.get(0).contains("정수기")&&(state == 0)) {
                state = 1;
                speehData.setText("정수기로 안내를 시작합니다.");
                tts.speak("정수기로 안내를 시작합니다.",TextToSpeech.QUEUE_ADD,null);
                destination = "4";
            }
            // 음성을 제대로 인식하지 못했다면, 제대로 인식하지 못하였음을 알려준다.
            else {
                speehData.setText("제대로 인식하지 못하였습니다.");
                tts.speak("제대로 인식하지 못하였습니다.",TextToSpeech.QUEUE_ADD,null);
            }

            // 원하는 목적지를 받았다면, Map을 보여준다.
            if(state == 1) showMap();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Mobius에 데이터를 넘겨줄 때 보내줘야 하는 Message설정 부분. 건드리지 않는 것을 권함.
    class ContentInstanceObject {
        private String aeName = "";
        private String containerName = "";
        private String content = "";

        public void setAeName(String value) {
            this.aeName = value;
        }
        public void setContainerName(String value) {
            this.containerName = value;
        }
        public void setContent(String value) {
            this.content = value;
        }

        public String makeBodyXML() {
            String xml = "";

            // XML 메시지를 보낼 때 메시지를 조립하는 부분이다.
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             + "<m2m:cin "
             + "xmlns:m2m=\"http://www.onem2m.org/xml/protocols\" "
             + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
             + "<con>" + content + "</con>"
             + "</m2m:cin>";

            return xml;
        }
    }

    // 인터넷 연결 확인
    public  boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!=null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
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

    // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    // 음성 인식 UI를 나타나게 하는 코드
    public void soundRecog() {
        if(isConnected()){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            startActivityForResult(intent, REQUEST_CODE);
        }
        else{
            Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
        }
    }

    // 화면을 전환시켜 주는 코드
    public void showMap() {
        Intent intent = new Intent(getApplicationContext(),MapActivity.class);
        intent.putExtra("departure",Integer.parseInt(departure));
        intent.putExtra("destination",Integer.parseInt(destination));
        startActivity(intent);
        state = -1;

    }

}


