# Creative View Pager

Creative View Pager easy to use !
This view pager library combines a header list which is scrolling automatically in coordination with the page contents.

## Download
```Gradle
dependencies {
    implementation 'com.tbuonomo:creative-viewpager:1.0.0'
}
```

## Usage

### In your XML layout
Add `CreativeViewPagerView` in your activity's layout
```Xml
<com.tbuonomo.creativeviewpager.CreativeViewPagerView
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
| `contentMargin` | Margin in dp of each content page (by default 8dp) |
| `imagesMargin` | Margin in dp of each header image view (by default 8dp) |
| `imagesSize` | The size in dp of the images layouts in header (by default 92dp) |

### Adapter
Create your own adapter extending `CreativeViewAdapter`
```Kotlin
class NatureCreativeViewAdapter(val context: Context) : CreativeViewAdapter
```

By default, you have to override the 3 methods `bindContentLayoutAt`, `bindProfileLayoutAt` and `getCount`.

```Kotlin
  override fun bindContentLayoutAt(inflater: LayoutInflater,
          container: ViewGroup, position: Int): View {
    // Inflate page layout
    val rootView = inflater.inflate(R.layout.item_creative_content_nature, container, false)

    // Bind the views
    val title: TextView = rootView.findViewById(R.id.itemCreativeNatureTitle)
    val description: TextView = rootView.findViewById(R.id.itemCreativeNatureDescription)
    val image: ImageView = rootView.findViewById(R.id.itemCreativeNatureImage)

    // Set views contents for position
    title.text = context.getString(R.string.item_nature_title, position)
    description.text = context.getString(R.string.lorem_ipsum)

    image.setImageDrawable(context.getDrawable(
            NatureItem.values()[position].natureDrawable))

    return rootView
  }

  override fun bindProfileLayoutAt(inflater: LayoutInflater,
          container: ViewGroup, position: Int): View {
    // Inflate the header view layout
    val profileView = inflater.inflate(R.layout.item_creative_image_profile, container,
            false)

    // Bind the views
    val image = profileView.findViewById<ImageView>(R.id.itemCreativeImage)

    image.setImageDrawable(
            ContextCompat.getDrawable(context,
                    NatureItem.values()[position].userDrawable))
    return profileView
  }

  override fun getCount(): Int {
    return NatureItem.values().size
  }
```

If you want to have the cool colored background effect when you're sliding the view pager, you have to override the two methods 

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
