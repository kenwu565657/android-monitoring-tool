package com.monitoring.app.layout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.monitoring.app.R

class GridViewAdapter(private val context: Context) : BaseAdapter() {

    private val imageIdList = listOf(
        R.drawable.twotone_monitor_24, R.drawable.delete_24dp_000000_fill0_wght400_grad0_opsz24,
        R.drawable.baseline_arrow_circle_up_24, R.drawable.baseline_android_24,
        R.drawable.baseline_add_a_photo_24, R.drawable.terminal_24dp_000000_fill0_wght400_grad0_opsz24
    )
    private val stringIdList = listOf(
        "Start Monitoring", "Uninstall App",
        "Version Update", "About App",
        "Test Camera", "Shell Command"
    )

    override fun getCount() = imageIdList.size

    override fun getItem(position: Int): Any {
        return imageIdList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.items, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.imageView.setImageResource(imageIdList[position])
        viewHolder.textView.text = stringIdList[position]

        return view!!
    }

    private class ViewHolder(view: View) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView: TextView = view.findViewById(R.id.textView)
    }
}
