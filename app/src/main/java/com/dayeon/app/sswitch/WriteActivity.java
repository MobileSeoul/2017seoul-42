package com.dayeon.app.sswitch;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dayeon.app.sswitch.util.HttpUrlConnectionUtil;
import com.dayeon.app.sswitch.util.UuidUtil;

public class WriteActivity extends AppCompatActivity {
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    private Activity ACTIVITY_MINWON;
    TextView tv;
    EditText et1, et2;
    Spinner s;
    private String MY_DEVICE_ID;
    private String USERNAME = "";
    private String NAME = "";
    private String LOCATION_X = "";
    private String LOCATION_Y = "";
    private String TYPE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        ACTIVITY_MINWON = this;
        tv = (TextView) findViewById(R.id.writeTitle);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        et1 = (EditText) findViewById(R.id.writeName);
        et2 = (EditText) findViewById(R.id.writeAddress);

        //데이터 가져오기
        Intent intent = getIntent();
        USERNAME = intent.getStringExtra("UserName");
        NAME = intent.getStringExtra("myLocationName");
        LOCATION_X = intent.getStringExtra("myLatitude");
        LOCATION_Y = intent.getStringExtra("myLongitude");
        et1.setText(USERNAME);
        et2.setText(NAME);

        s = (Spinner) findViewById(R.id.spinner);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //tv.setText("p: "+position);
                if(position == 1)
                    TYPE = "A";
                else if(position == 2)
                    TYPE = "B";
                else
                    TYPE = "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onClickMinwon(View view){

        MY_DEVICE_ID = UuidUtil.getDeviceUuid(this);
        USERNAME = et1.getText().toString();
        NAME = et2.getText().toString();

        if("".equals(USERNAME)){
            Toast.makeText(getApplicationContext(), "성명을 입력하세요.",Toast.LENGTH_SHORT).show();
            return;
        }else if("".equals(NAME)){
            Toast.makeText(getApplicationContext(), "주소를 확인하세요.",Toast.LENGTH_SHORT).show();
            return;
        }else if("".equals(TYPE)){
            Toast.makeText(getApplicationContext(), "내용을 선택하세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                params.put("deviceid",MY_DEVICE_ID);
                params.put("username",USERNAME);
                params.put("locationname",NAME);
                params.put("locationx",LOCATION_X);
                params.put("locationy",LOCATION_Y);
                params.put("type",TYPE);
                String result = HttpUrlConnectionUtil.request("/sswitch/insertUserMinwon.do",params);

                if("success".equals(result)){
                    Message msg = handler1.obtainMessage();
                    handler1.sendMessage(msg);
                }else if("fail".equals(result)){
                    Message msg = handler3.obtainMessage();
                    handler3.sendMessage(msg);
                }else{
                    Message msg = handler3.obtainMessage();
                    handler3.sendMessage(msg);
                }
            }
        }.start();
    }

    final Handler handler1 = new Handler(){
        public void handleMessage(Message msg){
            et1.setText("");
            et2.setText("");
            s.setSelection(0);
            Toast.makeText(getApplicationContext(), "민원이 접수되었습니다.",Toast.LENGTH_LONG).show();
            //ACTIVITY_MINWON.finish();

            Intent intent = new Intent(WriteActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
        }
    };

    final Handler handler3 = new Handler(){
        public void handleMessage(Message msg){
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정합니다. 다시 시도해주세요.",Toast.LENGTH_LONG).show();
        }
    };


    public void goSelectMap(View view){
        Intent intent22 = new Intent(this, SelectDaumMapActivity.class);
        intent22.putExtra("UserName",et1.getText().toString());
        intent22.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent22);
        //startActivityForResult(intent22, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==10){
            if(resultCode==11){
                //데이터 받기
                NAME = data.getStringExtra("myLocationName");
                LOCATION_X = data.getStringExtra("myLatitude");
                LOCATION_Y = data.getStringExtra("myLongitude");
                et2.setText(NAME);
            }
        }
    }


    public void onClickMenu(View view){

        switch (view.getId()){
            case R.id.home:
                Intent intent = new Intent(WriteActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.write:
                break;
            case R.id.daummap:
                Intent intent2 = new Intent(WriteActivity.this, DaumMapActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent2);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.mypage:
                Intent intent3 = new Intent(WriteActivity.this, MyPageActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent3);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav:
                Intent intent4 = new Intent(WriteActivity.this, FavActivity.class);
                intent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent4);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
    }
}
