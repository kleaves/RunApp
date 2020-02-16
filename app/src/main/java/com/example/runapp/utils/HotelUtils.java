package com.example.runapp.utils;

import java.util.List;

/**
 * Created by yushuangping on 2018/8/23.
 */

public class HotelUtils {
    public static <D> boolean isEmpty(List<D> list) {
        return list == null || list.isEmpty();
    }
}
