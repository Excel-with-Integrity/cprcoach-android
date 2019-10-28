package com.example.bluetooth
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.practice.*
import pl.droidsonroids.gif.GifDrawable

class PracticeFragment : Fragment() {

    lateinit var  mview: View

    companion object {

        fun newInstance(): PracticeFragment {
            return newInstance()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.practice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchButton.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                (activity as SettingActivity).startIso()
            } else {
                (activity as SettingActivity).stopIso()
            }
        }
    }

    fun unlockButton() {
        switchButton.setText("Read")
        switchButton.isClickable = true
        switchButton.setBackgroundResource(R.drawable.button_border)
    }

    fun lockButton() {
        switchButton.isClickable = false
        switchButton.setBackgroundResource(R.drawable.button_border_grey)
        switchButton.setText("Connect to Activ5 to Start")
    }

    fun stopGif(gifDrawable: GifDrawable) {
    }

    fun startGif(gifDrawable: GifDrawable) {
        gifDrawable.start()
    }

}