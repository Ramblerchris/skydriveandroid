package com.library.base.net.inteceptor

import com.library.base.config.GlobalUser
import okhttp3.Interceptor
import okhttp3.Response
import com.library.base.config.Constant
import okhttp3.Request
import java.util.concurrent.TimeUnit


class CommentInterceptor : Interceptor {
    @Throws(Exception::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request: Request = chain.request()
            request.header(Constant.CONNECT_TIMEOUT)?.let {
                var connectTimeout = Integer.valueOf(it)
                chain.withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            }
            request.header(Constant.READ_TIMEOUT)?.let {
                var readTimeout = Integer.valueOf(it)
                chain.withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
            }
            request.header(Constant.WRITE_TIMEOUT)?.let {
                var writeTimeout = Integer.valueOf(it)
                chain.withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            }

           return chain.proceed(chain.request().newBuilder().run {
                GlobalUser.token?.let {
                    addHeader("token", it)
                }
                build()
            })
        } catch (e: Exception) {
            throw e
        }
    }

}