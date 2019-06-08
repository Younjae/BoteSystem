package embedded.block.vote.UserSetting

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import embedded.block.vote.R
import org.json.JSONObject
import java.util.*


@Suppress("DEPRECATION")
public class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        progressON(LoginActivity.loginAct, "네트워크 변경을 감지하였습니다.")
        flag = false
        val handler = Handler()
        handler.postDelayed(Runnable{
        retryCount = 0
        progressON(LoginActivity.loginAct, "서버를 탐색중입니다.")
        checkServer()
        }, 8000)
    }
    //로딩 화면을 보여주는 함수
    fun progressON(activity: Activity?, message: String) {

        if (activity == null || activity.isFinishing) {
            return
        }


        if (progressDialog != null && progressDialog!!.isShowing()) {
            progressSET(message)
        } else {

            progressDialog = AppCompatDialog(activity)
            progressDialog?.setCancelable(false)
            progressDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog?.setContentView(R.layout.progress_loading)
            progressDialog?.show()

        }

        val img_loading_frame = progressDialog?.findViewById<ImageView>(
            R.id.iv_frame_loading
        )
        var gifImg = GlideDrawableImageViewTarget(img_loading_frame)
        Glide.with(activity).load(R.drawable.loading).into(gifImg)

        val tv_progress_message = progressDialog?.findViewById<TextView>(
            R.id.tv_progress_message
        )
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message?.text = message
        }


    }

    //로딩화면을 종료하는 함수
    fun progressOFF() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }
    //로딩화면을 셋팅하는 함수
    fun progressSET(message: String) {

        if (progressDialog == null || !progressDialog!!.isShowing) {
            return
        }
        progressDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tv_progress_message = progressDialog?.findViewById<TextView>(R.id.tv_progress_message)
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message!!.text = message
        }

    }
    fun checkServer() {
        Log.d("etest", LoginActivity.ipAdress + retryCount)
        var json = JSONObject()
        json.put("myid", "null!")
        //Volley를 사용하며 REST API를 활요한 서버 통신
        var queue: RequestQueue = Volley.newRequestQueue(LoginActivity.loginAct);
        val request = object : JsonObjectRequest(Request.Method.GET, LoginActivity.ipAdress + "65001/bote/login/?myid=admin&mypass=admin", json,
            Response.Listener { response ->
                run {
                    progressOFF()
                    retryCount = 1000
                    flag = true
                }
            }, Response.ErrorListener {
                //서버가 중단될 경우 3번까지 재시도하면서 사설IP와 공인IP를 전환해가며 접속 시도
                if(retryCount <= 2) {
                    if(LoginActivity.ipAdress == "http://203.249.127.32:") {
                        LoginActivity.ipAdress = "http://192.9.44.53:"
                        retryCount++
                    }
                    else {
                        LoginActivity.ipAdress = "http://203.249.127.32:"
                        retryCount++
                    }
                    checkServer()
                }
                //사설IP와 공인IP 모두 접속에 실패한 경우
                else {
                    if(flag == false) {
                        progressOFF()
                        retryCount = 0
                        Toast.makeText(LoginActivity.loginAct, "서버가 작동중이 아닙니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String>? {
                val headers = HashMap<String, String>()
                headers.put("Content-Type", "application/json")
                return headers
            }
        }
        queue.add(request)
    }
    companion object {
        var retryCount = 0
        var progressDialog : AppCompatDialog? = null
        var flag = false
    }
}