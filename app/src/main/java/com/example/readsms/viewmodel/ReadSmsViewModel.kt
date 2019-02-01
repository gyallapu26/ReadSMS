package com.example.readsms.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.net.Uri
import com.example.readsms.entity.Message
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.LinkedHashMap


class ReadSmsViewModel(private var context: Application) : AndroidViewModel(context) {

    private lateinit var  messagesHashMap : MutableLiveData<LinkedHashMap<Int, MutableList<Message>>>


    fun getMessages() : LiveData<LinkedHashMap<Int, MutableList<Message>>>{

        if (!::messagesHashMap.isInitialized) messagesHashMap = MutableLiveData()
        return messagesHashMap
    }


    fun loadSmsFromProvider(){
         val hashMap  : LinkedHashMap<Int, MutableList<Message>> = linkedMapOf()

        runBlocking {
            GlobalScope.async { hashMap[0] = getMessageOfDefinedTime(0) }.await()
           GlobalScope.async { hashMap[1] = getMessageOfDefinedTime(1) }.await()
            GlobalScope.async { hashMap[2] = getMessageOfDefinedTime(2) }.await()
            GlobalScope.async { hashMap[3] = getMessageOfDefinedTime(3) }.await()
            GlobalScope.async { hashMap[6] = getMessageOfDefinedTime(6) }.await()
            GlobalScope.async { hashMap[12] = getMessageOfDefinedTime(12) }.await()
            GlobalScope.async { hashMap[24] = getMessageOfDefinedTime(24) }.await()



            messagesHashMap.value = hashMap

        }



    }

    suspend fun getMessageOfDefinedTime(hoursAgo : Int) : MutableList<Message>{

        val messages  : MutableList<Message> = mutableListOf()
        val message = Uri.parse("content://sms/inbox")
        val cr = context.contentResolver
        val date = Date((System.currentTimeMillis() - ((hoursAgo + 1) * 3600 * 1000))).time
        val currentdate = Date(System.currentTimeMillis()- ((hoursAgo ) * 3600 * 1000) ).time

        Log.d("sos", "date is "+ date)
        val c =
            context.contentResolver.query(message, null, "date<=? AND date>=?" , arrayOf(""+ currentdate, "" + date ), "date DESC")

        val totalSMS = c!!.getCount()

        if (c.moveToFirst()) {
            for (i in 0 until totalSMS) {
                val message = Message()

                Log.d("sos", "date  is "+ (c.getString(c.getColumnIndexOrThrow("date"))))
                val timestamp = java.lang.Long.parseLong(c.getString(c.getColumnIndexOrThrow("date")))
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                val finaldate = calendar.time
                val smsDate = finaldate.toString()
                message.date = smsDate
                message.address = c.getString(c.getColumnIndexOrThrow("address"))
                message.body = c.getString(c.getColumnIndexOrThrow("body"))
                Log.d("sos", "read  is "+ c.getString(c.getColumnIndexOrThrow("read")))

                messages.add(message)
                c.moveToNext()
            }
        } else {
            Log.d("sos", "You have no SMS in Inbox   ")
        }
        c.close()
        return  messages

    }

}