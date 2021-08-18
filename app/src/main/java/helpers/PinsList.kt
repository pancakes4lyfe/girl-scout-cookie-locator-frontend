package helpers

// this expects an object called results who's value is a list not a straight list
//data class PinsList(val results: List<Pin>)
data class Pin(val id: String, val lat_lon: String, val notes: String, val pinned_at: String, val hours: String, val cookies_available: String, val upvote_count: Int)
data class PinResponse(val details: String, val data: Pin?)