package Alarm

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Interpolator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import se.diimperio.guardians.R


class AlarmingFragment : Fragment() {

    lateinit var animator:ObjectAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarming, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animator = ObjectAnimator.ofInt(
            view, "backgroundColor", Color.BLUE, Color.RED
        )

        blinkEffect()
        Toast.makeText(context,"blinking ?",Toast.LENGTH_LONG).show()


    }
    fun blinkEffect(){

        animator.duration = 300
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = Animation.INFINITE
        animator.start()
    }
}
