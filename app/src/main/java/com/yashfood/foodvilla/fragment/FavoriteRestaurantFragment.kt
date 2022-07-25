package com.yashfood.foodvilla.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import com.yashfood.foodvilla.R
import com.yashfood.foodvilla.adapter.DashboardFragmentAdapter
import com.yashfood.foodvilla.database.RestaurantDatabase
import com.yashfood.foodvilla.database.RestaurantEntity
import com.yashfood.foodvilla.model.Restaurant
import com.yashfood.foodvilla.utils.ConnectionManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_favorite_restaurant.*
import org.json.JSONException

class FavouriteRestaurantFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var favoriteAdapter: DashboardFragmentAdapter
    lateinit var favoriteProgressLayout: RelativeLayout
    var restaurantInfoList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favorite_restaurant, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerViewFavoriteRestaurant)
        favoriteProgressLayout = view.findViewById(R.id.favoriteProgressLayout)

        return view
    }

    private fun fetchData() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            favoriteProgressLayout.visibility = View.VISIBLE
            try {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        println("Response12 is  $it")
                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")

                        if (success) {
                            img_fav_bg_illus.visibility = View.INVISIBLE
                            tv_nothing_added.visibility = View.INVISIBLE
                            add_fav_frag_img.visibility = View.INVISIBLE

                            restaurantInfoList.clear()
                            val data = response.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantEntity = RestaurantEntity(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name")
                                )

                                if (DBAsyncTask(contextParam, restaurantEntity, 1).execute()
                                        .get()
                                ) {
                                    val restaurantObject = Restaurant(
                                        restaurantJsonObject.getString("id"),
                                        restaurantJsonObject.getString("name"),
                                        restaurantJsonObject.getString("rating"),
                                        restaurantJsonObject.getString("cost_for_one"),
                                        restaurantJsonObject.getString("image_url")
                                    )

                                    restaurantInfoList.add(restaurantObject)
                                    favoriteAdapter = DashboardFragmentAdapter(
                                        activity as Context,
                                        restaurantInfoList
                                    )
                                    recyclerView.adapter = favoriteAdapter
                                    recyclerView.layoutManager = layoutManager
                                }
                            }
                            if (restaurantInfoList.size == 0) {
                                img_fav_bg_illus.visibility = View.VISIBLE
                                tv_nothing_added.visibility = View.VISIBLE
                                add_fav_frag_img.visibility = View.VISIBLE

                            }
                        }
                        favoriteProgressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        println("Error12 is $it")

                        Toast.makeText(
                            activity as Context,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                        favoriteProgressLayout.visibility = View.INVISIBLE
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "be283b8c66b914"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    activity as Context,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            /*
            * Mode 1->check if restaurant is in favourites
            * Mode 2->Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant*/
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null
                }
                else -> return false
            }
        }
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }

        super.onResume()
    }
}
