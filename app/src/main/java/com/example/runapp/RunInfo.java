package com.example.runapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.runapp.sqlite.MyDbHelp;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.example.runapp.StartRunning.formatToNumber;

public class RunInfo extends AppCompatActivity implements View.OnClickListener {
    private TextView sum, hour, number;
    private RelativeLayout rl;
    private ImageView run;
    private MyDbHelp myDbHelp;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_info);
        findViewId();
        setOnClickListener();
    }

    private void findViewId() {
        sum = findViewById(R.id.sum);
        hour = findViewById(R.id.hour);
        number = findViewById(R.id.number);
        run = findViewById(R.id.run);
        rl = findViewById(R.id.rl);
    }

    private void setOnClickListener() {
        run.setOnClickListener(this);
        rl.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.run:
                startActivity(new Intent(RunInfo.this, StartRunning.class));
                break;
            case R.id.rl:
                startActivity(new Intent(RunInfo.this, RunFrequency.class));
                break;
        }
    }

    //    视图可见时执行
    @Override
    protected void onResume() {
        myDbHelp = new MyDbHelp(RunInfo.this, "mydb.db", null, 1);
        sqLiteDatabase = myDbHelp.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from map_data", null);
        int i = 0;/*统计跑步次数*/
        float hour1 = 0.0f;/*统计跑步小时*/
        float min1 = 0.0f;
        double mileage = 0.00;/*跑步里程*/
        while (cursor.moveToNext()) {
            String t = cursor.getString(cursor.getColumnIndex("time"));
            char c = t.charAt(0);
            char h = t.charAt(1);
            char a = t.charAt(3);
            char r = t.charAt(4);
            hour1 = Float.parseFloat(c + "" + h);

            int min = Integer.parseInt(a+"") + Integer.parseInt(r+"");
            min1 = min1 + min;

            String d = cursor.getString(cursor.getColumnIndex("distance"));
            mileage = mileage + Double.parseDouble(d);

            i++;
        }
        min1 = min1 / 60;
        hour1 = hour1 + min1;
        number.setText(i + "");
        hour.setText(KeepOneDecimalPlaces(new BigDecimal(hour1)) + "");/*只保留1位小数*/
        sum.setText(formatToNumber(new BigDecimal(mileage)) + "");/*只保留2位小数*/
        super.onResume();
    }

    //    转换为1位小数
    public static String KeepOneDecimalPlaces(BigDecimal obj) {
        DecimalFormat df = new DecimalFormat("#.0");
        if (obj.compareTo(BigDecimal.ZERO) == 0) {
            return "0.0";
        } else if (obj.compareTo(BigDecimal.ZERO) > 0 && obj.compareTo(new BigDecimal(1)) < 0) {
            return "0" + df.format(obj).toString();
        } else {
            return df.format(obj).toString();
        }
    }
}