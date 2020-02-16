package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.runapp.sqlite.MyDbHelp;
import com.google.gson.Gson;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.MessageDialog;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class StartRunning extends AppCompatActivity implements SensorEventListener,View.OnClickListener {
    private TextView count_down;
    private Timer timer;
    private int second1 = 3;
    private LinearLayout ll;


    private Button buttonMileage, buttonPause, finish;
    private ImageView iv_bottom;
    private RelativeLayout rl, rl_mapview;
    private TextView time, sum, speed, kcal;
    private int hour = 0, minute = 0, second = 0;
    private double km;//跑多少千米
    private boolean playAnimation = true, timeStop = true, pause = true;
    private Thread t,thread;
    private boolean threadRun = true;
    // 定位相关
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private TextView info;
    private RelativeLayout progressBarRl;

    private boolean isFirstLoc = true/*是否首次定位*/, fail = true/*是否定位成功*/;
    private MyLocationData locData;
    private float mCurrentZoom = 19f;//默认地图缩放比例值

    private SensorManager mSensorManager;
    //    跑步里程相关
    private double sum_distance = 0.00;
    private final double EARTH_RADIUS = 6378137.0;

    //起点图标
    private BitmapDescriptor startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
    //终点图标
    private BitmapDescriptor finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);

    private List<LatLng> points = new ArrayList<LatLng>();//位置点集合
    private Polyline mPolyline;//运动轨迹图层
    private LatLng last = new LatLng(0, 0);//上一个定位点
    private MapStatus.Builder builder;

    private MyDbHelp mySQLHelp;
    private SQLiteDatabase writableDatabase;

    private ObjectAnimator translation, objectAnimator;
    private RotateAnimation rotateAnimation;

    private int frequency = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        getWindow().setFlags(flag, flag);
        setContentView(R.layout.activity_start_running);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=

                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        initView();
        timer.schedule(timerTask, 1000, 1000);//等待时间一秒，停顿时间一秒
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, //定位模式 地图SDK支持三种定位模式：NORMAL（普通态）, FOLLOWING（跟随态）, COMPASS（罗盘态）
                true,
                null));//自定义定位图标样式，替换定位icon//自定义精度圈填充颜色

        /**
         * 添加地图缩放状态变化监听，当手动放大或缩小地图时，拿到缩放后的比例，然后获取到下次定位，
         *  给地图重新设置缩放比例，否则地图会重新回到默认的mCurrentZoom缩放比例
         */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                mCurrentZoom = arg0.zoom;
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {
                // TODO Auto-generated method stub

            }
        });

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//只用gps定位，需要在室外定位。
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//设置多久定位一次，0表示只定位一次
        mLocClient.setLocOption(option);//设置locationClientOption(锁定选项)
        if (mLocClient != null && !mLocClient.isStarted()) {
            mLocClient.start();
            progressBarRl.setVisibility(View.VISIBLE);//设置RelativeLayout布局的可见性
            info.setText("GPS信号搜索中，请稍后...");
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (fail) {
                            sleep(10000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    info.setText("GPS信号弱，请稍后...");
                                    Toast.makeText(StartRunning.this, "请移步到空旷地方进行定位", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            mBaiduMap.clear();
        }
        setOnClickListener();
    }

    private void initView() {
        count_down = findViewById(R.id.count_down);
        ll = findViewById(R.id.ll);
        buttonMileage = findViewById(R.id.buttonMileage);
        buttonPause = findViewById(R.id.buttonPause);
        finish = findViewById(R.id.buttonFinish);
        iv_bottom = findViewById(R.id.iv_bottom);
        sum = findViewById(R.id.sum);
        time = findViewById(R.id.time);
        speed = findViewById(R.id.speed);
        kcal = findViewById(R.id.kcal);
        rl = findViewById(R.id.rl);
        rl_mapview = findViewById(R.id.rl_mapview);
        info = findViewById(R.id.info);
        progressBarRl = findViewById(R.id.progressBarRl);

        timer = new Timer();
    }

    public void setOnClickListener() {
        buttonMileage.setOnClickListener(this);
        buttonPause.setOnClickListener(this);
        finish.setOnClickListener(this);
        iv_bottom.setOnClickListener(this);
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            second1--;
            if (second1 < 0)
            {
                timerTask.cancel();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (second1 > 0)
                            {
                                count_down.setText(String.valueOf(second1));
                            }else if (second1 == 0){
                                count_down.setText("Go");
                            }else {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);/*取消全屏*/
                                ll.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }).start();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            继续按钮

            case R.id.buttonMileage:
                if (pause == false) {
                    pause = true;
                }
                float p = buttonMileage.getTranslationX();
                translationAnimator(buttonMileage, "translationX", p, 0f, buttonMileage);

                float p1 = finish.getTranslationX();
                translationAnimator(finish, "translationX", p1, 0f, buttonMileage);

                break;
//                暂停按钮
            case R.id.buttonPause:

                if (pause == true) {
                    pause = false;
                }
                buttonMileage.setVisibility(View.VISIBLE);//设置可见或不可见
                finish.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.INVISIBLE);

                float pause = buttonMileage.getTranslationX();
                translationAnimator(buttonMileage, "translationX", pause, -180f, buttonPause);

                float fini = finish.getTranslationX();
                translationAnimator(finish, "translationX", fini, 180f, buttonPause);
                break;
//                停止按钮
            case R.id.buttonFinish:
                stop();
                break;
//                点击图片向下缩小或拉长
            case R.id.iv_bottom:
                action();
                break;
        }
    }


    private void stop() {
        double v = Double.parseDouble(sum.getText().toString());
        if (points.size() < 2 /*|| v < 0.20*/) {
            MessageDialog.show(StartRunning.this, "提示", "本次运动距离过短，将不会记录数据，是否结束？", "结束运动", "继续运动")
                    .setOnOkButtonClickListener(new OnDialogButtonClickListener() {
                        @Override
                        public boolean onClick(BaseDialog baseDialog, View v) {
                            fail = false;
                            finish();
                            return false;
                        }
                    });
        } else {
            MessageDialog.show(StartRunning.this, "提示", "是否结束运动？", "结束运动", "继续运动")
                    .setOnOkButtonClickListener(new OnDialogButtonClickListener() {
                        @Override
                        public boolean onClick(BaseDialog baseDialog, View v) {
                            if (mLocClient != null && mLocClient.isStarted()) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Gson gson = new Gson();
                                        String s = gson.toJson(points);
                                        mySQLHelp = new MyDbHelp(StartRunning.this, "mydb.db", null, 1);
                                        writableDatabase = mySQLHelp.getWritableDatabase();
                                        ContentValues contentValues = new ContentValues();
                                        Date date = new Date();
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                                        SimpleDateFormat format2 = new SimpleDateFormat("MM-dd");
                                        contentValues.put("year", format.format(date));
                                        contentValues.put("month", format2.format(date));
                                        contentValues.put("time", time.getText().toString());
                                        contentValues.put("distance", sum.getText().toString());
                                        contentValues.put("speed", speed.getText().toString());
                                        contentValues.put("kcal", kcal.getText().toString());
                                        contentValues.put("route", s);
                                        writableDatabase.insert("map_data", null, contentValues);
                                        writableDatabase.close();

                                        mLocClient.stop();
//                    设置RelativeLayout布局的可见性
                                        progressBarRl.setVisibility(View.GONE);

                                        if (isFirstLoc) {
                                            points.clear();
                                            last = new LatLng(0, 0);
                                            return;
                                        }

                                        MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
                                        oFinish.position(points.get(points.size() - 1));
                                        oFinish.icon(finishBD);// 设置覆盖物图片
                                        mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层

                                        //复位
                                        points.clear();
                                        last = new LatLng(0, 0);
                                        isFirstLoc = true;
                                        timeStop = false;
                                        if (!thread.equals(null)) {
                                            computingTime();
                                            thread.interrupt();//打断、停止线程
                                        }
                                        threadRun = false;
                                        sum_distance = 0.00;
                                    }
                                }).start();
                                startActivity(new Intent(StartRunning.this, RunOver.class));
                                finish();
                            }
                            return false;
                        }
                    });
        }
    }

    private void action() {
        if (playAnimation) {
//                    属性动画的平移动画，(平移控件，平移的轴，平移开始位置，平移到哪个位置a，平移到位置a后平移去哪里)
            float distance = rl.getTranslationX();
            translation = ObjectAnimator.ofFloat(rl, "translationY", distance, 660f);
            translation.setDuration(500);
            translation.start();

//                      补间动画的旋转动画
            rotateAnimation = new RotateAnimation(
                    0, 180,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            rotateAnimation.setFillAfter(true);//设置动画是否停留在最后一帧

            iv_bottom.startAnimation(rotateAnimation);

            playAnimation = false;

            DisplayMetrics metrics = new DisplayMetrics();//获取屏幕宽度
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;//得到屏幕的高度
//                    定义布局参数
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.height = (int) (height * 0.946);//为layoutParams设置高度
            rl_mapview.setLayoutParams(layoutParams);//将高度赋值给需要改变高度的布局
        } else if (!playAnimation) {
            float distance = rl.getTranslationX();
//                    属性动画的平移动画，(平移控件，平移的轴，平移开始位置，平移到哪个位置a，平移到位置a后平移去哪里)
            translation = ObjectAnimator.ofFloat(rl, "translationY", 660f, distance);
            translation.setDuration(500);
            translation.start();
//                      补间动画的旋转动画
            rotateAnimation = new RotateAnimation(
                    180, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            rotateAnimation.setFillAfter(true);//设置动画是否停留在最后一帧

            iv_bottom.startAnimation(rotateAnimation);

            playAnimation = true;
            rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                //                        动画开始播放时执行
                @Override
                public void onAnimationStart(Animation animation) {

                }

                //                        动画播放结束时执行
                @Override
                public void onAnimationEnd(Animation animation) {
                    DisplayMetrics metrics = new DisplayMetrics();//获取屏幕宽度
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int height = metrics.heightPixels;//得到屏幕的高度
//                    定义布局参数
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.height = (int) (height * 0.655);//为layoutParams设置高度
                    rl_mapview.setLayoutParams(layoutParams);//将高度赋值给需要改变高度的布局
                }

                //                        动画播放重复时执行
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void translationAnimator(final Button button, String propertyName, float p, float v, final Button button1) {
        objectAnimator = ObjectAnimator.ofFloat(button, propertyName, p, v);
        objectAnimator.setDuration(500);
        objectAnimator.start();
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!button1.getText().toString().equals(buttonPause.getText().toString())) {
                    buttonMileage.setVisibility(View.INVISIBLE);
                    finish.setVisibility(View.INVISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    double lastX;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];

        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            if (isFirstLoc) {
                lastX = x;
                return;
            }
            locData = new MyLocationData.Builder().accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat).longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }

            //注意这里只接受gps点，需要在室外定位。
            if (location.getLocType() == BDLocation.TypeGpsLocation) {

                if (isFirstLoc) {//首次定位
                    //第一个点很重要，决定了轨迹的效果，gps刚开始返回的一些点精度不高，尽量选一个精度相对较高的起始点
                    LatLng ll = null;

                    ll = getMostAccuracyLocation(location);
                    if (ll == null) {
                        return;
                    }
                    fail = false;
                    if (t != null)
                    {
                        t.interrupt();
                    }
                    isFirstLoc = false;
                    points.add(ll);//加入集合
                    last = ll;

                    //显示当前定位点，缩放地图
                    locateAndZoom(location, ll);

                    //标记起点图层位置
                    MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
                    oStart.position(points.get(0));// 覆盖物位置点，第一个点为起点
                    oStart.icon(startBD);// 设置覆盖物图片
                    mBaiduMap.addOverlay(oStart); // 在地图上添加此图层

                    progressBarRl.setVisibility(View.GONE);

                    return;//画轨迹最少得2个点，首地定位到这里就可以返回了
                }
                //从第二个点开始
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                //sdk回调gps位置的频率是1秒1个，位置点太近动态画在图上不是很明显，可以设置点之间距离小于为5米才添加到集合中
                if (DistanceUtil.getDistance(last, ll) > 10) {
                    return;
                }

                points.add(ll);//如果要运动完成后画整个轨迹，位置点都在这个集合中

//                得到所跑的公里
                LatLng p0 = last;
                LatLng last1 = new LatLng(location.getLatitude(), location.getLongitude());
                double m = gps2m(p0.latitude, p0.longitude, last1.latitude, last1.longitude);
                sum_distance = sum_distance + m;
                km = sum_distance / 1000;
                // 保留两位小数
                sum.setText(formatToNumber(new BigDecimal(km)));

                last = ll;

                //显示当前定位点，缩放地图
                locateAndZoom(location, ll);

                //清除上一次轨迹，避免重叠绘画
                mMapView.getMap().clear();

                //起始点图层也会被清除，重新绘画
                MarkerOptions oStart = new MarkerOptions();
                oStart.position(points.get(0));
                oStart.icon(startBD);
                mBaiduMap.addOverlay(oStart);

                //将points集合中的点绘制轨迹线条图层，显示在地图上
                OverlayOptions ooPolyline = new PolylineOptions().width(13).color(Color.GREEN).points(points);
                mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
            }
        }
    }

    //    转换为两位小数
    public static String formatToNumber(BigDecimal obj) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (obj.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00";
        } else if (obj.compareTo(BigDecimal.ZERO) > 0 && obj.compareTo(new BigDecimal(1)) < 0) {
            return "0" + df.format(obj).toString();
        } else {
            return df.format(obj).toString();
        }
    }

    private void locateAndZoom(final BDLocation location, LatLng ll) {
        mCurrentLat = location.getLatitude();
        mCurrentLon = location.getLongitude();
        locData = new MyLocationData.Builder().accuracy(0)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mCurrentDirection).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);

        builder = new MapStatus.Builder();
        builder.target(ll).zoom(mCurrentZoom);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(19));
