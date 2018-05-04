package com.tbuonomo.creativeviewpager

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.tbuonomo.creativeviewpager.adapter.CreativeViewAdapter
import com.tbuonomo.creativeviewpager.adapter.CreativeViewContentAdapter
import com.tbuonomo.creativeviewpager.adapter.CreativeViewImageAdapter
import com.tbuonomo.creativeviewpager.transformer.CreativeContentPageTransformer
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.creativeImageRecycler
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.creativeRoot
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.creativeViewPagerCenter
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.imageVerticalGuideline
import kotlinx.android.synthetic.main.layout_creative_view_pager_edit_mode.view.editModeContentLayout
import kotlinx.android.synthetic.main.layout_creative_view_pager_edit_mode.view.editModeImagesLayout

class CreativeViewPagerView : FrameLayout {
  companion object {
    const val SCALE_MIN = 0.35f
    const val SCALE_MAX = 1.0f
  }

  private var imagesMargin = resources.getDimension(R.dimen.dimens_8dp)
  private var contentMargin = resources.getDimension(R.dimen.dimens_4dp)
  private var contentHorizontalPadding = resources.getDimension(R.dimen.dimens_32dp)
  private var imagesSize = resources.getDimension(R.dimen.dimens_92dp)

  private lateinit var creativeImageAdapter: CreativeViewImageAdapter
  private lateinit var creativeContentAdapter: CreativeViewContentAdapter

  private var paletteCacheManager: PaletteCacheManager = PaletteCacheManager()
  private val argbEvaluator: ArgbEvaluator = ArgbEvaluator()

  private var creativeViewAdapter: CreativeViewAdapter? = null

