package com.dayeon.app.sswitch;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dayeon.app.sswitch.util.HttpUrlConnectionUtil;
import com.dayeon.app.sswitch.util.UuidUtil;
import com.dayeon.app.sswitch.vo.SsVO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class FavActivity extends AppCompatActivity {
    private String MY_DEVICE_ID;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    List<SsVO> ss = new ArrayList<>();

    LinearLayout yellowBox1,yellowBox2,yellowBox3,yellowBox4;
    TextView tv1, tv2, tv3, tv4;
    TextView tv1_1, tv2_1, tv3_1, tv4_1;
    ImageView del1, del2, del3, del4;
    private String favno1, favno2, favno3, favno4;
    private double lat_1 = 37.497953;
    private double lon_1 = 127.027624;
    private double lat_2 = 37.219438;
    private double lon_2 = 126.949043;
    private double lat_3 = 37.269044;
    private double lon_3 = 127.001048;
    private double lat_4 = 37.535190;
    private double lon_4 = 127.094655;

    private int DEL_INDEX = 100;
    private String DEL_NO = "";
    private String DEL_NAME = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        TextView tv = (TextView) findViewById(R.id.favTitle);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));

        tv1 = (TextView) findViewById(R.id.fav_box1);
        tv1.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv2 = (TextView) findViewById(R.id.fav_box2);
        tv2.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv3 = (TextView) findViewById(R.id.fav_box3);
        tv3.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv4 = (TextView) findViewById(R.id.fav_box4);
        tv4.setTypeface(Typeface.createFromAsset(getAssets(),"TmonMonsori.ttf"));
        tv1_1 = (TextView) findViewById(R.id.fav_box1_1);
        tv2_1 = (TextView) findViewById(R.id.fav_box2_1);
        tv3_1 = (TextView) findViewById(R.id.fav_box3_1);
        tv4_1 = (TextView) findViewById(R.id.fav_box4_1);
        del1 = (ImageView) findViewById(R.id.delFav1);
        del2 = (ImageView) findViewById(R.id.delFav2);
        del3 = (ImageView) findViewById(R.id.delFav3);
        del4 = (ImageView) findViewById(R.id.delFav4);
        yellowBox1 = (LinearLayout) findViewById(R.id.yellowBox1);
        yellowBox2 = (LinearLayout) findViewById(R.id.yellowBox2);
        yellowBox3 = (LinearLayout) findViewById(R.id.yellowBox3);
        yellowBox4 = (LinearLayout) findViewById(R.id.yellowBox4);


        //DB getUserFav
        MY_DEVICE_ID = UuidUtil.getDeviceUuid(this);
        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                params.put("deviceid",MY_DEVICE_ID);
                String result = HttpUrlConnectionUtil.request("/sswitch/getUserFav.do",params);

                if(result == "fail"){
                    Message msg = handlerFail.obtainMessage();
                    handlerFail.sendMessage(msg);
                }else{
                    Gson gson = new Gson();
                    ss = gson.fromJson(result, new TypeToken<List<SsVO>>(){}.getType());
                    if(ss == null) {
                        //System.out.println("ss is null");
                    }else{
                        //System.out.println("ss size : "+ss.size());
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                }
            }
        }.start();
    }

    public void setFavBox(){
        try {
            yellowBox1.setBackgroundResource(R.drawable.bg_box_plus);
            yellowBox2.setBackgroundResource(R.drawable.bg_box_plus);
            yellowBox3.setBackgroundResource(R.drawable.bg_box_plus);
            yellowBox4.setBackgroundResource(R.drawable.bg_box_plus);
            del1.setVisibility(View.INVISIBLE);
            del2.setVisibility(View.INVISIBLE);
            del3.setVisibility(View.INVISIBLE);
            del4.setVisibility(View.INVISIBLE);
            tv1.setText("");
            tv1_1.setText("");
            tv2.setText("");
            tv2_1.setText("");
            tv3.setText("");
            tv3_1.setText("");
            tv4.setText("");
            tv4_1.setText("");
            for (int i=0;i<ss.size();i++){
                switch (i){
                    case 0:
                        favno1 = ss.get(i).getFavno();
                        lat_1 = Double.parseDouble(ss.get(i).getLocationx());
                        lon_1 = Double.parseDouble(ss.get(i).getLocationy());
                        tv1.setText(URLDecoder.decode(ss.get(i).getFavname(), "UTF-8"));
                        tv1_1.setText(URLDecoder.decode(ss.get(i).getLocationname(), "UTF-8"));
                        yellowBox1.setBackgroundResource(R.drawable.bg_box_yellow);
                        del1.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        favno2 = ss.get(i).getFavno();
                        lat_2 = Double.parseDouble(ss.get(i).getLocationx());
                        lon_2 = Double.parseDouble(ss.get(i).getLocationy());
                        tv2.setText(URLDecoder.decode(ss.get(i).getFavname(), "UTF-8"));
                        tv2_1.setText(URLDecoder.decode(ss.get(i).getLocationname(), "UTF-8"));
                        yellowBox2.setBackgroundResource(R.drawable.bg_box_yellow);
                        del2.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        favno3 = ss.get(i).getFavno();
                        lat_3 = Double.parseDouble(ss.get(i).getLocationx());
                        lon_3 = Double.parseDouble(ss.get(i).getLocationy());
                        tv3.setText(URLDecoder.decode(ss.get(i).getFavname(), "UTF-8"));
                        tv3_1.setText(URLDecoder.decode(ss.get(i).getLocationname(), "UTF-8"));
                        yellowBox3.setBackgroundResource(R.drawable.bg_box_yellow);
                        del3.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        favno4 = ss.get(i).getFavno();
                        lat_4 = Double.parseDouble(ss.get(i).getLocationx());
                        lon_4 = Double.parseDouble(ss.get(i).getLocationy());
                        tv4.setText(URLDecoder.decode(ss.get(i).getFavname(), "UTF-8"));
                        tv4_1.setText(URLDecoder.decode(ss.get(i).getLocationname(), "UTF-8"));
                        yellowBox4.setBackgroundResource(R.drawable.bg_box_yellow);
                        del4.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }catch (Exception e){

        }
    }
    final Handler handler = new Handler(){
        public  void handleMessage(Message msg){
            setFavBox();
        }
    };


    final Handler handlerFail = new Handler(){
        public  void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정해 정보를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    };

    public void onClickBox(View view){
        switch (view.getId()) {
            case R.id.fav_box1:
                Intent intent = new Intent(this, DaumMapActivity.class);
                if(ss.size()>0)
                    intent.putExtra("gubun","fav");
                else
                    Toast.makeText(getApplicationContext(), "지도 우측 하단 별아이콘을 클릭해 등록해주세요.", Toast.LENGTH_LONG).show();
                intent.putExtra("lat",lat_1+"");
                intent.putExtra("lon",lon_1+"");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav_box2:
                Intent intent2 = new Intent(this, DaumMapActivity.class);
                if(ss.size()>1)
                    intent2.putExtra("gubun","fav");
                else
                    Toast.makeText(getApplicationContext(), "지도 우측 하단 별아이콘을 클릭해 등록해주세요.", Toast.LENGTH_LONG).show();
                intent2.putExtra("lat",lat_2+"");
                intent2.putExtra("lon",lon_2+"");
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent2);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav_box3:
                Intent intent3 = new Intent(this, DaumMapActivity.class);
                if(ss.size()>2)
                    intent3.putExtra("gubun","fav");
                else
                    Toast.makeText(getApplicationContext(), "지도 우측 하단 별아이콘을 클릭해 등록해주세요.", Toast.LENGTH_LONG).show();
                intent3.putExtra("lat",lat_3+"");
                intent3.putExtra("lon",lon_3+"");
                intent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent3);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav_box4:
                Intent intent4 = new Intent(this, DaumMapActivity.class);
                if(ss.size()>3)
                    intent4.putExtra("gubun","fav");
                else
                    Toast.makeText(getApplicationContext(), "지도 우측 하단 별아이콘을 클릭해 등록해주세요.", Toast.LENGTH_LONG).show();
                intent4.putExtra("lat",lat_4+"");
                intent4.putExtra("lon",lon_4+"");
                intent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent4);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
        }
    }

    public  void delFav(View view){
        switch (view.getId()) {
            case R.id.delFav1:
                //Toast.makeText(getApplicationContext(), "1 버튼 눌림",Toast.LENGTH_SHORT).show();
                DEL_INDEX = 0;
                DEL_NO = ss.get(0).getFavno();
                DEL_NAME = tv1_1.getText().toString();
                showFavDelDialog();
                break;
            case R.id.delFav2:
                //Toast.makeText(getApplicationContext(), "2 버튼 눌림",Toast.LENGTH_SHORT).show();
                DEL_INDEX = 1;
                DEL_NO = ss.get(1).getFavno();
                DEL_NAME = tv2_1.getText().toString();
                showFavDelDialog();
                break;
            case R.id.delFav3:
                //Toast.makeText(getApplicationContext(), "3 버튼 눌림",Toast.LENGTH_SHORT).show();
                DEL_INDEX = 2;
                DEL_NO = ss.get(2).getFavno();
                DEL_NAME = tv3_1.getText().toString();
                showFavDelDialog();
                break;
            case R.id.delFav4:
                //Toast.makeText(getApplicationContext(), "4 버튼 눌림",Toast.LENGTH_SHORT).show();
                DEL_INDEX = 3;
                DEL_NO = ss.get(3).getFavno();
                DEL_NAME = tv4_1.getText().toString();
                showFavDelDialog();
                break;
        }
    }

    public void showFavDelDialog(){
        LayoutInflater dialog = LayoutInflater.from(this);
        final View dialogLayout = dialog.inflate(R.layout.dialog_fav_del, null);
        final Dialog myDialog = new Dialog(this);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(dialogLayout);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = myDialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.75); //Display 사이즈의 75%
        int height = (int) (display.getHeight() * 0.5);  //Display 사이즈의 50%

        layoutParams.width = width;
        layoutParams.height = height;
        //layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.flags  = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount  = 0.7f;
        window.setAttributes(layoutParams);
        myDialog.show();

        TextView myLocationName = (TextView) dialogLayout.findViewById(R.id.myLocationName);
        TextView btn_ok = (TextView) dialogLayout.findViewById(R.id.btn_ok);
        TextView btn_cancel = (TextView) dialogLayout.findViewById(R.id.btn_cancel);
        myLocationName.setText(DEL_NAME);

        btn_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //OK 누르면 할거
                new  Thread(){
                    public void run(){
                        ContentValues params = new ContentValues();
                        params.put("deviceid",MY_DEVICE_ID);
                        params.put("favno",DEL_NO);
                        String result = HttpUrlConnectionUtil.request("/sswitch/deleteUserFav.do",params);

                        if("success".equals(result)){
                            Message msg = handler1.obtainMessage();
                            handler1.sendMessage(msg);
                            myDialog.cancel();
                        }else if("fail".equals(result)){
                            Message msg = handler3.obtainMessage();
                            handler3.sendMessage(msg);
                        }else{
                            myDialog.cancel();
                        }
                    }
                }.start();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myDialog.cancel();
            }
        });
    }
    final Handler handler1 = new Handler(){
        public void handleMessage(Message msg){
            Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다.",Toast.LENGTH_LONG).show();
            ss.remove(DEL_INDEX);
            setFavBox();
        }
    };

    final Handler handler3 = new Handler(){
        public void handleMessage(Message msg){
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정합니다. 다시 시도해주세요.",Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1111){
            if(resultCode==1234) {
                Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다.",Toast.LENGTH_LONG).show();
                ss.remove(0);
                setFavBox();
            }
        }else if(requestCode==2222){
            if(resultCode==1234) {
                Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다.",Toast.LENGTH_LONG).show();
                ss.remove(1);
                setFavBox();
            }
        }else if(requestCode==3333){
            if(resultCode==1234) {
                Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다.",Toast.LENGTH_LONG).show();
                ss.remove(2);
                setFavBox();
            }
        }else if(requestCode==4444){
            if(resultCode==1234) {
                Toast.makeText(getApplicationContext(), "즐겨찾기가 삭제되었습니다.",Toast.LENGTH_LONG).show();
                ss.remove(3);
                setFavBox();
            }
        }
    }

    public void onClickMenu(View view){
        switch (view.getId()){
            case R.id.home:
                Intent intent = new Intent(FavActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.write:
                Intent intent1 = new Intent(FavActivity.this, WriteActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent1);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.daummap:
                Intent intent2 = new Intent(FavActivity.this, DaumMapActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent2);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.mypage:
                Intent intent3 = new Intent(FavActivity.this, MyPageActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent3);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav:
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