//        当定位成功后开始计运动时间
        computingTime();
        thread.start();
    }

    private void computingTime() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (threadRun) {
                    threadRun = false;
                    while (timeStop) {
                        try {
                            sleep(1000);
                            if (pause == true) {
                                second = second + 1;
                                if (second == 60) {
                                    second = 0;
                                    minute++;
                                    if (minute == 60) {
                                        minute = 0;
                                        hour++;
                                    }
                                }
                                if (hour < 10) {
                                    if (minute < 10) {
                                        if (second < 10) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText("" + 0 + hour + ":" + 0 + minute + ":" + 0 + second);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText("" + 0 + hour + ":" + 0 + minute + ":" + second);
                                                }
                                            });
                                        }
                                    } else {
                                        if (second < 10) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText("" + 0 + hour + ":" + minute + ":" + 0 + second);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText("" + 0 + hour + ":" + minute + ":" + second);
                                                }
                                            });
                                        }
                                    }
                                } else if (hour >= 10) {
                                    if (minute >= 10 && minute < 60) {
                                        if (second < 10) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText(hour + ":" + minute + ":" + 0 + second);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText(hour + ":" + minute + ":" + second);
                                                }
                                            });
                                        }
                                    } else {
                                        if (second < 10) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText(hour + ":" + minute + ":" + 0 + second);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    time.setText(hour + ":" + minute + ":" + second);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        frequency++;
                        if (frequency % 8 == 0)
                        {
                            final double spe = ((hour * 60) + minute) / km;
                            final double kca = (60 * Double.parseDouble(sum.getText().toString())) * 1.036;//计算卡路里 = 体重 * 距离 * 1.036
                            if (hour == 0 && minute != 0 && km != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speed.setText(formatToNumber(new BigDecimal(spe)) + "");
                                        kcal.setText(formatToNumber(new BigDecimal(kca)));
                                    }
                                });
                            } else if (hour != 0 && minute != 0 && second != 0 && km != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speed.setText(formatToNumber(new BigDecimal(spe)) + "");
                                        kcal.setText(formatToNumber(new BigDecimal(kca)));
                                    }
                                });
                            } else if (hour != 0 && minute == 0 && second != 0 && km != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speed.setText(formatToNumber(new BigDecimal(spe)) + "");
                                        kcal.setText(formatToNumber(new BigDecimal(kca)));
                                    }
                                });
                            } else if (hour == 0 && minute == 0 && second != 0 && km != 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speed.setText(formatToNumber(new BigDecimal(spe)) + "");
                                        kcal.setText(formatToNumber(new BigDecimal(kca)));
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 首次定位很重要，选一个精度相对较高的起始点
     * 注意：如果一直显示gps信号弱，说明过滤的标准过高了，
     * 你可以将location.getRadius()>25中的过滤半径调大，比如>40，
     * 并且将连续5个点之间的距离DistanceUtil.getDistance(last, ll ) > 5也调大一点，比如>10，
     * 这里不是固定死的，你可以根据你的需求调整，如果你的轨迹刚开始效果不是很好，你可以将半径调小，两点之间距离也调小，
     * gps的精度半径一般是10-50米
     */
    private LatLng getMostAccuracyLocation(BDLocation location) {

        if (location.getRadius() > 30) {//gps位置精度大于30米的点直接弃用
            return null;
        }

        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

        if (DistanceUtil.getDistance(last, ll) > 10) {
            last = ll;
            points.clear();//有任意连续两点位置大于10，重新取点
            return null;
        }
        points.add(ll);
        last = ll;
        //有3个连续的点之间的距离小于10，认为gps已稳定，以最新的点为起始点
        if (points.size() >= 3) {
            points.clear();
            return ll;
        }
        return null;
    }

    //    获取累计公里
    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        //s = Math.round(s * 10000) / 10000;
        return 1.38 * s;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    //    活动不再可见时执行
    @Override
    protected void onStop() {
        moveTaskToBack(false);/*调用moveTaskToBack可以让程序退出到后台运行，false表示只对主界面生效，true表示任何界面都可以生效*/
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 退出时销毁定位
        mLocClient.unRegisterLocationListener(myListener);
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.getMap().clear();
        mMapView.onDestroy();
        mMapView = null;
        startBD.recycle();
        finishBD.recycle();
//        停止线程
        if (thread != null) {
            thread.interrupt();
        }
        timeStop = false;
        threadRun = false;
        fail = false;
        super.onDestroy();
    }

}
