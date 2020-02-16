package com.example.runapp.utils;

import com.example.runapp.entity.HotelEntity;
import com.google.gson.Gson;

/**
 * Created by yushuangping on 2018/8/23.
 */

public class JsonUtils {

    public static HotelEntity analysisJsonFile(String jsonData){
        Gson gson = new Gson();
        HotelEntity entity = gson.fromJson(jsonData, HotelEntity.class);
        return  entity;

    }
}
