package com.whitedns.vpn

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object TopSharesBuilder {
 private val io=Executors.newSingleThreadExecutor()
 private val main=Handler(Looper.getMainLooper())
 data class Share(val id:Int,val title:String,val proto:String,val host:String,val owner:String,val taken:Int,val health:Int,val url:String)
 fun load(activity:Activity, target:LinearLayout, add:(Share)->Unit, show:(String)->Unit){
  target.removeAllViews(); show(activity.getString(R.string.shares_loading))
  io.execute {
   val list=mutableListOf<Share>(); var err:String?=null
   try{
    val c=URL("https://api.enterprise-shield.ru/v1/shares").openConnection() as HttpURLConnection
    c.connectTimeout=8000;c.readTimeout=12000
    val a=org.json.JSONObject(c.inputStream.bufferedReader().use{it.readText()}).optJSONArray("shares")
    if(a!=null) for(i in 0 until a.length()){val x=a.getJSONObject(i);list+=Share(x.optInt("id"),x.optString("title"),x.optString("proto"),x.optString("host"),x.optString("owner_username").ifBlank{x.optString("owner")},x.optInt("taken"),x.optInt("health_score"),x.optString("url"))}
   }catch(e:Exception){err=e.message}
   main.post{if(err!=null)show(activity.getString(R.string.shares_error)) else if(list.isEmpty())show(activity.getString(R.string.shares_empty)) else {show("");list.forEach(add)}}
  }
 }
}
