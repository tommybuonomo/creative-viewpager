# Creative View Pager

Creative View Pager easy to use !
This view pager library combines a header list which is scrolling automatically in coordination with the page contents.

![CreativeViewPager](/art/creative-viewpager1.gif)

## Download
```Gradle
dependencies {
    implementation 'com.tbuonomo:creative-viewpager:1.0.1'
}
```

## Usage

### In your XML layout
Add `CreativeViewPager` in your activity's layout
```Xml
<com.tbuonomo.creativeviewpager.CreativeViewPager
    android:id="@+id/creativeViewPagerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:contentHorizontalPadding="32dp"
    app:contentMargin="8dp"
    app:imagesMargin="8dp"
    app:imagesSize="92dp"
    />
```
You will see a fake preview in Android Studio of the creative view.

### Custom XML Attributes
| Attribute | Description |
| --- | --- |
| `contentHorizontalPadding` | The start and end padding in dp of the content pages relative to the parent layout (by default 32dp) |
| `contentItemMargin` | Margin in dp of each content item (by default 8dp) |
| `headerItemMargin` | Margin in dp of each header item (by default 8dp) |
| `headerItemSize` | The size in dp of the header items (by default 92dp) |

### Adapter
Create your own adapter extending `CreativeViewAdapter`
```Kotlin
class NatureCreativePagerAdapter(val context: Context) : CreativePagerAdapter
```

By default, you have to override the 3 methods `instantiateHeaderItem`, `instantiateContentItem` and `getCount`.

```Kotlin
    override fun instantiateHeaderItem(inflater: LayoutInflater, container: ViewGroup, position: Int): View {
    // Inflate page layout
    val headerRoot = inflater.inflate(R.layout.item_creative_content_nature, container, false)

    // Bind the views
    val title: TextView = headerRoot.findViewById(R.id.itemCreativeNatureTitle)
    val image: ImageView = headerRoot.findViewById(R.id.itemCreativeNatureImage)

    title.text = context.getString(R.string.item_nature_title, position)
    image.setImageDrawable(context.getDrawable(NatureItem.values()[position].natureDrawable))

    return headerRoot
  }

  override fun instantiateContentItem(inflater: LayoutInflater, container: ViewGroup, position: Int): View {
    // Inflate the header view layout
    val contentRoot = inflater.inflate(R.layout.item_creative_header_profile, container,
            false)

    // Bind the views
    val imageView = contentRoot.findViewById<ImageView>(R.id.itemCreativeImage)

    imageView.setImageDrawable(ContextCompat.getDrawable(context, NatureItem.values()[position].userDrawable))
    return contentRoot
  }

  override fun getCount(): Int {
    return NatureItem.values().size
  }
```

If you want to have the cool colored background effect when you're sliding the view pager, you have to override the two methods and provide the bitmap used for specific position.

```Kotlin
  override fun isUpdatingBackgroundColor(): Boolean {
    return true
  }

  override fun requestBitmapAtPosition(position: Int): Bitmap? {
    // Return the bitmap used for the position
    return BitmapFactory.decodeResource(context.resources,
            NatureItem.values()[position].natureDrawable)
  }
```

### In your Activity / Fragment
Finally, set your adapter to the `CreativeViewPager` in your activity or fragment.
```Kotlin
creativeViewPagerView.setCreativeViewPagerAdapter(NatureCreativePagerAdapter(activity))
```

Don't forget to star the project if you like it! 
![star](https://user-images.githubusercontent.com/15737675/39397370-85f5b294-4afe-11e8-9c02-0dfdf014136a.png)
 == ![heart](https://user-images.githubusercontent.com/15737675/39397367-6e312c2e-4afe-11e8-9fbf-32001b0165a1.png)

## Changelog
### 1.0.1
- Rename class `CreativeViewPagerView` to `CreativeViewPager`
- Rename class `CreativeViewAdapter` to `CreativePagerAdapter`
- Rename attribute `imagesMargin` to `headerItemMargin`
- Rename attribute `imagesSize` to `headerItemSize`
- Rename attribute `contentMargin` to `contentItemMargin`
- Add the `setCurrentItem(position: Int)` to `CreativeViewPager`


[Icons](https://www.flaticon.com/packs/profession-avatars) designed by [Freepics](https://www.flaticon.com/authors/freepik) from [Flaticon](https://www.flaticon.com/)

## License
    Copyright 2018 Tommy Buonomo
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
