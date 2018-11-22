package pku.yanguoweather;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import pku.yanguoweather.bean.TodayWeather;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pku.yanguoweather.util.MyLocationListener;
import pku.yanguoweather.util.NetUtil;

public class MyActivity extends AppCompatActivity implements View.OnClickListener,ViewPager.OnPageChangeListener {
    //private EditText editText;
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int DB = 1;
    String cityvalue = "101010100";//默认北京天气
    String citynow = "北京";//默认当前城市是北京
    private ImageView mUpdateBtn;//更新按钮
    private ImageView mCitySelect;//选择城市
    private ImageView btn_start_location;//定位
    private ProgressBar progressBar;
    private LocationManager locationManager;
    public LocationClient mLocationClient=null;
    private MyLocationListener myListener=new MyLocationListener();

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv, wenduTv;//天气信息的文字
    //六天天⽓气信息展示
    //显示两个展示⻚页
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    //为引导页增加⼩小圆点
    private ImageView[] dots; //存放小圆点的集合
    private int[] ids = {R.id.iv1, R.id.iv2};
    private TextView
            week_today, temperature, climate, wind, week_today1, temperature1, climate1, wind1, week_today2, temperature2, climate2, wind2;
    private TextView
            week_today3, temperature3, climate3, wind3, week_today4, temperature4, climate4,
            wind4, week_today5, temperature5, climate5, wind5;
    private ImageView weatherImg, pmImg;
    private ImageView weatherimg, weatherimg1, weatherimg2;
    private ImageView weatherimg3, weatherimg4, weatherimg5;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {//消息机制传递消息
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {// onsaveInsanceState方法是用来保存Activity的状态的。当一个Activity在生命周期结束前，会调用该方法保存状态。
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        setContentView(R.layout.yanguo_layout);
        btn_start_location = (ImageView)findViewById(R.id.title_location);
        //setContentView(R.layout.sixday1);
        //setContentView(R.layout.sixday2);
        //editText=findViewById(R.id.edittext);
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        progressBar = findViewById(R.id.title_update_progress);
        mUpdateBtn.setOnClickListener(this);
        mLocationClient=new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(myListener);//注册监听函数
        initLocation();
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {//检查网络状态
            Log.d("myWeather", "网络OK");
            Toast.makeText(MyActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MyActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
        mCitySelect = findViewById(R.id.title_city_manager);//获取选择城市按钮
        mCitySelect.setOnClickListener(this);//监听选择城市按钮
        btn_start_location.setOnClickListener(this);//监听定位按钮

        //初始化两个滑动页⾯面
        initViews();
        //小圆点初始化
        initDots();
        initView();//初始化控件

        showweather(cityvalue);


    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span=0;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
    //初始化⼩小圆点
    void initDots() {
        dots = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            dots[i] = (ImageView) findViewById(ids[i]);
        }
    }

    //六天天⽓气信息展示
    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.sixday1, null));
        views.add(inflater.inflate(R.layout.sixday2, null));
        vpAdapter = new ViewPagerAdapter(views, this);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        vp.addOnPageChangeListener(this);//为pageviewer配置监听事件
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int
            positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        for (int a = 0; a < ids.length; a++) {
            if (a == position) {
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            } else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
    private void changeWeatherImage(String climate, ImageView weatherImg) {
        if (climate.equals("暴雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        if (climate.equals("暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        if (climate.equals("大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        if (climate.equals("大雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        if (climate.equals("大雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        if (climate.equals("多云"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        if (climate.equals("雷阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        if (climate.equals("雷阵雨冰雹"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        if (climate.equals("晴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        if (climate.equals("沙尘暴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        if (climate.equals("特大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        if (climate.equals("雾"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        if (climate.equals("小雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        if (climate.equals("小雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        if (climate.equals("阴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        if (climate.equals("雨夹雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        if (climate.equals("阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        if (climate.equals("阵雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        if (climate.equals("中雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        if (climate.equals("中雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
    }
    void updateTodayWeather(TodayWeather todayWeather) {//根据TodayWeather获取的信息更新控件内容
        if (mUpdateBtn.getVisibility() == View.GONE && progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
            mUpdateBtn.setVisibility(View.VISIBLE);
        }
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow() + "~" + todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());
        wenduTv.setText("当前温度：" + todayWeather.getWendu() + "℃");
        //weatherImg.setImageResource();
        //新增六天天气信息
        week_today.setText(todayWeather.getWeek_today());
        temperature.setText(todayWeather.getTemperatureL() + "~" + todayWeather.getTemperatureH());
        climate.setText(todayWeather.getClimate());
        wind.setText(todayWeather.getWind());
        String a = todayWeather.getClimate();
        changeWeatherImage(a,weatherimg);
        //weatherimg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        //
        week_today1.setText(todayWeather.getWeek_today1());
        temperature1.setText(todayWeather.getTemperatureL1() + "~" + todayWeather.getTemperatureH1());
        climate1.setText(todayWeather.getClimate1());
        wind1.setText(todayWeather.getWind1());
        String b = todayWeather.getClimate1();
        changeWeatherImage(b,weatherimg1);
        //
        week_today2.setText(todayWeather.getWeek_today2());
        temperature2.setText(todayWeather.getTemperatureL2() + "~" + todayWeather.getTemperatureH2());
        climate2.setText(todayWeather.getClimate2());
        wind2.setText(todayWeather.getWind2());
        String c = todayWeather.getClimate2();
        changeWeatherImage(c,weatherimg2);
        //
        week_today3.setText(todayWeather.getWeek_today3());
        temperature3.setText(todayWeather.getTemperatureL3() + "~" + todayWeather.getTemperatureH3());
        climate3.setText(todayWeather.getClimate3());
        wind3.setText(todayWeather.getWind3());
        String d = todayWeather.getClimate3();
        changeWeatherImage(d,weatherimg3);
        //
        week_today4.setText(todayWeather.getWeek_today4());
        temperature4.setText(todayWeather.getTemperatureL4() + "~" + todayWeather.getTemperatureH4());
        climate4.setText(todayWeather.getClimate4());
        wind4.setText(todayWeather.getWind4());
        String e = todayWeather.getClimate4();
        changeWeatherImage(e,weatherimg4);
        //
        week_today5.setText(todayWeather.getWeek_today5());
        temperature5.setText(todayWeather.getTemperatureL5() + "~" + todayWeather.getTemperatureH5());
        climate5.setText(todayWeather.getClimate5());
        wind5.setText(todayWeather.getWind5());
        String f = todayWeather.getClimate5();
        changeWeatherImage(f,weatherimg5);
        //

        if (todayWeather.getPm25() != null) {
            int pm2_5 = Integer.parseInt(todayWeather.getPm25());
            if (pm2_5 <= 50)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            if (pm2_5 > 50 && pm2_5 <= 100)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            if (pm2_5 > 100 && pm2_5 <= 150)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            if (pm2_5 > 150 && pm2_5 <= 200)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            if (pm2_5 > 200 && pm2_5 <= 300)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            if (pm2_5 > 300)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }
        //根据解析的天气类型更新界面的天气图案
        String climate = todayWeather.getType();
        changeWeatherImage(climate,weatherImg);
        /*
        if (climate.equals("暴雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        if (climate.equals("暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        if (climate.equals("大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        if (climate.equals("大雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        if (climate.equals("大雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        if (climate.equals("多云"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        if (climate.equals("雷阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        if (climate.equals("雷阵雨冰雹"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        if (climate.equals("晴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        if (climate.equals("沙尘暴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        if (climate.equals("特大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        if (climate.equals("雾"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        if (climate.equals("小雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        if (climate.equals("小雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        if (climate.equals("阴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        if (climate.equals("雨夹雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        if (climate.equals("阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        if (climate.equals("阵雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        if (climate.equals("中雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        if (climate.equals("中雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        Toast.makeText(MyActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();*/
    }

    void initView() {//初始化控件，并将控件内容设置成N/A
        week_today = views.get(0).findViewById(R.id.week_today);
        temperature = views.get(0).findViewById(R.id.temperature);
        climate = views.get(0).findViewById(R.id.climate);
        wind = views.get(0).findViewById(R.id.wind);
        weatherimg=(ImageView)views.get(0).findViewById(R.id.weather_img);
        //
        week_today1 = views.get(0).findViewById(R.id.week_today1);
        temperature1 = views.get(0).findViewById(R.id.temperature1);
        climate1 = views.get(0).findViewById(R.id.climate1);
        wind1 = views.get(0).findViewById(R.id.wind1);
        weatherimg1=(ImageView) views.get(0).findViewById(R.id.weather_img1);
        //
        week_today2 = views.get(0).findViewById(R.id.week_today2);
        temperature2 = views.get(0).findViewById(R.id.temperature2);
        climate2 = views.get(0).findViewById(R.id.climate2);
        wind2 = views.get(0).findViewById(R.id.wind2);
        weatherimg2=(ImageView) views.get(0).findViewById(R.id.weather_img2);
        //
        week_today3 = views.get(1).findViewById(R.id.week_today3);
        temperature3 = views.get(1).findViewById(R.id.temperature3);
        climate3 = views.get(1).findViewById(R.id.climate3);
        wind3 = views.get(1).findViewById(R.id.wind3);
        weatherimg3=(ImageView) views.get(1).findViewById(R.id.weather_img3);
        //
        week_today4 = views.get(1).findViewById(R.id.week_today4);
        temperature4 = views.get(1).findViewById(R.id.temperature4);
        climate4 = views.get(1).findViewById(R.id.climate4);
        wind4 = views.get(1).findViewById(R.id.wind4);
        weatherimg4=(ImageView) views.get(1).findViewById(R.id.weather_img4);
        //
        week_today5 = views.get(1).findViewById(R.id.week_today5);
        temperature5 = views.get(1).findViewById(R.id.temperature5);
        climate5 = views.get(1).findViewById(R.id.climate5);
        wind5 = views.get(1).findViewById(R.id.wind5);
        weatherimg5=(ImageView) views.get(1).findViewById(R.id.weather_img5);

        city_name_Tv = findViewById(R.id.title_city_name);
        cityTv = findViewById(R.id.city);
        timeTv = findViewById(R.id.time);
        humidityTv = findViewById(R.id.humidity);
        weekTv = findViewById(R.id.week_todayTT);
        pmDataTv = findViewById(R.id.pm_data);
        pmQualityTv = findViewById(R.id.pm2_5_quality);
        pmImg = findViewById(R.id.pm2_5_img);
        temperatureTv = findViewById(R.id.temperatureTT);
        climateTv = findViewById(R.id.climateTT);
        windTv = findViewById(R.id.windTT);
        weatherImg = findViewById(R.id.weather_imgTT);
        wenduTv = findViewById(R.id.wendu);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        wenduTv.setText("N/A");
        //
        week_today.setText("N/A");
        temperature.setText("N/A");
        climate.setText("N/A");
        wind.setText("N/A");
        //
        week_today1.setText("N/A");
        temperature1.setText("N/A");
        climate1.setText("N/A");
        wind1.setText("N/A");
        //
        week_today2.setText("N/A");
        temperature2.setText("N/A");
        climate2.setText("N/A");
        wind2.setText("N/A");
        //
        week_today3.setText("N/A");
        temperature3.setText("N/A");
        climate3.setText("N/A");
        wind3.setText("N/A");
        //
        week_today4.setText("N/A");
        temperature4.setText("N/A");
        climate4.setText("N/A");
        wind4.setText("N/A");
        //
        week_today5.setText("N/A");
        temperature5.setText("N/A");
        climate5.setText("N/A");
        wind5.setText("N/A");
    }

    void showweather(String cityvalue) {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code", cityvalue);
        Log.d("myWeather", cityCode);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {//检测网络状态
            Log.d("myWeather", "网络OK");
            queryWeatherCode(cityCode);
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MyActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            citynow = data.getStringExtra("citynow");
            cityvalue = newCityCode;
            Log.d("myWeather", "选择的城市代码为" + newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MyActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocate(){
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd0911");
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager) {//选择城市按钮
            Intent i = new Intent(this, SelectCity.class);
            //startActivity(i);
            i.putExtra("citynow", citynow);
            i.putExtra("cityvalue", cityvalue);
            startActivityForResult(i, 1);
        }
        if (view.getId() == R.id.title_update_btn) {//更新数据按钮
            mUpdateBtn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            showweather(cityvalue);
        }
        if(view.getId()==R.id.title_location){
            //转起来？
            startLocate();

            if(mLocationClient.isStarted()){
                mLocationClient.stop();
            }
            mLocationClient.start();
            final Handler BDHandler=new Handler(){
                public void handleMessage(Message msg){
                    switch (msg.what){
                        case UPDATE_TODAY_WEATHER:
                            if(msg.obj!=null){
                                if (NetUtil.getNetworkState(MyActivity.this) != NetUtil.NETWORN_NONE) {//检测网络状态
                                    Log.d("myWeather", "网络OK");
                                    queryWeatherCode(myListener.cityCode);
                                    cityvalue=myListener.cityCode;
                                } else {
                                    Log.d("myWeather", "网络挂了");
                                    Toast.makeText(MyActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
                                }
                            }
                            myListener.cityCode=null;
                            break;
                            default:
                                break;
                        }
                    }
                };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while(myListener.cityCode==null){
                            Thread.sleep(2000);
                        }
                        Message msg=new Message();
                        msg.what=UPDATE_TODAY_WEATHER;
                        msg.obj=myListener.cityCode;
                        BDHandler.sendMessage(msg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private TodayWeather parseXML(String xmldata) {//解析函数，解析获取的内容放入TodayWeather对象中
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;//风向
        int fengliCount = 0;//风力
        int dateCount = 0;//日期
        int highCount = 0;//最高温度
        int lowCount = 0;//最低温度
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {//根据名字更新todayWeather的信息
                                eventType = xmlPullParser.next(); //进入下一元素并触发相应事件
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                todayWeather.setWind1(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind2(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind3(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind4(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind5(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fl_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWind(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                todayWeather.setWeek_today1(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today2(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today3(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today4(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today5(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("date_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWeek_today(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureH1(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH5(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureH(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                todayWeather.setTemperatureL1(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL5(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setTemperatureL(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                todayWeather.setClimate1(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate2(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate3(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate4(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate5(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type_1")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setClimate(xmlPullParser.getText());
                            }
                            break;
                        }
                        //判断当前事件是否是标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private List<View> views;
        private Context context;

        public ViewPagerAdapter(List<View> views, Context context) {
            this.views = views;
            this.context = context;
        }

        @Override
        public int getCount() {//必须实现
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {//必须实现
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {//必须实现，实例化
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {//必须实现，销毁
            container.removeView(views.get(position));
        }

    }


    /**
     * @param cityCode
     */

    /*查询天气信息 根据区域代码查询*/
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;//绑定获取天气信息的APIkey
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {//建立线程
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection(
                    );
                    con.setRequestMethod("GET");//设置为get方式
                    con.setConnectTimeout(8000);//设置链接超时时间
                    con.setReadTimeout(8000);//设置读取超时时间
                    InputStream in = con.getInputStream();//输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//输入缓冲区
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {//若没读完，一直读，并添加到response
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);//解析获取的内容
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }
}





