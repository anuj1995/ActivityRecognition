package com.example.activity_recognition.models

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import java.math.BigDecimal

class ActivityShapelet {
        @CsvBindByPosition(position = 0)
        var rowId:Int? =null
        @CsvBindByPosition(position = 1)
        var level_0: Int? = null
        @CsvBindByPosition(position = 2)
        var index: Int? = null
        @CsvBindByPosition(position = 3)
        var tick: Int? = null
        @CsvBindByPosition(position = 4)
        var timestamp: BigDecimal? = null
        @CsvBindByPosition(position = 5)
        var activity: Int? = null
        @CsvBindByPosition(position = 6)
        var x: Double? = null
        @CsvBindByPosition(position = 7)
        var y: Double? = null
        @CsvBindByPosition(position = 8)
        var z: Double? = null
        @CsvBindByPosition(position = 9)
        var user: Int? = null
    constructor() {}
    constructor(level_0:Int?, index:Int?, tick:Int?, timestamp:BigDecimal?,activity:Int?, x:Double?,  y: Double?,
                z:Double?, user:Int?, rowId:Int? ){
        this.activity = activity
        this.index = index
        this.level_0 = level_0
        this.timestamp = timestamp
        this.tick = tick
        this.x = x
        this.y = y
        this.z = y
        this.user = user
        this.rowId = rowId
    }

    override fun toString(): String {
        return "ActivitShapelet [level_0=" + level_0 + ", index=" + index + ", activity=" + activity+
                ", timeStamp=" + timestamp + ",tick=" + tick + ",x=" + x + ",y=" + y + ",z=" + z +
                ",user=" + user + "]"
    }

}