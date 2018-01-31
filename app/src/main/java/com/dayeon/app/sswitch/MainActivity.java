package com.dayeon.app.sswitch;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dayeon.app.sswitch.util.HashKeyUtil;
import com.dayeon.app.sswitch.util.HttpUrlConnectionUtil;
import com.dayeon.app.sswitch.util.UuidUtil;
import com.dayeon.app.sswitch.vo.SsVO;
import com.google.gson.Gson;

import junit.framework.Test;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private String MY_DEVICE_ID;
    private Typeface tmonFont;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    ImageView img;
    TextView percentMsg;
    TextView treeMsg;
    EditText searchAddress;
    Random rd;

    SsVO ss = new SsVO();
    String sfName = "mainPopDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //HashKey
        //System.out.println("****************HashKey****************");
        //System.out.println(HashKeyUtil.getKeyHash(this));
        //Toast.makeText(this,HashKeyUtil.getKeyHash(this), Toast.LENGTH_SHORT).show();
        //UUID
        //System.out.println("****************UUID****************");
        //System.out.println(UuidUtil.getDeviceUuid(this));
        MY_DEVICE_ID = UuidUtil.getDeviceUuid(this);

        //메인 도움말 팝업
        SharedPreferences sf = getSharedPreferences(sfName, 0);
        String isShow = sf.getString("show", "");
        if(!"false".equals(isShow)){
            showMainDialog();
        }

        //세팅
        percentMsg = (TextView) findViewById(R.id.percentMsg);
        percentMsg.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        treeMsg = (TextView) findViewById(R.id.treeMsg);
        searchAddress = (EditText) findViewById(R.id.searchAddress);
        img = (ImageView) findViewById(R.id.flowerpot);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* just test
                rd = new Random();
                int num = rd.nextInt(51)+50;
                int flowerpotId = getResources().getIdentifier("flowerpot_"+num,"drawable",getPackageName());
                img.setImageResource(flowerpotId);
                TextView percentMsg1 = (TextView) findViewById(R.id.percentMsg);
                percentMsg1.setText(num+"%");
                */
            }
        });

        //DB getUserInfo
        //conntectCheck();
        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                params.put("deviceid",MY_DEVICE_ID);
                String result = HttpUrlConnectionUtil.request("/sswitch/getUserInfo.do",params);
                //System.out.println("****************getUserInfo.do****************");
                //System.out.println(result);

                if(result == "fail"){
                    Message msg = handlerFail.obtainMessage();
                    handlerFail.sendMessage(msg);
                }else{
                    Gson gson = new Gson();
                    ss = gson.fromJson(result, SsVO.class);
                    if(ss == null) {
                        HttpUrlConnectionUtil.request("/sswitch/insertUserInfo.do", params);
                    }else{
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                }
            }
        }.start();


        /*화면 해상도 구하기
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screen_width = size.x;
        int screen_height = size.y;
        System.out.println("screen_width : "+screen_width);
        System.out.println("screen_height : "+screen_height);
        //
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println("screen_width2 : "+metrics.widthPixels);
        System.out.println("screen_height2 : "+metrics.heightPixels);
*/
        //주소 검색 엔터키 이벤트
        searchAddress.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if("".equals(searchAddress.getText())){
                        Toast.makeText(getApplicationContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    //Enter키눌렀을떄 처리
                    Intent intent2 = new Intent(MainActivity.this, DaumMapActivity.class);
                    intent2.putExtra("address",searchAddress.getText().toString());
                    //System.out.println("searchAddress.getText() - "+searchAddress.getText());
                    intent2.putExtra("gubun","main");
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                    return true;
                }
                return false;
            }
        });
    }

    //웹에서 데이터를 가져오기 전에 먼저 네트워크 상태부터 확인
    public void conntectCheck(){

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            //Toast.makeText(this,"네트워크 연결중입니다.", Toast.LENGTH_SHORT).show();
        } else {
            // display error
            Toast.makeText(this,"네트워크 상태를 확인하십시오", Toast.LENGTH_SHORT).show();
        }
    }

    final Handler handler = new Handler(){
        public  void handleMessage(Message msg){
            int myPoint = Integer.parseInt(ss.getPoint());
            int myPoint1 = myPoint/100;
            int myPoint2 = myPoint%100;
            percentMsg.setText(myPoint2+"%");
            treeMsg.setText(myPoint1+"그루째 심는 중!");

            int flowerpotId = getResources().getIdentifier("flowerpot_"+myPoint2,"drawable",getPackageName());
            img.setImageResource(flowerpotId);
        }
    };

    final Handler handlerFail = new Handler(){
        public  void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정해 정보를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    };

    public void onClickHelp(View v){
        showMainDialog();
    }
    public void showMainDialog(){
        LayoutInflater dialog = LayoutInflater.from(this);
        final View dialogLayout = dialog.inflate(R.layout.dialog_main, null);
        final Dialog myDialog = new Dialog(this);

        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(dialogLayout);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = myDialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        myDialog.show();

        ImageView btn_ok = (ImageView) dialogLayout.findViewById(R.id.btn_ok);
        TextView txt1 = (TextView) dialogLayout.findViewById(R.id.txt1);
        TextView txt2 = (TextView) dialogLayout.findViewById(R.id.txt2);
        TextView txt3 = (TextView) dialogLayout.findViewById(R.id.txt3);
        TextView txt4 = (TextView) dialogLayout.findViewById(R.id.txt4);
        TextView txt5 = (TextView) dialogLayout.findViewById(R.id.txt5);
        TextView txt6 = (TextView) dialogLayout.findViewById(R.id.txt6);
        TextView txt7 = (TextView) dialogLayout.findViewById(R.id.txt7);
        txt1.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        txt2.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        //txt3.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        txt4.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        //txt5.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        txt6.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        //txt7.setTypeface(Typeface.createFromAsset(getAssets(),"NanumPen.ttf"));
        SpannableStringBuilder ssb = new SpannableStringBuilder("쓰레기통의 위치를 알려준다!");
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FAD759")), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt1.setText(ssb);
        txt2.setText("1. 쓰레기통 위치는 어디에?");
        SpannableStringBuilder ssb3 = new SpannableStringBuilder("밖을 돌아다니다 쓰레기통을 찾을 수 없을 때 지도에서 쓰레기통 위치를\n" +
                "메인화면 검색바 or 하단 두번째 지도 아이콘을 통해 검색해보세요!");
        ssb3.setSpan(new ForegroundColorSpan(Color.parseColor("#FAD759")), 25, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt3.setText(ssb3);
        txt4.setText("2. 내가 자주가는 장소를 등록하고 싶다면?");
        SpannableStringBuilder ssb5 = new SpannableStringBuilder("내가 자주가는 장소를 등록하고 싶다면 즐겨찾기 페이지에 들어가거나\n" +
                "지도 하단 별 아이콘 클릭해 위치를 등록하면 좀 더 쉽게 찾아볼 수 있어요!");
        ssb5.setSpan(new ForegroundColorSpan(Color.parseColor("#FAD759")), 21, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb5.setSpan(new ForegroundColorSpan(Color.parseColor("#FAD759")), 36, 48, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt5.setText(ssb5);
        txt6.setText("3. 내가 버린 쓰레기들이 나무가 된다?");
        SpannableStringBuilder ssb7 = new SpannableStringBuilder("쓰레기를 버리기 위해 쓰레기통 위치를 검색하면 포인트가 쌓이고,\n" +
                "포인트가 쌓여 100%가 되면 우리가 나무를 심어줘요!");
        ssb7.setSpan(new ForegroundColorSpan(Color.parseColor("#FAD759")), 57, 59, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt7.setText(ssb7);

        btn_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences sf = getSharedPreferences(sfName, 0);
                SharedPreferences.Editor editor = sf.edit();
                editor.putString("show", "false");
                editor.commit();
                myDialog.cancel();
            }
        });
    }

    public void onClickMenu(View view){

        switch (view.getId()){
            case R.id.write:
                Intent intent1 = new Intent(MainActivity.this, WriteActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent1);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.map:
                Intent intent2 = new Intent(MainActivity.this, DaumMapActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent2);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.mypage:
                Intent intent3 = new Intent(MainActivity.this, MyPageActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent3);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav:
                Intent intent4 = new Intent(MainActivity.this, FavActivity.class);
                intent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent4);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.goMyLocation:
                Intent intent6 = new Intent(MainActivity.this, DaumMapActivity.class);
                intent6.putExtra("gubun","mylocation");
                intent6.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent6);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if(0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime){
            ActivityCompat.finishAffinity(this);
            //super.onBackPressed();
        }else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한 번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }
    }
}