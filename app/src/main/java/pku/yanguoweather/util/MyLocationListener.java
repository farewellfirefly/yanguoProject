package pku.yanguoweather.util;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;

import java.util.List;

import pku.yanguoweather.app.MyApplication;
import pku.yanguoweather.bean.City;



public class MyLocationListener extends BDAbstractLocationListener{
    public String recity;
    public String cityCode;
    @Override
    public void onReceiveLocation(BDLocation location) {
        String city =location.getCity();
        Log.d("location_city",city);
        recity=city.replace("市","");

        List<City> mCityList;
        MyApplication myApplication;
        myApplication=MyApplication.getInstance();
        mCityList=myApplication.getCityList();
        for(City cityl:mCityList){
            if(cityl.getCity().equals(recity)){
                cityCode=cityl.getNumber();
                Log.d("location_code",cityCode);
            }
        }
    }
}
