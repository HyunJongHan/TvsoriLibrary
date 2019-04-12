package com.tvsori.library.retrofit

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

interface ZillerService {
    @GET("/app/smGatewayNew.asp")
    fun songlist(@Query("specId") specId: String): Call<String>

    @GET("/app/smGatewayNew.asp")
    fun songlist(@Query("specId") specId: String, @Query("searchDiv") searchDiv: String, @Query("searchValue") searchValue: String): Call<String>

    companion object {
        fun create(): ZillerService {
            return Retrofit.Builder().baseUrl("http://tvsori.com").addConverterFactory(ScalarsConverterFactory.create()).build().create()
        }
    }
}