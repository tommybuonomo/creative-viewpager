package com.tbuonomo.creativeviewpager.adapter

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.MarginLayoutParams
import com.tbuonomo.creativeviewpager.R

class CreativeContentAdapter(private val parent: View,
        private val contentMargin: Float,
        private val contentWidthPadding: Float) :
        PagerAdapter() {

  lateinit var creativePagerAdapter: CreativePagerAdapter

  override fun instantiateItem(container: ViewGroup, position: Int): View {
    val inflater = LayoutInflater.from(container.context)
    val wrapper = inflater.inflate(R.layout.item_creative_content, container, false) as ViewGroup

    val view = creativePagerAdapter.instantiateHeaderItem(
            inflater,
            wrapper, position)

    val layoutParams = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT)
    if (position == count - 1) {
      layoutParams.marginEnd = (contentWidthPadding + contentWidthPadding / 2).toInt()
    }
    wrapper.addView(view, layoutParams)
    container.addView(wrapper)
    return wrapper
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view == `object`
  }

  override fun getCount() = creativePagerAdapter.getCount()

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as View)
  }

  override fun getPageWidth(position: Int): Float {
    if (position == count - 1) {
      return 1.0f
    }
    return 1.0f - (contentWidthPadding * 2) / parent.width
  }
}