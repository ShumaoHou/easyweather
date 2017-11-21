package com.mndream.easyweather.db;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Administrator on 2017/11/15.
 * 数据库映射对象，省
 */

@Table(database = WeatherDatabase.class)
public class Province extends BaseModel {

    @PrimaryKey(autoincrement = true)
    private long id;

    @Column
    private String name;   //名称

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
