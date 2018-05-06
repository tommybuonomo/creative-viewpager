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
import com.tbuonomo.creativeviewpager.adapter.CreativePagerAdapter
import com.tbuonomo.creativeviewpager.adapter.CreativeContentAdapter
import com.tbuonomo.creativeviewpager.adapter.CreativeHeaderAdapter
import com.tbuonomo.creativeviewpager.transformer.CreativeContentPageTransformer
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.creativeContentViewPager
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.creativeHeaderRecycler
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.creativeRoot
import kotlinx.android.synthetic.main.layout_creative_view_pager.view.headerGuideline
import kotlinx.android.synthetic.main.layout_creative_view_pager_edit_mode.view.editModeContentLayout
import kotlinx.android.synthetic.main.layout_creative_view_pager_edit_mode.view.editModeHeaderLayout

class CreativeViewPager : FrameLayout {
  companion object {
    const val SCALE_MIN = 0.35f
    const val SCALE_MAX = 1.0f
  }

  private var headerItemMargin = resources.getDimension(R.dimen.dimens_8dp)
  private var contentItemMargin = resources.getDimension(R.dimen.dimens_4dp)
  private var contentHorizontalPadding = resources.getDimension(R.dimen.dimens_32dp)
  private var headerItemSize = resources.getDimension(R.dimen.dimens_92dp)
  private var contentHeight = -1f

  private lateinit var creativeImageAdapter: CreativeHeaderAdapter
  private lateinit var creativeContentAdapter: CreativeContentAdapter

  private var paletteCacheManager: PaletteCacheManager = PaletteCacheManager()
  private val argbEvaluator: ArgbEvaluator = ArgbEvaluator()

