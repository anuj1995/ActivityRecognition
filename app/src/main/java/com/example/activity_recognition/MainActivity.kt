package com.example.activity_recognition

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import android.widget.Button
import androidx.core.util.forEach
import androidx.core.util.getOrDefault
import com.example.activity_recognition.models.ActivityShapelet
import com.example.activity_recognition.models.InputData
import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {

    var count = 0
    var ShapeletUri: Uri? = null
    var inputFileUri: Uri? = null

    private val ACTIVITY_CHOOSE_SHAPELET_FILE = 1
    private val ACTIVITY_CHOOSE_INPUT_FILE = 2
    private var allDistances = SparseArray<Double>()
    private val allWindows = SparseArray<Queue<Array<String>>>()
    var allActivityShapelets = SparseArray<List<ActivityShapelet>>()
    private val chunckData :Queue<Array<String>> = LinkedList()
    var time1:Long = 0
    var time2:Long = 0

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // load the shapelet files
        val selectFile = findViewById<Button>(R.id.load_shapelet)
        selectFile.setOnClickListener {
            val chooseFile: Intent = Intent(Intent.ACTION_GET_CONTENT)
            val intent: Intent
            chooseFile.type = "*/*"
            intent = Intent.createChooser(chooseFile, "Choose a File")
            startActivityForResult(intent, ACTIVITY_CHOOSE_SHAPELET_FILE)
        }

        // load input data
        val loadInputData = findViewById<Button>(R.id.load_input_data)
        loadInputData.setOnClickListener{
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            val intent: Intent
            chooseFile.type = "*/*"
            intent = Intent.createChooser(chooseFile, "Choose a File")
            startActivityForResult(intent, ACTIVITY_CHOOSE_INPUT_FILE)
        }

        // Start recognising activities
        val startInputStream = findViewById<Button>(R.id.start_input_stream)
        startInputStream.setOnClickListener{
            initAllWindows()
            val sizeMap = getShapeletSizeMap()
            val fileReadObservable= createFileReadObservable()
            fileReadObservable
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            //.delayEach(5,TimeUnit.MILLISECONDS)
            .subscribe {
                time1 = System.currentTimeMillis();
                count++
                //println(it?.get(3) + " | " + it?.get(4) + " | " + it?.get(5) + " | " + it?.get(6))
                it.forEach {
                    allWindows.forEach{activity,window ->
                        window.add(it)
                        if(window.size >= sizeMap.get(activity))
                        {
                            matchShapelet(ArrayList(window),allActivityShapelets.get(activity))
                            allWindows.get(activity).remove()
                        }
                    }
                }
                Log.d("count: ",count.toString())
                time2 = System.currentTimeMillis()
                printActivity()

            }

        }

    }


    fun createFileReadObservable(): Observable<ArrayList<Array<String>?>>{
        val fileReadObservable = Observable.create<ArrayList<Array<String>?>>{
                emitter ->
            val inputStream = inputFileUri?.let { it1 -> contentResolver.openInputStream(it1) }
            var csvReader: CSVReader = CSVReader(BufferedReader(InputStreamReader(inputStream!!)))
            var record: Array<String>?
            record = csvReader.readNext()
            while (record != null) {
                chunckData.add(record)
                if(chunckData.size>=300){
                    emitter.onNext(ArrayList(chunckData))
                    chunckData.remove()
                }

                record = csvReader.readNext()
            }
        }
        return fileReadObservable
    }

    fun loadActivityShapelet(filepath:Uri?):List<ActivityShapelet>{
        var fileReader: Reader?
        var csvToBean: CsvToBean<ActivityShapelet>?= null
        lateinit var activityShapelet: List<ActivityShapelet>
        val inputStream = filepath?.let { contentResolver.openInputStream(it) }
        try {
            fileReader = BufferedReader(InputStreamReader(inputStream!!))
            csvToBean = CsvToBeanBuilder<ActivityShapelet>(fileReader)
                .withSkipLines(1)
                .withType(ActivityShapelet::class.java)
                .withThrowExceptions(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build()

            activityShapelet = csvToBean.parse()
            Log.d("Shapelet_data: ","parsed")
            for (row in activityShapelet){
                println(row)
            }
        }
        catch (e:Exception){
            Log.e("File_loading", e.printStackTrace().toString())
        }
        return activityShapelet
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ACTIVITY_CHOOSE_SHAPELET_FILE){
            ShapeletUri = data?.data
            val filepath = ShapeletUri?.path
            Log.d("filepath",filepath.toString())
            val x = loadActivityShapelet(ShapeletUri)
            this.allActivityShapelets.put(x[0].activity!!,x)

        }
        else if(requestCode == ACTIVITY_CHOOSE_INPUT_FILE){
            inputFileUri = data?.data
            val filepath = inputFileUri?.path
            Log.d("filepath",filepath.toString())
        }
    }

    fun loadInputData(filepath:Uri?):List<InputData>{
        val fileReader: Reader?
        var csvToBean: CsvToBean<InputData>?= null
        lateinit var inputData: List<InputData>
        val inputStream = filepath?.let { contentResolver.openInputStream(it) }
        try {
            fileReader = BufferedReader(InputStreamReader(inputStream!!))
            csvToBean = CsvToBeanBuilder<InputData>(fileReader)
                .withType(InputData::class.java)
                .withThrowExceptions(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build()

            inputData = csvToBean.parse()
            Log.d("input_data: ","parsed")
            for (row in inputData){
                println(row)
            }
        }
        catch (e:Exception){
            Log.e("File_loading", e.printStackTrace().toString())
        }
        return inputData
    }

    private fun calculateEuclideanDistance(x1:Double, x2:Double, y1:Double, y2:Double, z1:Double, z2:Double):Double{
        return sqrt(((x2 - x1)*(x2 - x1)) + ((y2 - y1)*(y2 - y1)) + ((z2 - z1)*(z2 - z1)) )
    }

    @SuppressLint("LongLogTag")
    fun matchShapelet(windowData: ArrayList<Array<String>>, activityShapelet:List<ActivityShapelet>){
        var totalDistance = 0.00
        var i = 0
        for(row in activityShapelet){
            var distance = (calculateEuclideanDistance(
                row.x!!.toDouble(),
                windowData[i][3].toDouble(),
                row.y!!.toDouble(),
                windowData[i][4].toDouble(),
                row.z!!.toDouble(),
                windowData[i][5].toDouble()
            ))
            i++
            totalDistance += distance
        }
        allDistances.put(activityShapelet[0].activity!!,allDistances.
            getOrDefault(activityShapelet[0].activity!!,0.00)+ (totalDistance/activityShapelet.size))
        /*allDistances.forEach { key, value ->
            Log.d("activity: $key total distance:", value.toString())
        }*/
    }

    private fun getShapeletSizeMap():SparseIntArray{
        val x = SparseIntArray()
        allActivityShapelets.forEach { key, shapelet ->
            x.put(key,shapelet.size)
        }
        return x
    }

    private fun initAllWindows(){
        allActivityShapelets.forEach{activity,shapelet ->
            val window :Queue<Array<String>> = LinkedList()
            allWindows.put(activity,window)
        }
    }

    @SuppressLint("LongLogTag")
    private fun printActivity(){
        allDistances.forEach{activity,distance ->
            Log.d("activity: $activity total distance:", distance.toString())
            Log.d("The prediction took:", (time2-time1).toString() + "ms")
        }
        allDistances.clear()
    }

}


