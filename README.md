infodio 개발자 가이드입니다.
============================

시작하기
--------
위치 트래킹 및 추천정보, TTS에 대한 사용 방법을 설명합니다.
이 문서는 Android Studio 기반으로 작성하였습니다.


준비 단계
---------
개발하기 위해서는 *.aar(libInfodio-v1.0.0.aar)의 infodio 라이브러리 및 GSON 라이브러리가 필요합니다.

1. *.aar 라이브러리 파일을 추가해 주세요
[File] > [Poject Structure] > [+]New Module 클릭 > import .JAR/.AAR Package 선택 > infodio 피일 선택 > [OK]
[Dependencies] > [+]Add 클릭> Module Dependency 클릭> infoido 라이브러리 선택 > [OK]

2. Gson 라이브러리를 추가해 주세요
[File] > [Poject Structure] > [Dependencies] > [+}Add 클릭 > Liblaly Dependency 쿨릭 > com.google.code.gson:gson:xx.xx.xx 선택 > [OK]

3. manifast 권한 추가
~~~~
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
~~~~

4. maifast matadata 추가
~~~~
<application
    ...
    >
    
    <meta-data
        android:name="emgram.kr.co.infodio.API_KEY"
        // 발급 받은 infodio key 추가
        android:value="621d02b390e*********************" />

   ...
</application>
~~~~

infodio 기능 사용 전
--------------------
infodio 기능을 사용하기 위해 infodio 객체를 생성합니다.

~~~~
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	...

        infodio = new Infodio(this, infodioReadyListener);

        registerTTSReceiver();

    }

    ...

    Infodio.InfodioReadyListener infodioReadyListener = new Infodio.InfodioReadyListener() {
        @Override
        public void onReady() {
	    // infodio를 사용할 준비가 되었습니다.
	    Log.d("sample", "onReady");

            startTracking();
        }

        @Override
        public void onFail(Exception e) {
	    Log.d("sample", "onFail");

            if(e!=null){
                e.printStackTrace();
            }
        }
    };

    ...
~~~~

위치 트래킹 사용
----------------
현재 위치 값을 받고, 추천 정보를 요청하기 시작합니다.
Location의 좌표를 이용하여 현재 위치를 표시할 수 있습니다.

~~~~
 private void startTracking(){
     // 위치 트래킹 시작
     infodio.startTracking(infodioLocationListener);

     ...
 }

 ...

 Infodio.InfodioLocationListener infodioLocationListener = new Infodio.InfodioLocationListener() {
     @Override
     public void onUpdatedLocation(final Location location, Exception e) {
         if(e!=null){
             e.printStackTrace();
             return;
         }

         Log.d("sample", "lat : " + location.getLatitude() + ", lng : " + location.getLongitude());

     }
};
~~~~


추천 정보
---------
위치 추적을 사용하면서 추천 정보를 받을 수 있습니다.
TourData를 통해 추천정보 Title 및 위치, 설명 등을 사용할 수 있습니다.

~~~~
    ...
    private void startTracking(){
        ...

        // 추천 정보 받기 위해 리스너 등록
        infodio.setRecommendListener(infodioRecommendListener);

        ...
    }
    ...

    // 추천 정보 리스너
    Infodio.InfodioRecommendListener infodioRecommendListener = new Infodio.InfodioRecommendListener() {
        @Override
        public void onRecommend(final TourData tourData, Exception e) {

            if(e!=null){
                e.printStackTrace();
                return;
            }

            Log.d("sample", "recom title : " + tourData.getTitle());

        }
    };
~~~~

TTS
----
TTS의 재생 관련 정보들을 받아 표시하거나 플레이 상태를 제어 할 수 있습니다.
TTS 재생 관련 이벤트를 수신 받기 위해 LocalBroadcastManager를 통해 Reciever를 등록 합니다.
TTSPlayInfo를 통해 play관련 정보를 얻을 수 있습니다.

~~~~
    ...
    // tts 이벤트 수신을 위한 receiver 등록
    private void registerTTSReceiver(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();

        filter.addAction(Infodio.Action_PlayerPause);
        filter.addAction(Infodio.Action_PlayerPrepare);
        filter.addAction(Infodio.Action_PlayerStart);
        filter.addAction(Infodio.Action_PlayerStop);
        filter.addAction(Infodio.Action_PlayerTicker);

        lbm.registerReceiver(broadcastReceiver, filter);
    }

    ...

    // tts 이벤트 수신 Receiver
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // TTS 정보 얻음
            final TTSPlayInfo ttsPlayInfo = intent.getParcelableExtra(Infodio.Intent_Key_Object);

            if(action.equals(Infodio.Action_PlayerPause)){
                Log.d("sample", "play pause");

            }else if(action.equals(Infodio.Action_PlayerTicker)){
                Log.d("sample", "play ticker : " + ttsPlayInfo.getPosition());

            }else if(action.equals(Infodio.Action_PlayerPrepare)){
                Log.d("sample", "play prepare");

            }else if(action.equals(Infodio.Action_PlayerStart)){
                Log.d("sample", "play start : "+ ttsPlayInfo.getContentTitle());

            }else if(action.equals(Infodio.Action_PlayerStop)){
                Log.d("sample", "play stop");

            }

        }
    };
    ...
~~~~