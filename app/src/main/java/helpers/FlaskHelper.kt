package helpers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FlaskHelper {

    var pinsListData: MutableLiveData<PinsList> = MutableLiveData()

//    lateinit var pinsListData: MutableLiveData<PinsList>
//
//    init {
//        pinsListData = MutableLiveData()
//    }

    fun getPinsListObservable() : MutableLiveData<PinsList> {
        return pinsListData
    }

//    fun getAllPins() {
//        //logic to get all pins
//        val retroInstance = RetroInstance.getRetroInstance().create(RetroService::class.java)
//        val call = retroInstance.getPinsList()
//        call.enqueue(object : Callback<PinsList>{
//            override fun onFailure(call: Call<PinsList>, t: Throwable) {
//                Log.d("TAG", "An error happened!")
//                t.printStackTrace()
//                pinsListData.postValue(null)
//            }
//
//            override fun onResponse(call: Call<PinsList>, response: Response<PinsList>) {
//                Log.d("TAG_", response.body().toString())
//                if(response.isSuccessful) {
//                    pinsListData.postValue(response.body())
//                } else {
//                    pinsListData.postValue(null)
//                }
//            }
//        })
//    }
//    fun createPin() {
//        //logic to create new pin
//    }
}