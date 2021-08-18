package com.library.base.net

import com.library.BuildConfig
import com.library.base.config.Constant
import com.library.base.net.inteceptor.CommentInterceptor
import com.library.base.net.inteceptor.HttpLoggingInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class RetrofitClient {

    private fun createRetrofit(){
        retrofit = Retrofit.Builder()
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constant.BASE_URL).build()
    }

    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(Constant.Timeout, TimeUnit.SECONDS)
            .readTimeout(Constant.Timeout, TimeUnit.SECONDS)
            .writeTimeout(Constant.Timeout, TimeUnit.SECONDS)
            .addNetworkInterceptor(CommentInterceptor())
        if (BuildConfig.DEBUG) {
            var httpLoggingInterceptor = HttpLoggingInterceptor(Constant.HttpTAG)
            httpLoggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
            httpLoggingInterceptor.setColorLevel(Level.INFO)
            builder.addNetworkInterceptor(httpLoggingInterceptor)
        }
        return builder.connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))
                .build()
    }

    private object SingleRetorfit {
        val INSTANCE = RetrofitClient()
    }

    companion object {
        fun getInstance() = SingleRetorfit.INSTANCE
        private  var retrofit: Retrofit? =null
    }

    fun updateBaseUrl(ip: String) {
        Constant.setBaseUrl(ip)
        createRetrofit()
    }

    fun <T> create(service: Class<T>?): T {
        if(retrofit==null){
            synchronized(this){
                if(retrofit==null){
                    createRetrofit();
                }
            }
        }
       return  retrofit!!.create(service!!) ?: throw RuntimeException("api is null ")
    }
}