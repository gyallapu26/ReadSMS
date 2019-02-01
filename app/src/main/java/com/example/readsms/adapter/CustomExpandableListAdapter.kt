package com.example.readsms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.readsms.R
import com.example.readsms.entity.Message


class CustomExpandableListAdapter(
    private val context: Context, private var expandableListTitle: MutableList<Int>,
    private var expandableListDetail: HashMap<Int, MutableList<Message>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Message {
        return this.expandableListDetail[this.expandableListTitle[listPosition]]?.get(expandedListPosition)!!
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View? {
        var convertView = convertView
        val message = getChild(listPosition, expandedListPosition) as Message
        if (convertView == null) {
            val layoutInflater = this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_message, null)
        }

        message.body.let {
            convertView?.findViewById<TextView>(R.id.message_tv)?.text = it
        }
        message.address.let {
            convertView?.findViewById<TextView>(R.id.sender_tv)?.text = it
        }

        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.expandableListDetail[this.expandableListTitle[listPosition]]?.size!!
    }

    override fun getGroup(listPosition: Int): Any {
        return this.expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View? {

        var convertView = convertView
        val listTitle = getGroup(listPosition) as Any
        if (convertView == null) {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_header, null)
        }
        convertView?.findViewById<TextView>(R.id.header_title_tv)?.text = listTitle.toString() + " hours ago "
      /*  val parent = parent as ExpandableListView
        parent.expandGroup(listPosition)*/
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

    fun updateList(keys: MutableSet<Int>, it: HashMap<Int, MutableList<Message>>) {

        this.expandableListTitle = keys.toMutableList()
        this.expandableListDetail = it
        this.notifyDataSetChanged()
    }
}