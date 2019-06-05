package embedded.block.vote.UserSetting

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialog
import android.text.TextUtils
import android.view.View
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
import embedded.block.vote.Admin.AdminActivity
import embedded.block.vote.R
import embedded.block.vote.Voter.VoteListActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*


class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //기본 ipAdress 주소를 사설 IP주소로 할당
        ipAdress = "http://192.9.44.53:"

        //회원가입 액티비티 호출 버튼
        regBtn.setOnClickListener {
            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        //로딩 화면 함수 호출, 로그인 함수 호출
        loginBtn.setOnClickListener {
            progressON(this, "로그인중")
            login(myID.text.toString(), myPass.text.toString())
        }
        //아이디 찾기 액티비티 호출 버튼
        findID.setOnClickListener {
            var intent = Intent(this, FindidActivity::class.java)
            startActivity(intent)
        }
        //비밀번호 찾기 액티비티 호출 버튼
        findPass.setOnClickListener {
            var intent = Intent(this, FindpassActivity::class.java)
            startActivity(intent)
        }
    }
    //로그인 함수
    fun login(myid: String, mypass: String) {
        var json = JSONObject()
        json.put("myid", "null!")
        //Volley를 사용하며 REST API를 활요한 서버 통신
        var queue: RequestQueue = Volley.newRequestQueue(this);
        val request = object : JsonObjectRequest(Request.Method.GET, ipAdress + "65001/bote/login/?myid=" + myid + "&mypass=" + mypass, json,
            Response.Listener { response ->
                run {
                    //로그인 성공시 회원 데이터를 받아와서 Companion Object에 선언된 변수들에 저장
                    progressOFF()
                    retryCount = 0
                    userClass.clear()
                    if(response.getString("userauthor") != "undefind") {
                        var tmp = response.getString("userclassnum")
                        var tempList = tmp.split(",")


                        for (i in 0 until tempList.size)
                            userClass.add(tempList.get(i))

                        userId = myid
                        userPass = mypass
                        userName = response.getString("username")
                        userNum = response.getString("usernum")
                        userPhone = response.getString("userphone")
                        userAuthor = response.getString("userauthor")
                        //회원 권한이 투표 등록자일 경우 해당 액티비티 호출
                        if(userAuthor == "1") {
                            var intent = Intent(this, AdminActivity::class.java)
                            startActivity(intent)
                        }
                        //회원 권한이 유권자일 경우 해당 액티비티 호출
                        else if(userAuthor == "2") {
                            var intent = Intent(this, VoteListActivity::class.java)
                            startActivity(intent)
                        }
                        //회원 권한이 탈퇴한 유저일 경우 Toast 메시지 띄우기
                        else if(userAuthor == "3") {
                            Toast.makeText(this, "회원 탈퇴한 정보입니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //로그인  실패시 Toast 메시지 띄우기
                    else {
                        Toast.makeText(this, "아이디 혹은 비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }, Response.ErrorListener {
                //서버가 중단될 경우 3번까지 재시도하면서 사설IP와 공인IP를 전환해가며 접속 시도
                retryCount++
                if(retryCount <= 2) {
                    if(ipAdress == "http://203.249.127.32:")
                        ipAdress = "http://192.9.44.53:"
                    else
                        ipAdress = "http://203.249.127.32:"
                    login(myid, mypass)
                }
                //사설IP와 공인IP 모두 접속에 실패한 경우
                else {
                    progressOFF()
                    Toast.makeText(this, "서버가 작동중이 아닙니다.", Toast.LENGTH_SHORT).show()
                    retryCount = 0
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
    //로딩을 화면 구성하여 호출하는 함수
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
        Glide.with(this).load(R.drawable.loading).into(gifImg)

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
    //로딩 화면에 메시지를 세팅하는 함수
    fun progressSET(message: String) {

        if (progressDialog == null || !progressDialog!!.isShowing) {
            return
        }

        val tv_progress_message = progressDialog?.findViewById<View>(
            R.id.tv_progress_message
        ) as TextView?
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message!!.text = message
        }

    }
    //로그인시 저장되는 사용자 정보 및 로딩화면, 재시도 횟수를 저장하는 Companion Object
    companion object {
        var userId = ""
        var userPass = ""
        var userName = ""
        var userNum = ""
        var userClass = ArrayList<String>()
        var userPhone = ""
        var userAuthor = ""
        var ipAdress = ""
        var retryCount = 0
        var progressDialog : AppCompatDialog? = null
    }
}