  constructor(context: Context?) : super(context) {
    init(null)
  }

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    init(attrs)
  }

  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
          defStyleAttr) {
    init(attrs)
  }

  private fun init(attrs: AttributeSet?) {
    readAttributes(attrs)

    // Render edit mode with fake layouts, to see layouts in preview
    if (isInEditMode) {
      setUpEditMode()
      return
    }

    View.inflate(context, R.layout.layout_creative_view_pager, this)
    setUpImages()
    setUpContent()
  }

  private fun readAttributes(attrs: AttributeSet?) {
    if (attrs != null) {
      val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CreativeViewPagerView)

      imagesSize = ta.getDimension(R.styleable.CreativeViewPagerView_imagesSize, imagesSize)
      imagesMargin = ta.getDimension(R.styleable.CreativeViewPagerView_imagesMargin, imagesMargin)
      contentMargin = ta.getDimension(R.styleable.CreativeViewPagerView_contentMargin,
              contentMargin)
      contentHorizontalPadding = ta.getDimension(
              R.styleable.CreativeViewPagerView_contentHorizontalPadding,
              contentHorizontalPadding)
      ta.recycle()
    }
  }

  private fun setUpEditMode() {
    View.inflate(context, R.layout.layout_creative_view_pager_edit_mode, this)

    val contentView = LayoutInflater.from(context).inflate(
            R.layout.item_creative_content_placeholder, editModeContentLayout, false)
    editModeContentLayout.setPadding(contentHorizontalPadding.toInt(), 0,
            contentHorizontalPadding.toInt(), 0)
    editModeContentLayout.addView(contentView, LayoutParams(MATCH_PARENT, MATCH_PARENT))

    for (i in 0..10) {
      val imageView = LayoutInflater.from(context).inflate(R.layout.item_creative_image_placeholder,
              editModeImagesLayout,
              false)
      val marginLayoutParams = MarginLayoutParams(imagesSize.toInt(), imagesSize.toInt())
      val layoutParams = imageVerticalGuideline.layoutParams as ConstraintLayout.LayoutParams
      val parentMargin = layoutParams.guidePercent * context.resources.displayMetrics.widthPixels
      if (i == 0) {
        marginLayoutParams.marginStart = (parentMargin - imagesSize / 2).toInt()
      } else {
        marginLayoutParams.marginStart = imagesMargin.toInt()
        marginLayoutParams.marginEnd = imagesMargin.toInt()
        marginLayoutParams.width = (imagesSize * SCALE_MIN).toInt()
        marginLayoutParams.height = (imagesSize * SCALE_MIN).toInt()
      }

      editModeImagesLayout.addView(imageView, marginLayoutParams)
    }
  }

  private fun setUpContent() {
    // Contents
    creativeContentAdapter = CreativeViewContentAdapter(this, contentMargin,
            contentHorizontalPadding)
    creativeViewPagerCenter.setPageTransformer(false,
            CreativeContentPageTransformer(contentHorizontalPadding))
    creativeViewPagerCenter.pageMargin = contentMargin.toInt()
    creativeViewPagerCenter.addOnPageChangeListener(OnContentPageChangeListener())
  }

  private fun setUpImages() {
    // Create adapter for images
    creativeImageAdapter = CreativeViewImageAdapter(this, imageVerticalGuideline, imagesSize,
            imagesMargin, {
      creativeViewPagerCenter.setCurrentItem(it, true)
    })
    creativeImageRecycler.layoutParams.height = (imagesSize + resources.getDimension(
            R.dimen.dimens_16dp) * 2).toInt()
    creativeImageRecycler.recycledViewPool.setMaxRecycledViews(0, 0)

    // Delegate onTouch to the content view pager
    creativeImageRecycler.setOnTouchListener { _, event ->
      creativeViewPagerCenter.onTouchEvent(event)
      true
    }
  }

  fun setCreativeViewPagerAdapter(creativeViewAdapter: CreativeViewAdapter) {
    post({
      this.creativeViewAdapter = creativeViewAdapter
      // Setup adapter for palette manager
      paletteCacheManager.setCreativeViewAdapter(creativeViewAdapter)
      paletteCacheManager.cachePalettesAroundPositionAsync(0, {
        refreshBackgroundColor(0, 0f)
      })

      // Setup image adapter
      creativeImageAdapter.creativeViewAdapter = creativeViewAdapter
      creativeImageRecycler.layoutManager = LinearLayoutManager(context,
              LinearLayoutManager.HORIZONTAL, false)
      creativeImageRecycler.adapter = creativeImageAdapter

      // Setup content adapter
      creativeContentAdapter.creativeViewAdapter = creativeViewAdapter
      creativeViewPagerCenter.adapter = creativeContentAdapter

      creativeImageRecycler.post({ refreshImagesPosition(0f, 0) })
    })
  }

  private fun refreshImagesSize() {
    for (i in 0 until creativeImageRecycler.childCount) {
      val itemView = creativeImageRecycler.getChildAt(i)

      val centerX = itemView.x + itemView.width.toFloat() / 2
      val diffX = Math.abs(imageVerticalGuideline.x - centerX)
      val maxScaleDistance = imagesSize - imagesMargin * 2
      val scale = if (diffX < maxScaleDistance) SCALE_MAX - diffX / maxScaleDistance * (SCALE_MAX - SCALE_MIN) else SCALE_MIN
      itemView.size = (imagesSize * scale).toInt()
    }
    creativeImageRecycler.requestLayout()
  }

  private fun refreshImagesPosition(positionOffset: Float, position: Int) {
    val linearLayoutManager = creativeImageRecycler.layoutManager as LinearLayoutManager

    val distanceBetweenImages = (imagesSize * SCALE_MIN + imagesMargin * 2).toInt()
    val imageOffsetPixels = positionOffset * distanceBetweenImages

    val scrollOffsetPixels = (-(position * distanceBetweenImages + imageOffsetPixels)).toInt()

    linearLayoutManager.scrollToPositionWithOffset(0,
            scrollOffsetPixels)
  }

  inner class OnContentPageChangeListener : OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      refreshImagesPosition(positionOffset, position)
      refreshImagesSize()
      refreshBackgroundColor(position, positionOffset)
    }

    override fun onPageSelected(position: Int) {
      paletteCacheManager.cachePalettesAroundPositionAsync(position)
    }
  }

  private fun refreshBackgroundColor(position: Int, positionOffset: Float) {
    if (creativeViewAdapter?.isUpdatingBackgroundColor() == true) {

      // Retrieve palettes from cache
      var startPalette = paletteCacheManager.getPaletteForPosition(position)
      var endPalette = paletteCacheManager.getPaletteForPosition(position + 1)

      var startTopColor = Color.TRANSPARENT
      var startBottomColor = Color.TRANSPARENT
      var endTopColor = Color.TRANSPARENT
      var endBottomColor = Color.TRANSPARENT

      if (startPalette != null) {
        startTopColor = startPalette.getVibrantColor(startPalette.getLightVibrantColor(
                startPalette.getMutedColor(Color.TRANSPARENT)))

        startBottomColor = startPalette.getDominantColor(
                startPalette.getDarkVibrantColor(startPalette.getDarkMutedColor(
                        Color.TRANSPARENT)))
      }

      if (endPalette != null) {
        endTopColor = endPalette.getVibrantColor(endPalette.getLightVibrantColor(
                endPalette.getMutedColor(Color.TRANSPARENT)))

        endBottomColor = endPalette.getDominantColor(
                endPalette.getDarkVibrantColor(endPalette.getDarkMutedColor(
                        Color.TRANSPARENT)))
      }

      val topColor = argbEvaluator.evaluate(positionOffset, startTopColor, endTopColor) as Int
      val bottomColor = argbEvaluator.evaluate(positionOffset, startBottomColor,
              endBottomColor) as Int

      (creativeRoot.background as GradientDrawable).colors = intArrayOf(topColor, bottomColor)
    }
  }

  private var View.size: Int
    get() {
      return layoutParams.width
    }
    set(size) {
      layoutParams.width = size
      layoutParams.height = size
    }
}


