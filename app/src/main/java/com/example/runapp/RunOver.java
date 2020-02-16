package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.runapp.sqlite.MyDbHelp;
import com.kongzue.dialog.v3.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RunOver extends AppCompatActivity implements View.OnClickListener {
    private MapView mapView;
    private BaiduMap mBaiduMap;
    private Polyline polyline;
    private TextView sum, time, speed, kcal;
    private Button close, share;
    private List<LatLng> latLng = new ArrayList<>();
    private MyDbHelp myDbHelp;
    private SQLiteDatabase sqLiteDatabase;

    //起点图标
    public BitmapDescriptor startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
    //终点图标
    public BitmapDescriptor finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_over);
        initView();
        setOnClickListener();
    }

    private void initView() {
        mapView = findViewById(R.id.bmapView);
        sum = findViewById(R.id.sum);
        time = findViewById(R.id.time);
        speed = findViewById(R.id.speed);
        kcal = findViewById(R.id.kcal);
        close = findViewById(R.id.btnClose);
        share = findViewById(R.id.btnShare);

        Intent intent = getIntent();
        String km = intent.getStringExtra("km");
        String date = intent.getStringExtra("date");
        String time1 = intent.getStringExtra("time");
        if (km!=null && date!=null && time1!=null){
            myDbHelp = new MyDbHelp(RunOver.this, "mydb.db", null, 1);
            sqLiteDatabase = myDbHelp.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from map_data", null);
            while (cursor.moveToNext()){
                String k = cursor.getString(cursor.getColumnIndex("distance"));
                String d = cursor.getString(cursor.getColumnIndex("month"));
                String t = cursor.getString(cursor.getColumnIndex("time"));
                if (k.equals(km) && d.equals(date) && t.equals(time1)){
                    String strSpeed = cursor.getString(cursor.getColumnIndex("speed"));
                    String strKcal = cursor.getString(cursor.getColumnIndex("kcal"));
                    String strRoute = cursor.getString(cursor.getColumnIndex("route"));

                    time.setText(t);
                    sum.setText(k);
                    speed.setText(strSpeed);
                    kcal.setText(strKcal);
                    latLng = valueOfList(strRoute);
                }
            }
            initMap();
            addRouteLine(latLng);
        }else {
            initMap();
            initData();
        }
        setOnClickListener();
    }

    private void initMap() {
        SDKInitializer.initialize(RunOver.this);/*初始化百度地图*/

        mBaiduMap = mapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);/*设置分支位置*/
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20));
    }

    private void initData() {
        myDbHelp = new MyDbHelp(RunOver.this, "mydb.db", null, 1);
        sqLiteDatabase = myDbHelp.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from map_data", null);
        while (cursor.moveToNext())
        {
            if (cursor.moveToLast())/*如果是最后一条数据*/
            {
                String strTime = cursor.getString(cursor.getColumnIndex("time"));
                String strDistance = cursor.getString(cursor.getColumnIndex("distance"));
                String strSpeed = cursor.getString(cursor.getColumnIndex("speed"));
                String strKcal = cursor.getString(cursor.getColumnIndex("kcal"));
                String strRoute = cursor.getString(cursor.getColumnIndex("route"));

                time.setText(strTime);
                sum.setText(strDistance);
                speed.setText(strSpeed);
                kcal.setText(strKcal);
                latLng = valueOfList(strRoute);
            }
        }
        addRouteLine(latLng);
    }

    private void setOnClickListener() {
        close.setOnClickListener(this);
        share.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                finish();
                break;
            case R.id.btnShare:
                List<ShareDialog.Item> itemList = new ArrayList<>();
                itemList.add(new ShareDialog.Item(RunOver.this ,R.drawable.qq,"QQ"));
                itemList.add(new ShareDialog.Item(RunOver.this ,R.drawable.wx,"微信"));
                ShareDialog.show(RunOver.this, itemList, new ShareDialog.OnItemClickListener() {
                    @Override
                    public boolean onClick(ShareDialog shareDialog, int index, ShareDialog.Item item) {
                        Toast.makeText(RunOver.this, "点击了：" + item.getText(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                break;
        }
    }

    //    解析json数据
    private List<LatLng> valueOfList(String str) {
        List<LatLng> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0;i < jsonArray.length();i++)
            {
                JSONObject obj = jsonArray.getJSONObject(i);
                String latitude = obj.getString("latitude");/*纬度*/
                String longitude = obj.getString("longitude");/*经度*/

                LatLng latLng = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                list.add(latLng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    //    添加路线
    public void addRouteLine(List<LatLng> lat){
        mBaiduMap.clear();
        if (lat.size() > 10000)
        {
            lat = lat.subList(0, 10000);
        }
        //标记起点图层位置
        MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
        oStart.position(lat.get(0));// 覆盖物位置点，第一个点为起点
        oStart.icon(startBD);// 设置覆盖物图片
        mBaiduMap.addOverlay(oStart); // 在地图上添加此图层

        mBaiduMap.addOverlay(new PolylineOptions().width(13).points(lat).color(Color.GREEN));/*画线*/
        moveToLocation(lat.get(lat.size() / 2));

        //标记终点图层位置
        MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
        oFinish.position(lat.get(lat.size() - 1));
        oFinish.icon(finishBD);// 设置覆盖物图片
        mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层
    }

    //    移动到指定位置并缩放
    public void moveToLocation(LatLng lat){
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(lat);/*设置新的起点*/
        mBaiduMap.animateMapStatus(mapStatusUpdate);
    }

    @Override
    protected void onDestroy() {
        mBaiduMap.setMyLocationEnabled(false);
//        图片回收
        startBD.recycle();
        finishBD.recycle();
        super.onDestroy();
    }
}
