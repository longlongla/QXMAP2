package com.practice.flyer.qxmap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baidu.mapapi.*;
import com.baidu.mapapi.map.MapView;
import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;




public class MainActivity extends Activity {
    private MapView mMapView;
    private Button botton1;
    private Button botton2;
    private BaiduMap mBaiduMap;
    private LocationManager locationManager;
    private String provider;
    private boolean isFirstLocate = true;
    private String friends = "15820575684";
    private IntentFilter receiveFilter;
    private Location location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if(providerList.contains(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
            Toast.makeText(this, "gps use", Toast.LENGTH_SHORT).show();
        }
        else if(providerList.contains(LocationManager.NETWORK_PROVIDER))
        {
            provider = LocationManager.NETWORK_PROVIDER;
            Toast.makeText(this, "net to use", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        if(location != null)
        {
            navigateTo(location);
        }
        locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);

        botton1 = (Button)findViewById(R.id.btn_friends);
        botton1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this,FriendListActivity.class);
                startActivity(intent);
            }
        });
        //点击刷新，发送短信（where are you?）给朋友
        botton2 = (Button)findViewById(R.id.btn_refresh);
        botton2.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view)
            {
                for(int i=0;i<ListAll.friendslist.size(); i++)
                {
                    SmsManager smsManager = SmsManager.getDefault();   //发送短信的逻辑
                    smsManager.sendTextMessage(ListAll.friendslist.get(i).getNum(),null,"where are you?",null,null);
                }
            }
        });
	   /**/
        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //收取短信的处理逻辑，监听广播。
        class MessageReceiver extends BroadcastReceiver
        {
            @Override
            public void onReceive(Context context, Intent intent)  //  onReceive()
            {
                Bundle bundle = intent.getExtras();
                Object[] pdus = (Object[])bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for(int i = 0; i<messages.length; i++)
                {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                String address = messages[0].getOriginatingAddress();
                String fullMessage = " ";
                for(SmsMessage message : messages)
                {
                    fullMessage += message.getMessageBody();
                }
                //收取处理短信字符串   ->  address  fullmessage

                if(fullMessage.trim().equals("where are you?"))
                {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(address,null,String.valueOf(location.getLatitude())+"/"+String.valueOf(location.getLongitude()),null,null);
                }
                //如果是到的短信是经纬度，那么，设置
                //对于判断收到的短信是经纬度，可以采用正则表达式，这里使用自己的标记
                System.out.println(fullMessage.trim().substring(0,4));
                //if(fullMessage.trim().substring(0,4).equals("flag"))
                if(fullMessage.trim().contains("/"))
                {
                    String[] s = fullMessage.split("/");
                    LatLng point = new LatLng(Double.valueOf(s[0])+0.002,Double.valueOf(s[1])+0.002);
                    //LatLng point = new LatLng(location.getLatitude()+0.02, location.getLongitude()+0.02);
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.blue);
                    OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
                    mBaiduMap.addOverlay(option);

                    //计算距离并放入属性，并显示，连线
                    LatLng middlepoint = new LatLng(location.getLatitude(), location.getLongitude());
                    double d = DistanceUtil.getDistance(point,middlepoint);
                    List<LatLng> points = new ArrayList<LatLng>();
                    points.add(middlepoint);
                    points.add(point);
                    OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAAAA0000).points(points);


                    LatLng latlng = new LatLng((location.getLatitude()+Double.valueOf(s[0]))/2, (location.getLongitude()+Double.valueOf(s[1]))/2);

                    OverlayOptions o2 = new TextOptions()
                            .position(latlng)
                            .text("距离"+d).fontSize(35)
                            .fontColor(0xAAAA0000);
	/**/
                    mBaiduMap.addOverlay(o2);
                    mBaiduMap.addOverlay(ooPolyline);
                }
            }
        }
        MessageReceiver messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, receiveFilter);


        //对 marker 添加点击相应事件
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "Marker被点击了！", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void setNavigate(double latitude, double longitude)
    {
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.blue);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        mBaiduMap.addOverlay(option);
    }

    private void navigateTo(Location location)
    {
        if(isFirstLocate)
        {
            //setNavigate(location.getLatitude(),location.getLongitude());

            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
            //定义Maker坐标点
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.blue);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
            /**/
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }
    LocationListener locationListener = new LocationListener()
    {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){
        }
        @Override
        public void onProviderEnabled(String provider){}
        @Override
        public void onProviderDisabled(String provider){}
        @Override
        public void onLocationChanged(Location location){
            if(location != null)
                navigateTo(location);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        if(locationManager != null)
        {
            locationManager.removeUpdates(locationListener);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}