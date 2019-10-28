package com.example.bluetooth

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AboutFragment : Fragment() {

    companion object {

        fun newInstance(): AboutFragment {
            return newInstance()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.about, container, false)
    }

}