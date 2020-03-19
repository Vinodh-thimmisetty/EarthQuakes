package com.example.android.earthquakes

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.earthquakes.EarthQuakeRecycleViewAdapter.EarthQuakeHolder
import com.example.android.earthquakes.databinding.ActivityMainBinding
import com.example.android.earthquakes.databinding.EarthquakeListViewItemBinding
import com.example.android.earthquakes.databinding.EarthquakeRecycleViewItemBinding
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

const val LOG_TAG: String = "EQuakeLog"

data class EarthQuake(
    var magnitude: Double,
    var locationOffset: String, var primaryLocation: String,
    var date: LocalDate, var time: LocalTime
)

class EarthQuakeListViewAdapter(context: Context, data: List<EarthQuake>) :
    ArrayAdapter<EarthQuake>(context, 0, data) {

    private lateinit var adapterBinding: EarthquakeListViewItemBinding

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        adapterBinding = EarthquakeListViewItemBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )

        val eQuake: EarthQuake = this.getItem(position)
            ?: throw IllegalArgumentException("Invalid Data")
//        if (position % 3 == 0) {
//            adapterBinding.cardViewId.setCardBackgroundColor(
//                ContextCompat.getColor(
//                    context,
//                    android.R.color.holo_green_light
//                )
//            )
//        }
        adapterBinding.magnitude.text = DecimalFormat("#.##").format(eQuake.magnitude)
        val magnitudeCircle: GradientDrawable =
            adapterBinding.magnitude.background as GradientDrawable
        magnitudeCircle.setColor(
            ContextCompat.getColor(
                context,
                getMagnitudeColor(eQuake.magnitude)
            )
        )
        adapterBinding.locationOffset.text = eQuake.locationOffset
        adapterBinding.primaryLocation.text = eQuake.primaryLocation
        adapterBinding.date.text =
            DateTimeFormatter.ofPattern("MMM dd yyyy").format(eQuake.date)
        adapterBinding.time.text =
//            eQuake.time.truncatedTo(ChronoUnit.SECONDS).toString()
//            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US).format(eQuake.time)
            DateTimeFormatter.ofPattern("hh:mm a").format(eQuake.time)
        return adapterBinding.root
    }


}

class EarthQuakeRecycleViewAdapter(
    private val context: Context,
    private val earthQuakeList: List<EarthQuake>
) :
    RecyclerView.Adapter<EarthQuakeHolder>() {

    private lateinit var adapterBinding: EarthquakeRecycleViewItemBinding

    class EarthQuakeHolder(
        private val adapterBinding: EarthquakeRecycleViewItemBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(adapterBinding.root) {
        fun bindData(eQuake: EarthQuake) {
            adapterBinding.magnitude.text = DecimalFormat("#.##").format(eQuake.magnitude)
            val magnitudeCircle: GradientDrawable =
                adapterBinding.magnitude.background as GradientDrawable
            magnitudeCircle.setColor(
                ContextCompat.getColor(
                    context,
                    getMagnitudeColor(eQuake.magnitude)
                )
            )
            adapterBinding.locationOffset.text = eQuake.locationOffset
            adapterBinding.primaryLocation.text = eQuake.primaryLocation
            adapterBinding.date.text =
                DateTimeFormatter.ofPattern("MMM dd yyyy").format(eQuake.date)
            adapterBinding.time.text =
                DateTimeFormatter.ofPattern("hh:mm a").format(eQuake.time)
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EarthQuakeHolder {
        adapterBinding =
            EarthquakeRecycleViewItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return EarthQuakeHolder(adapterBinding, context)
    }

    override fun getItemCount(): Int = earthQuakeList.size

    override fun onBindViewHolder(
        holder: EarthQuakeHolder,
        position: Int
    ) {
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Yoyo", Toast.LENGTH_SHORT).show();
        }
        holder.bindData(earthQuakeList[position])
    }


}

class EarthQuakeTask : AsyncTask<String, Int, List<EarthQuake>>() {

    override fun onPreExecute() {
        // show the progress bar Visibility
    }

    override fun doInBackground(vararg params: String?): List<EarthQuake> = jsonFromWeb()

    override fun onPostExecute(result: List<EarthQuake>?) {
    }
}

class EarthQuakeLiveData : LiveData<List<EarthQuake>> {
    var context: Context

    constructor(context: Context) {
        this.context = context
        EarthQuakeTask().execute()
    }
}

class EarthQuakeViewModel(application: Application) : AndroidViewModel(application) {
    private val earthQuakeLiveData: EarthQuakeLiveData = EarthQuakeLiveData(application)
    fun getData(): LiveData<List<EarthQuake>> = earthQuakeLiveData
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var listViewBtn: Button
    private lateinit var recycleViewBtn: Button
    private lateinit var listViewAdapter: ListView
    private lateinit var recycleListView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showToast("onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recycleViewBtn = binding.recyclerViewAdapterBtn
        val earthQuakeViewModel = ViewModelProvider(this).get(EarthQuakeViewModel::class.java)
        recycleViewBtn.setOnClickListener {
            listViewAdapter = binding.listViewList
            listViewAdapter.visibility = View.GONE
            recycleListView = binding.recyclerViewList
            recycleListView.visibility = View.VISIBLE
            recycleListView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        this@MainActivity,
                        DividerItemDecoration.VERTICAL
                    )
                )
                adapter = EarthQuakeRecycleViewAdapter(
                    this@MainActivity,
//                    EarthQuakeTask().execute().get()
                    earthQuakeViewModel.getData().value!!
                )
            }

        }

        listViewBtn = binding.listViewAdapterBtn
        listViewBtn.setOnClickListener {
            recycleListView = binding.recyclerViewList
            recycleListView.visibility = View.GONE
            listViewAdapter = binding.listViewList
            listViewAdapter.visibility = View.VISIBLE
            val earthQuakeAdapter =
                EarthQuakeListViewAdapter(this, EarthQuakeTask().execute().get())
            listViewAdapter.adapter = earthQuakeAdapter
        }


    }

    override fun onStart() {
        super.onStart()
        showToast("onStart")
    }

    override fun onResume() {
        super.onResume()
        showToast("onResume")
    }

    //    private fun mockData() {
////        val capitalCities = listOf(
////            "San Francisco",
////            "London",
////            "Tokyo",
////            "Mexico City",
////            "Moscow",
////            "Rio de Janeiro",
////            "Paris"
////        )
//        val capitalListView: ListView = binding.capitalList
////        val arrayAdapter: ArrayAdapter<String> =
////            ArrayAdapter(this, android.R.layout.simple_list_item_1, capitalCities)
////        capitalListView.adapter = arrayAdapter
////
////        val earthQuakes = mutableListOf<EarthQuake>()
////
////        var apiRequest: Uri =
////            Uri.parse("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=10")
////        for ((index, cityName) in capitalCities.withIndex()) {
////            earthQuakes.add(
////                EarthQuake(
////                    7.2,
////                    capitalCities[index],
////                    LocalDate.of(2016, Month.FEBRUARY, 2)
////                )
////            )
////        }
//
//        val earthQuakeAdapter =
//            EarthQuakeAdapter(this, EarthQuakeTask().execute().get())
//        capitalListView.adapter = earthQuakeAdapter
//    }
    override fun onDestroy() {
        super.onDestroy()
        showToast("onDestroy")
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        Log.v(LOG_TAG, msg)
    }


}


