package com.dayeon.app.sswitch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dayeon.app.sswitch.util.GpsUtil;
import com.dayeon.app.sswitch.util.HttpUrlConnectionUtil;
import com.dayeon.app.sswitch.util.UuidUtil;
import com.dayeon.app.sswitch.vo.SsVO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class DaumMapActivity extends FragmentActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{
    private String MY_DEVICE_ID;
    static final String API_KEY = "d8da16e88ca7f92bc4968236bd7f21c7";
    private RelativeLayout mapViewContainer;
    String gpsEnabled;
    private GpsUtil gps;
    private double latitude = 37.484296;
    private double longitude = 126.929787;
    private MapView mapView;
    private MapReverseGeoCoder mReverseGeoCoder = null;
    private String FLAG = "";
    private String ADDRESS = "";
    private String FAV_ADDRESS = "";
    private String FAV_X = "";
    private String FAV_Y = "";
    private double x_Val = 0;
    private double y_Val = 0;
    private EditText searchAddress;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private Activity activity;
    List<SsVO> ss = new ArrayList<>();
    SsVO ss_a = new SsVO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_map);
        activity = this;

        searchAddress = (EditText) findViewById(R.id.searchAddress);

        chkGpsService();

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(DaumMapActivity.this, "권한 확인 완료", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(DaumMapActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DaumMapActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle("권한 요구")
                .setRationaleMessage("서비스 이용을 위해 위치 권한 허용이 필요합니다.")
                .setDeniedTitle("권한 거부")
                .setDeniedMessage(
                        "권한을 거부한다면 서비스를 이용하실 수 없습니다.\n[설정] > [권한]에서 변경해주세요.")
                .setGotoSettingButtonText("설정")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
//If you reject permission,you can not use this service Please turn on permissions at [Setting] > [Permission]

        gps = new GpsUtil(DaumMapActivity.this);

        //다음맵
        mapView = new MapView(this);
        mapView.setDaumMapApiKey(API_KEY);
        mapView.setMapViewEventListener(this);

        mapViewContainer = (RelativeLayout) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        // 중심점 설정
        // 신림역 37.484296, 126.929787
        // 관악우체국 37.484908, 126.933180
        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);
        //mapView.setZoomLevel(1,true);//낮을수록 자세히 -2 ~ 12

        Intent data = getIntent();
        String gubun = data.getStringExtra("gubun");
        if("fav".equals(gubun)){
            String lat = data.getStringExtra("lat");
            String lon = data.getStringExtra("lon");
            //System.out.println("parseDouble "+Double.parseDouble(lat));
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(Double.parseDouble(lat), Double.parseDouble(lon)), 2, true);
        }else if("main".equals(gubun)){
            ADDRESS = data.getStringExtra("address");
            searchAddress.setText(ADDRESS);
            searchAddress();
        }else if("mylocation".equals(gubun)){
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        }else{
            //mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 2, true);
            //mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        }

        //DB getLocationInfo
        MY_DEVICE_ID = UuidUtil.getDeviceUuid(this);

        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                params.put("deviceid",MY_DEVICE_ID);
                String result = HttpUrlConnectionUtil.request("/sswitch/getLocation.do",params);

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
                        Message msg = handlerSS.obtainMessage();
                        handlerSS.sendMessage(msg);
                    }
                }
            }
        }.start();

        //주소 검색 엔터키 이벤트
        searchAddress.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                ADDRESS = searchAddress.getText().toString();

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if("".equals(ADDRESS)){
                        Toast.makeText(getApplicationContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    searchAddress();
                    return true;
                }
                return false;
            }
        });

        //
    }

    private boolean chkGpsService() {

        //GPS가 켜져 있는지 확인함.
        gpsEnabled = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gpsEnabled.matches(".*gps.*") && gpsEnabled.matches(".*network.*"))) {
            //gps가 사용가능한 상태가 아니면
            new AlertDialog.Builder(this).setTitle("GPS 설정").setMessage("설정에서 GPS를 활성화해주세요.\n비활성화 시 위치 서비스가 제한됩니다.").setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //GPS 설정 화면을 띄움
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create().show();
        }
        return false;
    }

    public void searchAddress(){
        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                //params.put("name",ADDRESS);
                //String result = HttpUrlConnectionUtil.request("/sswitch/searchAddress.do",params);
                String result = NaverMapGeocode.request(ADDRESS);
                //System.out.println(result);

                if("fail".equals(result)){
                    Message msg = handlerFail.obtainMessage();
                    handlerFail.sendMessage(msg);
                }else{
                    JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
                    JsonObject jsonObject1 = jsonObject.getAsJsonObject("result");
                    if(jsonObject1 == null){
                        Message msg = handlerNone.obtainMessage();
                        handlerNone.sendMessage(msg);
                    }else{
                        JsonArray jsonArray = jsonObject1.getAsJsonArray("items");
                        JsonObject jsonObject2 = jsonArray.get(0).getAsJsonObject();
                        JsonObject jsonObject3 = jsonObject2.getAsJsonObject("point");
                        JsonPrimitive jsonPrimitive_x = jsonObject3.getAsJsonPrimitive("x");
                        JsonPrimitive jsonPrimitive_y = jsonObject3.getAsJsonPrimitive("y");
                        x_Val = jsonPrimitive_x.getAsDouble();
                        y_Val = jsonPrimitive_y.getAsDouble();

                        Message msg = handlerSearch.obtainMessage();
                        handlerSearch.sendMessage(msg);
                        addPoint();
                    }
                }
            }
        }.start();
    }
    public void addPoint(){
        //point
        new  Thread(){
            public void run(){
                ContentValues params = new ContentValues();
                params.put("deviceid",MY_DEVICE_ID);
                String result = HttpUrlConnectionUtil.request("/sswitch/addUserPoint.do",params);
            }
        }.start();
    }

    final Handler handlerSS = new Handler(){
        public  void handleMessage(Message msg) {
            for(int i=0;i<ss.size();i++){
                MapPOIItem marker = new MapPOIItem();

                try {
                    marker.setItemName(URLDecoder.decode(ss.get(i).getName(),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    marker.setItemName("쓰레기통");
                    e.printStackTrace();
                }
                marker.setTag(0);
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(ss.get(i).getLocationx()), Double.parseDouble(ss.get(i).getLocationy())));
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 커스텀 이미지
                marker.setCustomImageResourceId(R.drawable.ic_marker);
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomSelectedImageResourceId(R.drawable.ic_marker_on);
                marker.setShowDisclosureButtonOnCalloutBalloon(false);
                mapView.addPOIItem(marker);
            }
        }
    };

    final Handler handlerSearch = new Handler(){
        public  void handleMessage(Message msg) {
            //Toast.makeText(getApplicationContext(), "검색 성공", Toast.LENGTH_LONG).show();
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(y_Val, x_Val), 3, true);
        }
    };

    final Handler handlerA = new Handler(){
        public  void handleMessage(Message msg) {
            //Toast.makeText(getApplicationContext(), "검색 성공", Toast.LENGTH_LONG).show();
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(Double.parseDouble(ss_a.getLocationx()), Double.parseDouble(ss_a.getLocationy())), 3, true);
        }
    };

    final Handler handlerFail = new Handler(){
        public  void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정해 정보를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    };

    final Handler handlerNone = new Handler(){
        public  void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        //Toast.makeText(this, "ZoomLevel : " + i, Toast.LENGTH_SHORT).show();
        if(i > 3){

        }
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        //Toast.makeText(this, "Clicked " + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    private void onFinishReverseGeoCoding(String result) {
        //Toast.makeText(DaumMapActivity.this, "현재 위치 : " + result, Toast.LENGTH_SHORT).show();
        FAV_ADDRESS = result;
        FAV_X = mapView.getMapCenterPoint().getMapPointGeoCoord().latitude + "";
        FAV_Y = mapView.getMapCenterPoint().getMapPointGeoCoord().longitude + "";
        if("MINWON".equals(FLAG)) {
            Intent intent = new Intent(this, WriteActivity.class);
            intent.putExtra("myLocationName", result);
            intent.putExtra("myLatitude", FAV_X);
            intent.putExtra("myLongitude", FAV_Y);
            startActivityForResult(intent, 1);
        }else{
            showFavAddDialog();
        }
    }

    public void onClickMyLocation(View v){

        if(mapView.getCurrentLocationTrackingMode() == MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading){
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }else{
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        }
        addPoint();
    }

    public void addFav(View v){

        mReverseGeoCoder = new MapReverseGeoCoder(API_KEY, mapView.getMapCenterPoint(), DaumMapActivity.this, DaumMapActivity.this);
        mReverseGeoCoder.startFindingAddress();
    }

    public void onClickMinwon(View v){
        FLAG = "MINWON";
        mReverseGeoCoder = new MapReverseGeoCoder(API_KEY, mapView.getMapCenterPoint(), DaumMapActivity.this, DaumMapActivity.this);
        mReverseGeoCoder.startFindingAddress();
    }

    public void showFavAddDialog(){
        LayoutInflater dialog = LayoutInflater.from(this);
        final View dialogLayout = dialog.inflate(R.layout.dialog_fav_add, null);
        final Dialog myDialog = new Dialog(this);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(dialogLayout);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = myDialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.75); //Display 사이즈의 70%
        int height = (int) (display.getHeight() * 0.5);  //Display 사이즈의 90%

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
        myLocationName.setText(FAV_ADDRESS);

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
                        params.put("locationname",FAV_ADDRESS);
                        params.put("locationx",FAV_X);
                        params.put("locationy",FAV_Y);
                        String result = HttpUrlConnectionUtil.request("/sswitch/insertUserFav.do",params);

                        if("success".equals(result)){
                            Message msg = handler1.obtainMessage();
                            handler1.sendMessage(msg);
                            myDialog.cancel();
                            //Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정해 정보를 불러오지 못했습니다.",Toast.LENGTH_SHORT).show();
                        }else if("over".equals(result)){
                            Message msg = handler2.obtainMessage();
                            handler2.sendMessage(msg);
                            myDialog.cancel();
                        }else if("fail".equals(result)){
                            Message msg = handler3.obtainMessage();
                            handler3.sendMessage(msg);
                        }else{

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
            Toast.makeText(getApplicationContext(), "즐겨찾기에 추가되었습니다.",Toast.LENGTH_LONG).show();
        }
    };

    final Handler handler2 = new Handler(){
        public void handleMessage(Message msg){
            Toast.makeText(getApplicationContext(), "최대 4개까지 추가할 수 있습니다.",Toast.LENGTH_LONG).show();
        }
    };

    final Handler handler3 = new Handler(){
        public void handleMessage(Message msg){
            Toast.makeText(getApplicationContext(), "서버와의 연결이 불안정합니다. 다시 시도해주세요.",Toast.LENGTH_LONG).show();
        }
    };

    public void onClickMenu(View view){

        switch (view.getId()){
            case R.id.home:
                Intent intent = new Intent(DaumMapActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.write:
                Intent intent2 = new Intent(DaumMapActivity.this, WriteActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent2);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.daummap:
                break;
            case R.id.mypage:
                Intent intent3 = new Intent(DaumMapActivity.this, MyPageActivity.class);
                intent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent3);
                overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
                break;
            case R.id.fav:
                Intent intent4 = new Intent(DaumMapActivity.this, FavActivity.class);
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

        if(0 <= intervalTime && 2000 >= intervalTime){
            ActivityCompat.finishAffinity(this);
            //super.onBackPressed();
        }else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한 번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        //mapView 제거
        mapViewContainer.removeView(mapView);
        super.finish();
        overridePendingTransition(R.anim.none_anim,R.anim.none_anim);
    }
}
