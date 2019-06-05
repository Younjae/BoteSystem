package embedded.block.vote.Admin

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import embedded.block.vote.R

class StartPopupActivity : Activity(), View.OnClickListener {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.start_popup)

        val finishBtn = findViewById<Button>(R.id.finishBtn)

        finishBtn.setOnClickListener {
        val temp = AdminVoteStartClicked.AActivity as AdminVoteStartClicked
        val temp2 = AdminVoteStart.th as AdminVoteStart
        finish()
        temp.finish()
        temp2.recreate()
        }

        }


        override fun onTouchEvent(event: MotionEvent): Boolean {
        return event.action != MotionEvent.ACTION_OUTSIDE
        }

        override fun onBackPressed() {
        return
        }

        override fun onClick(v: View) {

        }
        }
