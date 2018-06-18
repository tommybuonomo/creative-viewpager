package com.tbuonomo.creativeviewpager.transformer

import android.support.v4.view.ViewPager
import android.view.View

class CreativeContentPageTransformer(private val contentWidthPadding: Float) : ViewPager.PageTransformer {

  override fun transformPage(view: View, position: Float) {
    view.translationX = contentWidthPadding
  }
}