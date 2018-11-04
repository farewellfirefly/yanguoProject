package pku.yanguoweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pku.yanguoweather.app.MyApplication;
import pku.yanguoweather.bean.City;

public class SelectCity extends Activity implements View.OnClickListener{//选择城市
    private ImageView mBackBtn;
    private ListView mList;
    private List<City>cityList;
    private List<String>filterDateList=new ArrayList<>();//存储城市名称
    private List<String> oringinalList=new ArrayList<>();//存储城市代码
    private HashMap<String,String> map=new HashMap<>();// 查询过程中使用
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        initViews();
    }
    private void initViews(){
        //为mBackBtn设置监听事件
        mBackBtn=(ImageView)findViewById(R.id.title_back);//返回选项
        mBackBtn.setOnClickListener(this);
        //mClearEditText =(ClearEditText)findViewById(R.id.search_city);
        mList=(ListView)findViewById(R.id.title_list);
        MyApplication myApplication =(MyApplication)getApplication();
        cityList = myApplication.getCityList();
        for(City city:cityList){
            filterDateList.add(city.getCity());//存储城市名称
            oringinalList.add(city.getNumber());//存储城市代码
            map.put(city.getCity(),city.getNumber());//存储到hashmap中
        }
        ArrayAdapter<String> myadapter=new ArrayAdapter<String>(
                SelectCity.this,android.R.layout.simple_list_item_1,filterDateList
        );
        mList.setAdapter(myadapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName=filterDateList.get(position);
                String cityCode=map.get(cityName);
                Intent i = new Intent();
                i.putExtra("cityCode",cityCode);
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back://返回主界面
                Intent i = new Intent();
                i.putExtra("cityCode","101160101");//如果返回，则显示兰州的天气
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
}
