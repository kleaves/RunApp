package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.runapp.adapter.DescHolder;
import com.example.runapp.adapter.HotelEntityAdapter;
import com.example.runapp.adapter.SectionedSpanSizeLookup;
import com.example.runapp.entity.HotelEntity;
import com.example.runapp.sqlite.MyDbHelp;
import com.example.runapp.utils.JsonUtils;
import com.google.gson.Gson;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.MessageDialog;

import java.util.ArrayList;
import java.util.List;

public class RunFrequency extends AppCompatActivity {
    LinearLayout return_i;
    MyDbHelp myDbHelp;
    SQLiteDatabase readableDatabase;
    RecyclerView mRecyclerView;
    HotelEntityAdapter mAdapter;
    String json;
    HotelEntity one;
    HotelEntity.TagsEntity two;
    HotelEntity.TagsEntity.TagInfo there;
    List<String> strs = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_frequency);
        getJsonData();
        initView();
        setOnClickListener();
    }

    private void getJsonData() {
        myDbHelp = new MyDbHelp(RunFrequency.this, "mydb.db", null, 1);
        readableDatabase = myDbHelp.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery("select * from map_data", null);
        ArrayList<HotelEntity> runRecordbeans = new ArrayList<>();
        ArrayList<HotelEntity.TagsEntity> twos = new ArrayList<>();
        ArrayList<HotelEntity.TagsEntity.TagInfo> theres = new ArrayList<>();
        int ii = cursor.getCount();
        System.out.println("ii"+ii);
        if (ii <= 0){
            System.out.println("ii"+ii);
            MessageDialog.show(RunFrequency.this,"无跑步数据！","","确定")
                    .setOnOkButtonClickListener(new OnDialogButtonClickListener() {
                        @Override
                        public boolean onClick(BaseDialog baseDialog, View v) {
                            finish();
                            return false;
                        }
                    });
            return;
        }
        while (cursor.moveToNext()) {
            one = new HotelEntity();
            two = one.new TagsEntity();
            there = new HotelEntity().new TagsEntity().new TagInfo();
            String year = cursor.getString(cursor.getColumnIndex("year"));

            String month = cursor.getString(cursor.getColumnIndex("month"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String distance = cursor.getString(cursor.getColumnIndex("distance"));
            String speed = cursor.getString(cursor.getColumnIndex("speed"));
            there.month = month;
            there.time = time;
            there.distance = distance;
            there.speed = speed;

            if (strs.contains(year)) {
                theres.add(there);
                two.tagInfoList = theres;
            }else {
                strs.add(year);
                theres = new ArrayList<>();/*重新实例化*/
                theres.add(there);
                two.year = year;
                two.tagInfoList = theres;
                twos.add(two);
            }
        }
        one.allTagsList = twos;
        runRecordbeans.add(one);
        cursor.close();
        readableDatabase.close();

        Gson gson = new Gson();
        String s = gson.toJson(runRecordbeans);
        if (s.startsWith("[") && s.endsWith("]"))
            json = s.substring(1, s.length() - 1);
        System.out.println(json);

    }

    private void initView() {
        return_i = findViewById(R.id.return_i);
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new HotelEntityAdapter(this);
        GridLayoutManager manager = new GridLayoutManager(this, 4);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置header
        manager.setSpanSizeLookup(new SectionedSpanSizeLookup(mAdapter, manager));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        HotelEntity entity = JsonUtils.analysisJsonFile(json);
        mAdapter.setData(entity.allTagsList);
    }

    private void setOnClickListener() {
        mAdapter.setOnItemClick(new HotelEntityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int section, int position, DescHolder holder) {
                String km = holder.km.getText().toString();
                String date = holder.date.getText().toString();
                String time = holder.time.getText().toString();
                Intent intent = new Intent(RunFrequency.this,RunOver.class);
                intent.putExtra("km",km);
                intent.putExtra("date",date);
                intent.putExtra("time",time);
                startActivity(intent);
            }
        });

        return_i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}