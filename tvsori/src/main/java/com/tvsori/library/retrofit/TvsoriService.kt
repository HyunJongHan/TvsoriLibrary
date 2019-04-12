package com.tvsori.library.retrofit

import com.tvsori.library.retrofit.model.TvsoriArrayResponse
import com.tvsori.library.retrofit.model.TvsoriResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*

interface TvsoriService {
    @FormUrlEncoded
    @POST("/errorLog")
    fun errorLog(@Field("error_msg") error_msg: String, @Field("filename") filename: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/chkVersion")
    fun chkVersion(@Field("app_code") app_code: String, @Field("app_package") app_package: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/loginAndroid")
    fun loginAndroid(@Field("userid") userid: String, @Field("password") password: String, @Field("deviceid") deviceid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/loginGoogle")
    fun loginGoogle(@Field("email") email: String, @Field("password") password: String, @Field("deviceid") deviceid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/userItemList")
    fun userItemList(@Field("userid") userid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/user/updateUserItem")
    fun updateUserItem(@Field("userid") userid: String, @Field("itemcode") itemcode: String, @Field("price") price: String, @Field("amount") amount: String, @Field("ordernum") ordernum: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/joinAndroid")
    fun joinAndroid(@Field("joinid") userid: String, @Field("password") password: String, @Field("usernick") nickname: String, @Field("usersex") sex: String, @Field("useryear") birth: String, @Field("deviceid") deviceid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/joinGoogle")
    fun joinGoogle(@Field("email") email: String, @Field("joinid") userid: String, @Field("password") password: String, @Field("usernick") nickname: String, @Field("usersex") sex: String, @Field("useryear") birth: String, @Field("deviceid") deviceid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/chkUserId")
    fun chkUserId(@Field("userid") userid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/chkUserNick")
    fun chkUserNick(@Field("usernick") usernick: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/user/userOut")
    fun userOut(@Field("userid") userid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/tvsori/appRoomList")
    fun appRoomList(@Field("userid") userid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/tvsori/check_createroom")
    fun checkCreateroom(@Field("check_userid") check_userid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/tvsori/check_guest_enterroom")
    fun checkGuestEnterroom(@Field("roomid") roomid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/tvsori/noticelist")
    fun noticelist(@Field("userid") userid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/tvsori/noticeview")
    fun noticeview(@Field("num") num: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/tvsori/enterAppRoom")
    fun enterAppRoom(@Field("roomid") roomid: String, @Field("passwd") passwd: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/tvsori/songFavorite")
    fun songFavorite(@Field("userid") userid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/tvsori/songFavoriteUpdate")
    fun songFavoriteDel(@Field("userid") userid: String, @Field("songno") songno: String, @Field("is_add") is_add: String = "N"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/tvsori/songFavoriteUpdate")
    fun songFavoriteAdd(@Field("userid") userid: String, @Field("songno") songno: String, @Field("songname") songname: String, @Field("songsinger") songsinger: String, @Field("is_add") is_add: String = "Y"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/contest_list")
    fun contestList(@Field("startdate") startdate: String, @Field("enddate") enddate: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/audition/contest_list")
    fun contestSubscribeList(@Field("type") type: String, @Field("userid") userid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/audition/contest_list")
    fun contestUserList(@Field("type") type: String, @Field("ownerid") ownerid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/audition/check_add_contest")
    fun checkAddContest(@Field("userid") userid: String, @Field("filename") filename: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/delete_contest")
    fun deleteContest(@Field("idx") idx: String, @Field("userid") userid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/subscribe_contest")
    fun subscribeContest(@Field("subscribe") subscribe: String, @Field("ownerid") ownerid: String, @Field("userid") userid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/view_contest")
    fun viewContest(@Field("idx") idx: String, @Field("userid") userid: String, @Field("ownerid") ownerid: String, @Field("useridx") useridx: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/contest_comment")
    fun addContestComment(@Field("idx") idx: String, @Field("userid") userid: String, @Field("useridx") useridx: String, @Field("nick") nick: String, @Field("comment") comment: String, @Field("is_add") isAdd: String = "Y"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/contest_comment")
    fun delContestComment(@Field("comment_idx") comment_idx: String, @Field("useridx") useridx: String, @Field("is_add") isAdd: String = "N"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/contest_star")
    fun addContestStar(@Field("idx") idx: String, @Field("useridx") useridx: String, @Field("star") star: String, @Field("is_add") isAdd: String = "Y"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/contest_star")
    fun delContestStar(@Field("idx") idx: String, @Field("useridx") useridx: String, @Field("is_add") isAdd: String = "N"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/audition/contest_comment_list")
    fun contestCommentList(@Field("idx") idx: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/settop/duetMedia")
    fun insertDuetMedia(@Field("userid") userid: String, @Field("path") path: String, @Field("type") type: String, @Field("is_add") is_add: String = "Y"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/settop/duetMedia")
    fun deleteDuetMedia(@Field("userid") userid: String, @Field("path") path: String, @Field("type") type: String, @Field("filename") filename: String, @Field("is_add") is_add: String = "N"): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/settop/duetUserInfo")
    fun duetUserInfo(@Field("userid") userid: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/settop/duetUserImageList")
    fun duetUserImageList(@Field("userid") userid: String): Call<TvsoriArrayResponse>

    @FormUrlEncoded
    @POST("/settop/registerDuetUser")
    fun registerDuetUser(@Field("userid") userid: String, @Field("usernick") usernick: String, @Field("message") info: String, @Field("path") mainpath: String): Call<TvsoriResponse>

    @FormUrlEncoded
    @POST("/settop/updateDuetUser")
    fun updateDuetUser(@Field("userid") userid: String, @Field("duet_mode") duet_mode: String): Call<TvsoriResponse>

    @Multipart
    @POST("/file/uploadDuetImage")
    fun uploadImage(@Part("userid") userid: RequestBody, @Part file: MultipartBody.Part): Call<TvsoriResponse>

    companion object {
        fun create(): TvsoriService {
            return Retrofit.Builder().baseUrl("http://218.145.161.133:7163").addConverterFactory(GsonConverterFactory.create()).build().create()
        }
    }
}