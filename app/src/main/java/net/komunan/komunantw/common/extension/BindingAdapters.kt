package net.komunan.komunantw.common.extension

import androidx.databinding.BindingAdapter
import com.facebook.drawee.view.SimpleDraweeView

@BindingAdapter("imageUrl")
fun SimpleDraweeView.loadImage(url: String?) {
    this.setImageURI(url)
}
