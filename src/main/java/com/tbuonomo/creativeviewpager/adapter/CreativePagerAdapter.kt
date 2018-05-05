package com.tbuonomo.creativeviewpager.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

interface CreativePagerAdapter {
  /**
   * Method to override in child adapter to instantiate and bind header item at position [position].
   */
  fun instantiateHeaderItem(inflater: LayoutInflater, container: ViewGroup,
          position: Int): View

  /**
   * Method to override in child adapter to instantiate and bind content item at position [position].
   */
  fun instantiateContentItem(inflater: LayoutInflater,
          container: ViewGroup, position: Int): View

  /**
   * Count of items in adapter.
   */
  fun getCount(): Int

  /**
   * Override this methods to render the background gradient color effect during sliding.
   *
   * You need to implement the [requestBitmapAtPosition] method in the same way.
   * @return true if you want to display color slide effect.
   */
  fun isUpdatingBackgroundColor(): Boolean {
    return false
  }

  /**
   * Methods used for updating the background color of the entire view depending on the bitmaps
   * displayed in contents layouts.
   *
   * This methods is called asynchronously in the library, so you can load your bitmaps with Glide
   * or Picasso using the synchronous way.
   *
   * @param position The position of the requested bitmap.
   */
  fun requestBitmapAtPosition(position: Int): Bitmap? {
    return null
  }
}