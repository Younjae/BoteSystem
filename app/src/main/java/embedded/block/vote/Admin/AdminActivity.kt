package embedded.block.vote.Admin

import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import embedded.block.vote.UserSetting.EliminationActivity
import embedded.block.vote.UserSetting.LoginActivity
import embedded.block.vote.R
import embedded.block.vote.UserSetting.NetworkReceiver
import embedded.block.vote.UserSetting.UpdateActivity
import kotlinx.android.synthetic.main.admin_input.*
import kotlinx.android.synthetic.main.admin_result.*
import kotlinx.android.synthetic.main.admin_stop.*
import kotlinx.android.synthetic.main.content_admin.*
import kotlinx.android.synthetic.main.user_setting.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdminActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //각 메뉴버튼 화면을 인플레이션 하기위한 변수들을 lateinit으로 선언
    private var arr_voter = ArrayList<Int>()
    private lateinit var input_view: View
    private lateinit var stop_view: View
    private lateinit var user_setting_view: View
    private lateinit var result_view: View
    private lateinit var start_view: View
    private lateinit var image_view: View
    //메인화면 인플레이션 및 메뉴버튼마다 추가되는 화면 인플레이션
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        input_view = View.inflate(this, R.layout.admin_input, null)
        stop_view = View.inflate(this, R.layout.admin_stop, null)
        user_setting_view = View.inflate(this, R.layout.user_setting, null)
        result_view = View.inflate(this, R.layout.admin_result, null)
        start_view = View.inflate(this, R.layout.admin_start, null)
        image_view = View.inflate(this, R.layout.image_content, null)

        navView.setNavigationItemSelectedListener(this)
    }
    //뒤로 가기 버튼 눌렀을때 메뉴 상태에 따라 종료 및 메뉴 닫기
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    //선택된 메뉴에 따라 추가되는 화면 구분
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //투표 등록 메뉴 선택 시

        when (item.itemId) {
            R.id.nav_home -> {
                admin_content.removeAllViews()
                admin_content.addView(input_view)
                var arr_can = ArrayList<String?>()
                var adapter_canList = ArrayAdapter(this, android.R.layout.simple_list_item_1, arr_can)
                listViw_admin_input_can.adapter = adapter_canList

                //투표 참여자 선택 액티비티 호출
                button_admin_input_callSelect.setOnClickListener { v: View? ->
                    val intent = Intent(this, AdminInputActivity::class.java)
                    startActivityForResult(intent, 0)
                }

                //투표 후보자 입력 다이얼로그 호출
                button_admin_input_can.setOnClickListener { v: View? ->
                    val alertDialogBuilder = android.app.AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("후보자 입력")
                    alertDialogBuilder.setMessage("후보자 이름을 입력하세요")
                    val editText = EditText(this)
                    alertDialogBuilder.setView(editText)
                    alertDialogBuilder.setCancelable(false)
                    alertDialogBuilder.setPositiveButton("입력") { dialog, id ->
                        arr_can.add(editText.text.toString())
                        adapter_canList.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    alertDialogBuilder.setNegativeButton("취소") { dialog, id ->
                        dialog.dismiss()
                    }
                    alertDialogBuilder.show()
                }

                //서버에 투표 등록
                button_admin_inputGo.setOnClickListener { v: View? ->
                    if(arr_can.size == 0 || arr_voter.size == 0 ||
                            editText_admin_input_name.text == null || editText_admin_input_name.text.length == 0)
                        Toast.makeText(this, "입력되지 않은 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                    else {
                        //서버로 보낼 JSON 구성
                        var arr_userNum = ArrayList<Int>()
                        for (i in 0..(arr_voter.size-1)) {
                            arr_userNum.add(AdminInputAdapter.arr_getParticipation.getJSONObject(arr_voter[i]).getInt("userNum"))
                        }
                        var voteName = editText_admin_input_name.text.toString()
                        var json_toServer = JSONObject()

                        json_toServer.put("voteName", voteName)
                        json_toServer.put("voteVoter", Gson().toJson(arr_userNum))
                        json_toServer.put("voteCandidate", Gson().toJson(arr_can))
                        json_toServer.put("userNum", LoginActivity.userNum.toInt())

                        //서버에 보내는 부분
                        var queue: RequestQueue = Volley.newRequestQueue(this);
                        val request = object : JsonObjectRequest(
                            Request.Method.POST,
                            LoginActivity.ipAdress +"65001/bote/vote/votemaker",
                            json_toServer,
                            Response.Listener { response ->
                                run {

                                }
                            },
                            null
                        ) {
                            @Throws(AuthFailureError::class)
                            override fun getHeaders(): MutableMap<String, String>? {
                                val headers = HashMap<String, String>()
                                headers.put("Content-Type", "application/json")
                                return headers
                            }
                        }
                        queue.add(request)
                        admin_content.removeAllViews()
                        admin_content.addView(image_view)
                        Toast.makeText(this, "투표 등록 완료!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            //투표 시작 눌렀을때 투표 시작 액티비티 호출
            R.id.nav_start -> {
                admin_content.removeAllViews()
                admin_content.addView(image_view)
               val intent = Intent(this, AdminVoteStart::class.java)
               startActivity(intent)

            }
            //투표 중단 버튼 눌렀을때
            R.id.nav_slideshow -> {
                admin_content.removeAllViews()
                admin_content.addView(stop_view)
            //사용자 별 투표 목록을 서버로 부터 불러옴
                var queue: RequestQueue = Volley.newRequestQueue(this);
                val request = object : StringRequest(
                    Request.Method.GET,
                    LoginActivity.ipAdress +"65001/bote/vote/voteupdater/getlist/?userNum=" + LoginActivity.userNum,
                    Response.Listener { response ->
                        run {
                            var arr_getlist = JSONArray(response.toString())
                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            var nowTime = format.format(System.currentTimeMillis())
                            var i = 0
                            //현재 시간과 비교하여 중단 시간에 도달하지 않은 투표만 출력
                            while(i <= (arr_getlist.length()-1)) {
                                if (arr_getlist.getJSONObject(i).getString("quitTime") == "null" ||
                                    arr_getlist.getJSONObject(i).getString("quitTime") < nowTime) {
                                    arr_getlist.remove(i)
                                    i = 0
                                }
                                else
                                    i++
                            }
                            AdminStopAdapter.arr_getlistforStop = arr_getlist
                            var adapter = AdminStopAdapter(this)
                            listView_admin_stoplist.adapter = adapter
                        }
                    },
                    null
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
            //투표 결과 눌럿을때
            R.id.nav_result -> {
                admin_content.removeAllViews()
                admin_content.addView(result_view)
                var queue: RequestQueue = Volley.newRequestQueue(this);
                var arr_getName = ArrayList<String>()
            //사용자별 투표 목록 불러옴
                val request = object : StringRequest(
                    Request.Method.GET,
                    LoginActivity.ipAdress +"65001/bote/vote/voteresulter/admingetlist/?userNum=" + LoginActivity.userNum,
                    Response.Listener { response ->
                        run {
                            var arr_getlist = JSONArray(response.toString())
                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            var nowTime = format.format(System.currentTimeMillis())
                            var i = 0
                            //현재 시간과 비교하여 종료 시간이 지난 투표만 가져옴
                            while(i <= (arr_getlist.length()-1)) {
                                if (arr_getlist.getJSONObject(i).getString("quitTime") == "null" ||
                                    arr_getlist.getJSONObject(i).getString("quitTime") > nowTime) {
                                    arr_getlist.remove(i)
                                    i = 0
                                }
                                else
                                    i++
                            }

                            AdminResultAdapter.arr_getList = arr_getlist
                            for(i in 0..(AdminResultAdapter.arr_getList.length()-1))
                                arr_getName.add(AdminResultAdapter.arr_getList.getJSONObject(i).getString("voteName"))
                            //리스트 아이템 선택시 득표수를 가져오는 액티비티 호출
                            var madapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arr_getName)
                            admin_result.setOnItemClickListener { parent, view, position, id ->
                                var intent = Intent(this, AdminResultActivity::class.java)
                                intent.putExtra("position", position)
                                startActivity(intent)

                            }
                            admin_result.adapter = madapter

                        }
                    },
                    null
                ) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): MutableMap<String, String>? {
                        val headers = HashMap<String, String>()
                        headers.put("Content-Type", "application/json")
                        return headers
                    }
                }
                queue.add(request)
            //사용자 정보 메뉴 선택시
            }
            R.id.user_settings -> {
                admin_content.removeAllViews()
                admin_content.addView(user_setting_view)
                //정보 변경 버튼 클릭시 정보 변경 액티비티 호출
                button_user_setting_change.setOnClickListener { v: View? ->
                    var intent = Intent(this, UpdateActivity::class.java)
                    startActivityForResult(intent, 666)
                }
                //회원 탈퇴 버튼 클릭시 회원 탈퇴 액티비티 호출
                button_user_setting_elimination.setOnClickListener { v: View? ->
                    var intent = Intent(this, EliminationActivity::class.java)
                    startActivityForResult(intent, 666)
                }
                //로그아웃 클릭시 나가기
                button_user_setting_logout.setOnClickListener { v: View? ->
                    super.recreate()
                    finish()

                }


            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    //액티비티 호출후 전달받은 결과에 따라 분기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                when (resultCode) {
                    //0은 투표 등록에서 후보자 선택 액티비티에서 전달 받은 값을 다른 변수에 저장하고 리스트로 출력
                    0 -> {
                        var arr = data?.getIntegerArrayListExtra("userNum")

                        var temp = AdminInputAdapter.arr_getParticipation
                        var temp3 = ArrayList<String>()
                        arr_voter.removeAll(arr_voter)
                        for (i in 0..(arr!!.size - 1)) {
                            temp3.add(temp.getJSONObject(arr[i]).getString("userName"))
                            arr_voter.add(arr[i])
                        }
                        var adpater = ArrayAdapter(this, android.R.layout.simple_list_item_1, temp3)
                        listView_admin_input_selected.adapter = adpater
                        adpater.notifyDataSetChanged()
                    }
                    //666은 투표 등록에서 후보자 선택 액티비티에서 취소 버튼이 선택될 경우
                    666 -> {

                    }
                }
            }
            //정보변경, 회원탈퇴 액티비티에서 자동 로그아웃 시키기 위함
            666 -> {
                finish()
                super.recreate()
            }
        }
    }
}
