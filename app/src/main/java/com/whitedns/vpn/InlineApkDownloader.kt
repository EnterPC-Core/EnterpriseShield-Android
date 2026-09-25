package com.whitedns.vpn

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

object InlineApkDownloader {
    val active=AtomicBoolean(false); val failed=AtomicBoolean(false); val done=AtomicBoolean(false)
    val bytes=AtomicLong(0); val total=AtomicLong(-1); val error=AtomicReference("")
    private val executor=Executors.newSingleThreadExecutor()
    fun reset(){ active.set(false);failed.set(false);done.set(false);bytes.set(0);total.set(-1);error.set("") }
    fun start(context:Context,url:String,target:File){
        if(active.get())return
        reset();active.set(true);target.parentFile?.mkdirs();val tmp=File(target.parentFile,"update.apk.part")
        DiagnosticLogger.info(context,"ota.inline.start","url=$url target=${target.absolutePath}")
        executor.execute {
            var c:HttpURLConnection?=null
            try{
                c=URL(url).openConnection() as HttpURLConnection;c.connectTimeout=20000;c.readTimeout=60000;c.instanceFollowRedirects=true;c.setRequestProperty("User-Agent","EnterpriseShield/inline")
                val code=c.responseCode;if(code !in 200..299)throw IOException("HTTP $code")
                total.set(c.contentLengthLong);DiagnosticLogger.info(context,"ota.inline.headers","http=$code total=${total.get()}")
                c.inputStream.use{input->FileOutputStream(tmp).use{out->val b=ByteArray(64*1024);while(true){val n=input.read(b);if(n<0)break;out.write(b,0,n);bytes.addAndGet(n.toLong())};out.flush()}}
                if(target.exists())target.delete();if(!tmp.renameTo(target))throw IOException("APK rename failed")
                done.set(true);DiagnosticLogger.info(context,"ota.inline.done","bytes=${bytes.get()} file=${target.length()}")
            }catch(e:Exception){tmp.delete();error.set("${e.javaClass.simpleName}: ${e.message}");failed.set(true);DiagnosticLogger.warn(context,"ota.inline.failed",error=e)}finally{c?.disconnect();active.set(false)}
        }
    }
}
