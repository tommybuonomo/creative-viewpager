package com.tbuonomo.creativeviewpagersample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity2 : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    creativeViewPagerView.setCreativeViewPagerAdapter(NatureCreativePagerAdapter(context!!))
  }
}