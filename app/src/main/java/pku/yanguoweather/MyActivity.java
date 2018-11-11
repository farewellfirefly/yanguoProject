package pku.yanguoweather;

import android.content.Intent;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
import pku.yanguoweather.util.NetUtil;

public class MyActivity extends AppCompatActivity implements View.OnClickListener{
    //private EditText editText;
    private static final int UPDATE_TODAY_WEATHER = 1;
    String cityvalue="101010100";//默认北京天气
    String citynow="北京";//默认当前城市是北京
    private ImageView mUpdateBtn;//更新按钮
    private ImageView mCitySelect;//选择城市
    private ProgressBar progressBar;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv,wenduTv;//天气信息的文字
    private ImageView weatherImg, pmImg;
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
        //editText=findViewById(R.id.edittext);
        mUpdateBtn=(ImageView)findViewById(R.id.title_update_btn);
        progressBar=findViewById(R.id.title_update_progress);
        mUpdateBtn.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {//检查网络状态
            Log.d("myWeather", "网络OK");
            Toast.makeText(MyActivity.this,"网络OK！", Toast.LENGTH_LONG).show();
        }else
        {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MyActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }
        mCitySelect = findViewById(R.id.title_city_manager);//获取选择城市按钮
        mCitySelect.setOnClickListener(this);//监听选择城市按钮
        initView();//初始化控件
        showweather(cityvalue);


    }
    void updateTodayWeather(TodayWeather todayWeather){//根据TodayWeather获取的信息更新控件内容
        if(mUpdateBtn.getVisibility()==View.GONE&&progressBar.getVisibility()==View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            mUpdateBtn.setVisibility(View.VISIBLE);
        }
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        wenduTv.setText("当前温度："+todayWeather.getWendu()+"℃");
        //weatherImg.setImageResource();
        if(todayWeather.getPm25()!=null){
            int pm2_5=Integer.parseInt(todayWeather.getPm25());
            if(pm2_5<=50)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            if(pm2_5>50&&pm2_5<=100)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            if(pm2_5>100&&pm2_5<=150)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            if(pm2_5>150&&pm2_5<=200)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            if(pm2_5>200&&pm2_5<=300)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            if(pm2_5>300)
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }
        //根据解析的天气类型更新界面的天气图案
        String climate=todayWeather.getType();
        if(climate.equals("暴雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
        if(climate.equals("暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
        if(climate.equals("大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
        if(climate.equals("大雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
        if(climate.equals("大雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
        if(climate.equals("多云"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
        if(climate.equals("雷阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
        if(climate.equals("雷阵雨冰雹"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
        if(climate.equals("晴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        if(climate.equals("沙尘暴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
        if(climate.equals("特大暴雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
        if(climate.equals("雾"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
        if(climate.equals("小雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
        if(climate.equals("小雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
        if(climate.equals("阴"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
        if(climate.equals("雨夹雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
        if(climate.equals("阵雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
        if(climate.equals("阵雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
        if(climate.equals("中雪"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
        if(climate.equals("中雨"))
            weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
        Toast.makeText(MyActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }
    void initView(){//初始化控件，并将控件内容设置成N/A
        city_name_Tv = findViewById(R.id.title_city_name);
        cityTv = findViewById(R.id.city);
        timeTv =  findViewById(R.id.time);
        humidityTv =  findViewById(R.id.humidity);
        weekTv =  findViewById(R.id.week_today);
        pmDataTv =  findViewById(R.id.pm_data);
        pmQualityTv =  findViewById(R.id.pm2_5_quality);
        pmImg = findViewById(R.id.pm2_5_img);
        temperatureTv =  findViewById(R.id.temperature);
        climateTv =  findViewById(R.id.climate);
        windTv =  findViewById(R.id.wind);
        weatherImg = findViewById(R.id.weather_img);
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
    }
    void showweather(String cityvalue){
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code",cityvalue);
        Log.d("myWeather",cityCode);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {//检测网络状态
            Log.d("myWeather", "网络OK");
            queryWeatherCode(cityCode);
        }else
        {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MyActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            citynow=data.getStringExtra("citynow");
            cityvalue=newCityCode;
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MyActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.title_city_manager){//选择城市按钮
            Intent i = new Intent(this,SelectCity.class);
            //startActivity(i);
            i.putExtra("citynow",citynow);
            i.putExtra("cityvalue",cityvalue);
            startActivityForResult(i,1);
        }
        if (view.getId() == R.id.title_update_btn){//更新数据按钮
            mUpdateBtn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            showweather(cityvalue);
        }
    }
    private TodayWeather parseXML(String xmldata){//解析函数，解析获取的内容放入TodayWeather对象中
        TodayWeather todayWeather =null;
        int fengxiangCount=0;//风向
        int fengliCount =0;//风力
        int dateCount=0;//日期
        int highCount =0;//最高温度
        int lowCount=0;//最低温度
        int typeCount =0;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while (eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if(todayWeather != null){
                        if (xmlPullParser.getName().equals("city")) {//根据名字更新todayWeather的信息
                            eventType = xmlPullParser.next(); //进入下一元素并触发相应事件
                            todayWeather.setCity(xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("updatetime")){
                            eventType = xmlPullParser.next();
                            todayWeather.setUpdatetime(xmlPullParser.getText());
                        }else if (xmlPullParser.getName().equals("shidu")) {
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
                            fengliCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                        eventType = xmlPullParser.next();
                            todayWeather.setDate(xmlPullParser.getText());
                        dateCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                            highCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setType(xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                        }
                    //判断当前事件是否是标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType= xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }


    /**
     *
     * @param cityCode
     */

    /*查询天气信息 根据区域代码查询*/
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;//绑定获取天气信息的APIkey
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {//建立线程
                HttpURLConnection con=null;
                TodayWeather todayWeather =null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection(
                    );
                    con.setRequestMethod("GET");//设置为get方式
                    con.setConnectTimeout(8000);//设置链接超时时间
                    con.setReadTimeout(8000);//设置读取超时时间
                    InputStream in = con.getInputStream();//输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//输入缓冲区
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){//若没读完，一直读，并添加到response
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather=parseXML(responseStr);//解析获取的内容
                    if (todayWeather!=null){
                        Log.d("myWeather",todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

}
