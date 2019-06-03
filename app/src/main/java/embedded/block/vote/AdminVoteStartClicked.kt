package embedded.block.vote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.jvm.Throws;

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap

class AdminVoteStartClicked : AppCompatActivity(), View.OnClickListener {
private var quitTime: String? = null
private var voteNum: Int = 0
private var selectTime = 1
        override fun onClick(v: View) {

        }

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        voteNum = intent.getIntExtra("voteNum", 9999)

        setContentView(R.layout.admin_start_clicked)
        val spinner = findViewById(R.id.spinnerStart) as Spinner
        val startBtn = findViewById(R.id.startBtn) as Button

        val requestQueue = Volley.newRequestQueue(this)
        AActivity = this

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(
        parent: AdapterView<*>, view: View,
        position: Int, id: Long
        ) {
        print(parent.getItemAtPosition(position))
        var str = parent.getItemAtPosition(position).toString()
        print(str)
        str = str.substring(0, 1)
        print(str)
        selectTime = Integer.parseInt(str)

        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }

        }
        startBtn.setOnClickListener {
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE, selectTime)
        val time = cal.time
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        quitTime = sdf.format(time)
        val url =
        "http://203.249.127.32:65001/bote/vote/voteupdater/votereaper/?voteNum=$voteNum&quitTime=$quitTime"
        val stringRequest = object : StringRequest(Request.Method.GET,
        url, Response.Listener { }, Response.ErrorListener { error -> error.printStackTrace() }) {
@Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["quitTime"] = quitTime!!
        params["voteNum"] = Integer.toString(voteNum)
        return params
        }
        }
        try {
        requestQueue.add(stringRequest)
        val intent = Intent(applicationContext, StartPopupActivity::class.java)
        startActivity(intent)


        } catch (e: Exception) {

        }
        }
        }

        companion object {
        lateinit var AActivity: Activity
        }

        }