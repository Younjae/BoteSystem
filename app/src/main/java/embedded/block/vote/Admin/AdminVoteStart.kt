package embedded.block.vote.Admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import embedded.block.vote.UserSetting.LoginActivity
import embedded.block.vote.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList

class AdminVoteStart : AppCompatActivity(), View.OnClickListener {

        lateinit var toolbar: Toolbar
private var queue: RequestQueue? = null
private var jsonObject = JSONObject()
private var jsonArray = JSONArray()
private val data = ArrayList<AdminVoteItem>()
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_start)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        val listView = findViewById<ListView>(R.id.admin_start)
        th = this

        val url = LoginActivity.ipAdress+ "65001/bote/vote/voteupdater/getlist/?userNum=" + LoginActivity.userNum


        queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
        Response.Listener { response ->
        var vote: AdminVoteItem
        try {
        jsonArray = JSONArray(response)
        for (i in 0 until jsonArray.length()) {
        jsonObject = jsonArray.getJSONObject(i)
        vote = AdminVoteItem(
                jsonObject.getInt("voteNum"),
                jsonObject.getString("voteName"),
                jsonObject.getString("quitTime")
        )
        if (jsonObject.getString("quitTime") === "null")
        data.add(vote)
        }

        val adapter = AdminVoteListAdapter(
                applicationContext,
                R.layout.admin_start_item,
                data
        )
        listView.adapter = adapter
        } catch (e: JSONException) {
        e.printStackTrace()
        }
        }, Response.ErrorListener { })

        stringRequest.tag = "MAIN"
        queue!!.add(stringRequest)




        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val intent = Intent(applicationContext, AdminVoteStartClicked::class.java)

        intent.putExtra("voteNum", data[position].voteNum)
        startActivity(intent)
        }
        toolbar.setNavigationOnClickListener { finish() }
        }


        override fun onClick(v: View) {

        }

        override fun onBackPressed() {
        super.onBackPressed()
        finish()
        }

        companion object {
        lateinit var th: Activity
                }
        }
