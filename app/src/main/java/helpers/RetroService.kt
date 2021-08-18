package helpers

import retrofit2.Call
import retrofit2.http.*

interface RetroService {

    @GET("pins")
    fun getPinsList(): Call<List<Pin>>

//    @GET("pins/{pin_id}")
//    fun getPin(@Path("pin_id") pin_id: String): Call<Pin>

    @POST("pins")
    fun createPin(@Body params: Pin): Call<PinResponse>

    @PUT("pins/{pin_id}")
    fun updatePin(@Path("pin_id") pin_id: String, @Body params: Pin): Call<PinResponse>

    @DELETE("pins/{pin_id}")
    fun deletePin(@Path("pin_id") pin_id: String): Call<PinResponse>
}