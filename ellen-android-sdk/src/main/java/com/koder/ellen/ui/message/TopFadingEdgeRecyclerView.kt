package com.koder.ellen.ui.message

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


internal class TopFadingEdgeRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getBottomFadingEdgeStrength(): Float {
        return 0f
    }
}