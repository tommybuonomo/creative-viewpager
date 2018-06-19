package com.tbuonomo.creativeviewpager

import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v7.graphics.Palette
import android.util.LruCache
import com.tbuonomo.creativeviewpager.adapter.CreativePagerAdapter
import java.util.concurrent.ConcurrentHashMap

class PaletteCacheManager {
  companion object {
    private const val BITMAP_PREFIX = "CREATIVE_BITMAP_"
  }

  private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
  private val cacheSize = maxMemory / 8

  private val palettes: HashMap<String, Palette> = HashMap()
  private val runningAsyncs: ConcurrentHashMap<String, CachePalettesAsync> = ConcurrentHashMap()

  private var creativePagerAdapter: CreativePagerAdapter? = null

  private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSize) {
    override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
      return bitmap?.byteCount?.div(1024) ?: 0
    }
  }

  fun getPalette(key: String): Palette? {
    return palettes[key]
  }

  private fun addBitmapToCache(key: String, bitmap: Bitmap) {
    if (getBitmapFromCache(key) == null) {
      memoryCache.put(key, bitmap)
    }
  }

  private fun getBitmapFromCache(key: String): Bitmap? {
    return memoryCache.get(key)
  }

  fun addPalette(key: String, palette: Palette) {
    palettes[key] = palette
  }

  fun getPaletteForPosition(position: Int): Palette? {
    val key = getKeyByPosition(position)
    return getPalette(key)
  }

  fun cachePalettesAroundPositionAsync(position: Int) {
    cachePalettesAroundPositionAsync(position, {})
  }

  fun cachePalettesAroundPositionAsync(position: Int, onPaletteCachedListener: () -> Unit) {
    val key = getKeyByPosition(position)
    val cachePalettesAsync = CachePalettesAsync(this, creativePagerAdapter, position, {
      runningAsyncs.remove(getKeyByPosition(it))
      onPaletteCachedListener()
    })

    if (!runningAsyncs.contains(key)) {
      runningAsyncs[key] = cachePalettesAsync
      cachePalettesAsync.execute()
    }
  }

  private fun getKeyByPosition(position: Int): String {
    return BITMAP_PREFIX.plus(position)
  }

  private class CachePalettesAsync(val paletteCacheManager: PaletteCacheManager,
          val creativePagerAdapter: CreativePagerAdapter?, val position: Int,
          val onPaletteCachedListener: (Int) -> Unit) : AsyncTask<Void, Void, Void>() {
    val cacheRadius = 4
    override fun doInBackground(vararg positions: Void): Void? {
      var startIndex = position - cacheRadius
      var endIndex = position + cacheRadius

      if (startIndex < 0) {
        startIndex = 0
      }

      if (endIndex > creativePagerAdapter?.getCount() ?: 0 - 1) {
        endIndex = creativePagerAdapter?.getCount() ?: 0 - 1
      }

      for (i in startIndex until endIndex) {
        val key = BITMAP_PREFIX.plus(i)
        val palette: Palette? = paletteCacheManager.getPalette(key)
        if (palette == null) {
          var bitmap = paletteCacheManager.getBitmapFromCache(key)
          if (bitmap == null) {
            bitmap = creativePagerAdapter?.requestBitmapAtPosition(i)
          }

          if (bitmap != null) {
            val generatedPalette = Palette.from(bitmap).generate()
            paletteCacheManager.addPalette(key, generatedPalette)
          }
        }
      }
      return null
    }

    override fun onPostExecute(result: Void?) {
      super.onPostExecute(result)
      onPaletteCachedListener(position)
    }
  }

  fun setCreativeViewAdapter(creativePagerAdapter: CreativePagerAdapter?) {
    this.creativePagerAdapter = creativePagerAdapter
  }
}