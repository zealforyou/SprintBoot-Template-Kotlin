package com.zz.mini.ss.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OkTest {
    companion object {
        fun getClient(): OkHttpClient {
           return OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        }

        fun test() {
            CoroutineScope(Dispatchers.IO).launch {
                val result = kotlin.runCatching {
                   getClient().newCall(Request.Builder().url("www.baidu.com").build()).execute()
               }
                println(result.getOrNull()?.body()?.string())
            }
        }
    }
}