  private var creativePagerAdapter: CreativePagerAdapter? = null

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
    setUpHeader()
    setUpContent()
  }

  private fun readAttributes(attrs: AttributeSet?) {
    if (attrs != null) {
      val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CreativeViewPager)

      headerItemSize = ta.getDimension(R.styleable.CreativeViewPager_headerItemSize, headerItemSize)
      contentHeight = ta.getDimension(R.styleable.CreativeViewPager_contentHeight, contentHeight)
      headerItemMargin = ta.getDimension(R.styleable.CreativeViewPager_headerItemMargin,
              headerItemMargin)
      contentItemMargin = ta.getDimension(R.styleable.CreativeViewPager_contentItemMargin,
              contentItemMargin)
      contentHorizontalPadding = ta.getDimension(
              R.styleable.CreativeViewPager_contentHorizontalPadding,
              contentHorizontalPadding)
      ta.recycle()
    }
  }

  private fun setUpEditMode() {
    View.inflate(context, R.layout.layout_creative_view_pager_edit_mode, this)

    val contentItem = LayoutInflater.from(context).inflate(
            R.layout.item_creative_content_placeholder, editModeContentLayout, false)
    editModeContentLayout.setPadding(contentHorizontalPadding.toInt(), 0,
            contentHorizontalPadding.toInt(), 0)
    editModeContentLayout.addView(contentItem, LayoutParams(MATCH_PARENT, MATCH_PARENT))

    for (i in 0..10) {
      val headerItem = LayoutInflater.from(context).inflate(
              R.layout.item_creative_header_placeholder,
              editModeHeaderLayout,
              false)
      val marginLayoutParams = MarginLayoutParams(headerItemSize.toInt(), headerItemSize.toInt())
      val layoutParams = headerGuideline.layoutParams as ConstraintLayout.LayoutParams
      val parentMargin = layoutParams.guidePercent * context.resources.displayMetrics.widthPixels
      if (i == 0) {
        marginLayoutParams.marginStart = (parentMargin - headerItemSize / 2).toInt()
      } else {
        marginLayoutParams.marginStart = headerItemMargin.toInt()
        marginLayoutParams.marginEnd = headerItemMargin.toInt()
        marginLayoutParams.width = (headerItemSize * SCALE_MIN).toInt()
        marginLayoutParams.height = (headerItemSize * SCALE_MIN).toInt()
      }

      editModeHeaderLayout.addView(headerItem, marginLayoutParams)
    }
  }

  private fun setUpContent() {
    // Contents
    creativeContentAdapter = CreativeContentAdapter(this, contentItemMargin,
            contentHorizontalPadding)
    if (contentHeight != -1f) {
      creativeContentViewPager.layoutParams.height = contentHeight.toInt()
      creativeContentViewPager.requestLayout()
    }
    creativeContentViewPager.setPageTransformer(false,
            CreativeContentPageTransformer(contentHorizontalPadding))
    creativeContentViewPager.pageMargin = contentItemMargin.toInt()
    creativeContentViewPager.addOnPageChangeListener(OnContentPageChangeListener())
  }

  private fun setUpHeader() {
    // Create adapter for images
    creativeImageAdapter = CreativeHeaderAdapter(this, headerGuideline, headerItemSize,
            headerItemMargin, {
      creativeContentViewPager.setCurrentItem(it, true)
    })
    creativeHeaderRecycler.layoutParams.height = (headerItemSize + resources.getDimension(
            R.dimen.dimens_16dp) * 2).toInt()
    creativeHeaderRecycler.recycledViewPool.setMaxRecycledViews(0, 0)

    // Delegate onTouch to the content view pager
    creativeHeaderRecycler.setOnTouchListener { _, event ->
      creativeContentViewPager.onTouchEvent(event)
      true
    }
  }

  private fun refreshImagesSize() {
    for (i in 0 until creativeHeaderRecycler.childCount) {
      val itemView = creativeHeaderRecycler.getChildAt(i)

      val centerX = itemView.x + itemView.width.toFloat() / 2
      val diffX = Math.abs(headerGuideline.x - centerX)
      val maxScaleDistance = headerItemSize - headerItemMargin * 2
      val scale = if (diffX < maxScaleDistance) SCALE_MAX - diffX / maxScaleDistance * (SCALE_MAX - SCALE_MIN) else SCALE_MIN
      itemView.size = (headerItemSize * scale).toInt()
    }
    creativeHeaderRecycler.requestLayout()
  }

  private fun refreshImagesPosition(positionOffset: Float, position: Int) {
    val linearLayoutManager = creativeHeaderRecycler.layoutManager as LinearLayoutManager

    val distanceBetweenImages = (headerItemSize * SCALE_MIN + headerItemMargin * 2).toInt()
    val imageOffsetPixels = positionOffset * distanceBetweenImages

    val scrollOffsetPixels = (-(position * distanceBetweenImages + imageOffsetPixels)).toInt()

    linearLayoutManager.scrollToPositionWithOffset(0,
            scrollOffsetPixels)
  }

  private inner class OnContentPageChangeListener : OnPageChangeListener {
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
    if (creativePagerAdapter?.isUpdatingBackgroundColor() == true) {

      // Retrieve palettes from cache
      val startPalette = paletteCacheManager.getPaletteForPosition(position)
      val endPalette = paletteCacheManager.getPaletteForPosition(position + 1)

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

  fun setCreativeViewPagerAdapter(creativePagerAdapter: CreativePagerAdapter) {
    post({
      this.creativePagerAdapter = creativePagerAdapter
      // Setup adapter for palette manager
      paletteCacheManager.setCreativeViewAdapter(creativePagerAdapter)
      paletteCacheManager.cachePalettesAroundPositionAsync(0, {
        refreshBackgroundColor(0, 0f)
      })

      // Setup image adapter
      creativeImageAdapter.creativePagerAdapter = creativePagerAdapter
      creativeHeaderRecycler.layoutManager = LinearLayoutManager(context,
              LinearLayoutManager.HORIZONTAL, false)
      creativeHeaderRecycler.adapter = creativeImageAdapter

      // Setup content adapter
      creativeContentAdapter.creativePagerAdapter = creativePagerAdapter
      creativeContentViewPager.adapter = creativeContentAdapter

      creativeHeaderRecycler.post({ refreshImagesPosition(0f, 0) })
    })
  }

  fun setCurrentItem(position: Int) {
    creativeContentViewPager.setCurrentItem(position, true)
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


