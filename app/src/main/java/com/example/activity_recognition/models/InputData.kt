package com.example.activity_recognition.models

import com.opencsv.bean.CsvBindByPosition

class InputData {
    @CsvBindByPosition(position = 0)
    var tick: Int? = null

    @CsvBindByPosition(position = 1)
    var timestamp: String? = null

    @CsvBindByPosition(position = 2)
    var activity: Int? = null

    @CsvBindByPosition(position = 3)
    var x: Double? = null

    @CsvBindByPosition(position = 4)
    var y: Double? = null

    @CsvBindByPosition(position = 5)
    var z: Double? = null

    constructor() {}
    constructor( tick:Int?, timestamp:String?,activity:Int?, x:Double?,  y: Double?, z:Double? ){
        this.activity = activity
        this.timestamp = timestamp
        this.tick = tick
        this.x = x
        this.y = y
        this.z = z
    }

    override fun toString(): String {
        return "Input data ~~ [activity=" + activity+ ", timeStamp=" + timestamp + ",tick=" + tick +
                ",x=" + x + ",y=" + y + ",z=" + z + "]"
    }
}