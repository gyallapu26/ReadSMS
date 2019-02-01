package com.example.readsms


import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.example.readsms.adapter.CustomExpandableListAdapter
import kotlinx.android.synthetic.main.fragment_blank.*
import com.example.readsms.services.ReadSmsService
import com.example.readsms.viewmodel.ReadSmsViewModel


class ReadSmsFragment : Fragment() {

    private lateinit var readSmsViewModel: ReadSmsViewModel
    private val  REQUEST_CODE_ASK_PERMISSIONS = 123
    private val recieve_sms_permission = 26
    private val customExpandableListAdapter : CustomExpandableListAdapter by lazy {
        CustomExpandableListAdapter(context!!, mutableListOf(), hashMapOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        schduleJob()
        initExpandableListView()
        readSmsViewModel = ViewModelProviders.of(this).get(ReadSmsViewModel::class.java)

        readSmsViewModel.getMessages().observe(this , Observer {

            it?.forEach { (i, mutableList) ->
                Log.d("sos", "hashmap  for key $i is $mutableList" )
                it?.let {
                    customExpandableListAdapter.updateList(it.keys, it)
                    expandGroup()
                }

            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity?.baseContext?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.RECEIVE_SMS) } == PackageManager.PERMISSION_GRANTED)
            {

            }
            else requestPermissions(arrayOf(android.Manifest.permission.RECEIVE_SMS), recieve_sms_permission)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity?.baseContext?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.READ_SMS) } == PackageManager.PERMISSION_GRANTED)
                loadSmsFromProvider()
            else requestPermissions(arrayOf(android.Manifest.permission.READ_SMS), REQUEST_CODE_ASK_PERMISSIONS)
        }else loadSmsFromProvider()
    }

    private fun schduleJob() {


        val componentName = ComponentName(context, ReadSmsService::class.java)
        val jobInfo  = JobInfo.Builder(123, componentName) .setOverrideDeadline(0).setPersisted(true).build()
        val jobScheduler = context?.getSystemService(JobService.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = jobScheduler.schedule(jobInfo)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("sos", "Job scheduled");
        } else {
            Log.d("sos", "Job scheduling failed");
        }



      /*  val serviceIntent = Intent(context, ReadSmsService::class.java)
       // serviceIntent.putExtra("inputExtra", "My input")

        ContextCompat.startForegroundService(context!!, serviceIntent)*/

    }

    private fun initExpandableListView() {
        expandableListView.setAdapter(customExpandableListAdapter)
    }
    private fun expandGroup(){
        for (i in 0 until customExpandableListAdapter.groupCount){
            expandableListView.expandGroup(i)
        }
    }


    private fun loadSmsFromProvider() {
        Log.d("sos" , "")
        readSmsViewModel.loadSmsFromProvider()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSmsFromProvider()
            }
            recieve_sms_permission -> {}
        }
    }


}
