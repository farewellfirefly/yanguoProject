package pku.yanguoweather.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pku.yanguoweather.bean.City;
import pku.yanguoweather.db.CityDB;

public class MyApplication extends Application{
    private static final String TAG ="MyAPP";
    private static MyApplication mApplication;
    private CityDB mCityDB;

    private List<City> mCityList;

    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication = this;
        mCityDB = openCityDB();
        initCityList();
    }
    private void initCityList(){//初始化城市列表
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
// TODO Auto-generated method stub
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        int i=0;
        for (City city : mCityList) {
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }
    public List<City> getCityList() {
        return mCityList;
    }
    public static MyApplication getInstance(){
        return mApplication;
    }
    private CityDB openCityDB() {//打开数据库
        String path = "/data"//data目录
                + Environment.getDataDirectory().getAbsolutePath
                ()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        if (!db.exists()) {//如果数据库不存在
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {//读入数据库数据
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }
}
