package com.tvsori.library.retrofit

import android.content.Context
import com.tvsori.library.R
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


interface TvsoriComService {
    @GET("/Redirect.asp")
    fun Redirect(@Query("id") id: String, @Query("menu") menu: String, @Query("sc") sc: String): Call<String>

    companion object {
        fun create(context: Context): TvsoriComService {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput = context.resources.openRawResource(R.raw.tvsoricrt)
            var ca: Certificate? = null
            try {
                ca = cf.generateCertificate(caInput)
            } catch (e: CertificateException) {
                e.printStackTrace()
            } finally {
                caInput.close()
            }

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.trustManagers, null)
            val client = OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager).build()
            return Retrofit.Builder().baseUrl("http://tvsori.com").client(client).addConverterFactory(ScalarsConverterFactory.create()).build().create()
        }
    }
}