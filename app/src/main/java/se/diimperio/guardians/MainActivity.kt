package se.diimperio.guardians

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var triggerBttn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        triggerBttn = findViewById(R.id.alarm_trigger)

        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                var timePressed:Long = 0

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d("Trigger down", "The trigger has been pressed")

                    }
                    MotionEvent.ACTION_UP -> triggerRelease()
                }


                return v?.onTouchEvent(event) ?: true
            }
        })
    }
    private fun triggerSupress(){

    }
    private fun triggerRelease(){
        Log.d("main","trigger release")
    }
}
