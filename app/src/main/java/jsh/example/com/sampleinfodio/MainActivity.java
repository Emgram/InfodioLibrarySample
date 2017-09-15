package jsh.example.com.sampleinfodio;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import co.kr.emgram.infodiolib.data.TTSPlayInfo;
import co.kr.emgram.infodiolib.data.TourData;
import co.kr.emgram.infodiolib.data.TourDatas;
import co.kr.emgram.infodiolib.export.Infodio;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = "MainActivity";

    Infodio infodio;

    TextView tv_ready_ck;
    TextView tv_loction;
    TextView tv_tourdatas_cnt;
    TextView tv_recom_title;

    //media
    TextView tv_mp_title;
    TextView tv_mp_position;
    TextView tv_mp_duration;
    Button bt_mp_start;
    Button bt_mp_pause;
    Button bt_mp_stop;
    SeekBar sb_mp;

    int position_change_user;           // postion 값 기록
    boolean check_user_change = false;  // 사용자에 의한 SeakBar 위치 변경 확인

    // 추천 정보 못받을 때 음성 재생을 위한 버튼
    Button bt_test_tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        initUi();

        infodio = new Infodio(getApplication(), infodioReadyListener);

        registerTTSReceiver();

    }

    private void initUi(){
        tv_ready_ck = (TextView)findViewById(R.id.tv_ready_ck);
        tv_loction = (TextView)findViewById(R.id.tv_location);
        tv_tourdatas_cnt = (TextView)findViewById(R.id.tv_tourdatas_cnt);
        tv_recom_title = (TextView)findViewById(R.id.tv_recom_title);

        tv_mp_title = (TextView)findViewById(R.id.tv_mp_title);
        tv_mp_position = (TextView)findViewById(R.id.tv_mp_position);
        tv_mp_duration = (TextView)findViewById(R.id.tv_mp_duration);
        sb_mp = (SeekBar)findViewById(R.id.sb_mp);
        bt_mp_start = (Button)findViewById(R.id.bt_mp_start);
        bt_mp_pause = (Button)findViewById(R.id.bt_mp_pause);
        bt_mp_stop = (Button)findViewById(R.id.bt_mp_stop);

        bt_mp_start.setOnClickListener(this);
        bt_mp_pause.setOnClickListener(this);
        bt_mp_stop.setOnClickListener(this);

        sb_mp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                check_user_change =fromUser;
                if(fromUser){
                    position_change_user = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if(check_user_change){
                    updatePlay(position_change_user);
                }
            }
        });


        // 추천정보 못 받을 때를 위한 버튼
        bt_test_tts = (Button)findViewById(R.id.bt_test_tts);
        bt_test_tts.setOnClickListener(this);
    }

    Infodio.InfodioReadyListener infodioReadyListener = new Infodio.InfodioReadyListener() {
        @Override
        public void onReady() {
            Log.d("sample", "onReady");

            tv_ready_ck.setText("onReady");

            startTracking();
        }

        @Override
        public void onFail(Exception e) {
            Log.d("sample", "onFail");

            tv_ready_ck.setText("onFail");

            if(e!=null){
                e.printStackTrace();
            }
        }
    };

    private void startTracking(){
        // 위치 추적 시작
        infodio.startTracking(infodioLocationListener);

        // 추천 정보 받기 위해 리스너 등록
        infodio.setRecommendListener(infodioRecommendListener);

        // 주변 투어 정보 요청
        infodio.requestTourDatas(tourDataListener);
    }

    // 위치 리스너(위치 추적)
    Infodio.InfodioLocationListener infodioLocationListener = new Infodio.InfodioLocationListener() {
        @Override
        public void onUpdatedLocation(final Location location, Exception e) {
            Log.d("sample", "onUpdateLocation");
            if(e!=null){
                e.printStackTrace();
                return;
            }
            Log.d("sample", "lat : " + location.getLatitude() + ", lng : " + location.getLongitude());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_loction.setText("lat : "+location.getLatitude()
                            +"\nlng : "+location.getLongitude());
                }
            });

        }
    };

    // 추천 정보 리스너
    Infodio.InfodioRecommendListener infodioRecommendListener = new Infodio.InfodioRecommendListener() {
        @Override
        public void onRecommend(final TourData tourData, Exception e) {

            if(e!=null){
                e.printStackTrace();
                return;
            }
            Log.d("sample", "recom title : " + tourData.getTitle());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_recom_title.setText("recom title : "+ tourData.getTitle());
                }
            });

        }
    };
    // 추천 더는 받을 수 없을 때 재생용도 - 라이브러리도 수정해서 적용(테스트용도)
    TourData testTourdata;

    // 투어 정보 리스너
    Infodio.InfodioNetworkListener<TourDatas> tourDataListener = new Infodio.InfodioNetworkListener<TourDatas>() {
        @Override
        public void onReceived(TourDatas tourDatas, Exception e) {
            if(e != null){
                e.printStackTrace();
                return;
            }

            final int datasCnt;
            datasCnt = tourDatas.getContents().size();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TourDatas, TourData 활용
                    tv_tourdatas_cnt.setText(String.valueOf(datasCnt));
                }
            });

            // 추천을 못 받았을 때 재생을 위한 정보(tts 테스트 용)
            testTourdata = tourDatas.getContents().get(0);

        }
    };

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

    // tts 이벤트 수신을 위한 receiver 해제
    private void unRegisterReceiver(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

        lbm.unregisterReceiver(broadcastReceiver);
    }

    // 시작한 tts 정보 표시 - start 이벤트
    private void showMediaInfo(String title, int dur, int position){

        Date date_dur = new Date(dur);
        Date date_position = new Date(position);

        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("HH:mm:ss");
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        simpleDateFormat.setTimeZone(gmtZone);

        tv_mp_title.setText(title);
        tv_mp_position.setText(simpleDateFormat.format(date_position));
        tv_mp_duration.setText(simpleDateFormat.format(date_dur));

        sb_mp.setMax(dur);
        sb_mp.setProgress(position);
    }

    // SeakBar position 과 시간 업데이트 - ticker 이벤트
    private void updateMPDisplay(int position){

        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("HH:mm:ss");
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        simpleDateFormat.setTimeZone(gmtZone);

        Date date_position = new Date(position);

        tv_mp_position.setText(simpleDateFormat.format(date_position));
        sb_mp.setProgress(position);
    }

    // SeakBar 조정 한 위치 를 전달
    private void updatePlay(int position){
        infodio.ttsSeek(position);
    }

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
                Log.d("sample", "play ticker");
                updateMPDisplay(ttsPlayInfo.getPosition());

            }else if(action.equals(Infodio.Action_PlayerPrepare)){
                Log.d("sample", "play prepare");

            }else if(action.equals(Infodio.Action_PlayerStart)){
                Log.d("sample", "play start : "+ttsPlayInfo.getContentTitle());
                showMediaInfo(ttsPlayInfo.getContentTitle(), ttsPlayInfo.getDuration(), ttsPlayInfo.getPosition());

            }else if(action.equals(Infodio.Action_PlayerStop)){
                Log.d("sample", "play stop");
                tv_mp_duration.setText("00:00:00");
                tv_mp_position.setText("00:00:00");
                tv_mp_title.setText("미재생");
                sb_mp.setProgress(0);
            }

        }
    };

    // 퍼미션 확인
    private void permissionCheck(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== 1111){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unRegisterReceiver();
        infodio.stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bt_mp_start:
                infodio.ttsPlay();
                break;
            case R.id.bt_mp_pause:
                infodio.ttsPause();
                break;
            case R.id.bt_mp_stop:
                infodio.ttsRelease(true);
                break;
            // 추천 정보 못 받을 때 TTS test 용
            case R.id.bt_test_tts:
//                if(testTourdata!= null)
//                ttsCheckTest(testTourdata);
                break;
        }
    }
}
