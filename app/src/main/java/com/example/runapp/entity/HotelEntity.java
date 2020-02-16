package com.example.runapp.entity;

import java.util.ArrayList;

/**
 * Created by yushuangping on 2018/8/23.
 */

public class HotelEntity {
    public ArrayList<TagsEntity> allTagsList;

    public class TagsEntity {
        public String year;/*未展开的名字*/
        public ArrayList<TagInfo> tagInfoList;/*未展开名字的列*/

        public class TagInfo {
            public String month;
            public String time;
            public String distance;
            public String speed;
        }
    }

}
