package com.dayeon.app.sswitch;

import android.app.ExpandableListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dayeon.app.sswitch.util.HttpUrlConnectionUtil;
import com.dayeon.app.sswitch.util.UuidUtil;
import com.dayeon.app.sswitch.vo.SsVO;
import com.google.gson.Gson;

public class MyPageActivity extends AppCompatActivity{//extends ExpandableListActivity
    private String MY_DEVICE_ID;
    SsVO ss = new SsVO();
    TextView tv,tv1,tv2,tv3,tv4;

    LinearLayout listLayout1, listLayout1_1, listLayout2, listLayout2_1, listLayout3, listLayout3_1, listLayout3_2;
    TextView list1, list1_1, list1_2;
    TextView list2, list2_1;
    TextView list3_1_1, list3_2_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        tv = (TextView) findViewById(R.id.userName);
        tv1 = (TextView) findViewById(R.id.pointCount);
        tv1.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv2 = (TextView) findViewById(R.id.treeCount);
        tv2.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv3 = (TextView) findViewById(R.id.writeCount);
        tv3.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv4 = (TextView) findViewById(R.id.myTitle);
        tv4.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));

        listLayout3 = (LinearLayout) findViewById(R.id.listLayout3);
        listLayout3_1 = (LinearLayout) findViewById(R.id.listLayout3_1);
        listLayout3_2 = (LinearLayout) findViewById(R.id.listLayout3_2);

        list3_1_1 = (TextView) findViewById(R.id.list3_1_1);
        list3_2_1 = (TextView) findViewById(R.id.list3_2_1);

        //DB getUserInfo
        MY_DEVICE_ID = UuidUtil.getDeviceUuid(this);
        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                params.put("deviceid",MY_DEVICE_ID);
                String result = HttpUrlConnectionUtil.request("/sswitch/getUserInfo.do",params);

                if(result == "fail"){
                    Message msg = handlerFail.obtainMessage();
                    handlerFail.sendMessage(msg);
                }else{
                    Gson gson = new Gson();
                    ss = gson.fromJson(result, SsVO.class);

                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    final Handler handler = new Handler(){
        public  void handleMessage(Message msg){
            tv.setText(ss.getUsername());

            int myPoint = Integer.parseInt(ss.getPoint());
            int myPoint1 = myPoint/100;
            int myPoint2 = myPoint%100;
            tv1.setText(myPoint2+"");
            tv2.setText(myPoint1+"");
            int state10 = Integer.parseInt(ss.getState10());
            int state50 = Integer.parseInt(ss.getState50());
            int total = state10+state50;
            tv3.setText(total+"");

            list3_1_1.setText(ss.getState10()+"");
            list3_2_1.setText(ss.getState50()+"");
        }
    };

    final Handler handlerFail = new Handler(){
        public  void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정해 정보를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    };

    public void onClickList(View view){
        int d = 30;
        float mScale = getResources().getDisplayMetrics().density;
        final int calHeight = (int)(d*mScale);

        switch (view.getId()){
            case R.id.listLayout3:
                if (listLayout3_1 .getVisibility() == View.VISIBLE)
                    listLayout3_1.setVisibility(View.INVISIBLE);
                else
                    listLayout3_1.setVisibility(View.VISIBLE);
                if (listLayout3_2 .getVisibility() == View.VISIBLE)
                    listLayout3_2.setVisibility(View.INVISIBLE);
                else
                    listLayout3_2.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onClickMenu(View view){

        switch (view.getId()){
            case R.id.home:
                Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.write:
                Intent intent1 = new Intent(MyPageActivity.this, WriteActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.daummap:
                Intent intent2 = new Intent(MyPageActivity.this, DaumMapActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.mypage:
                break;
            case R.id.fav:
                Intent intent4 = new Intent(MyPageActivity.this, FavActivity.class);
                startActivity(intent4);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
    }
}