package ir.pooyande.amar.data

import ir.pooyande.amar.util.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface AmarApi {

    @POST("${Constants.API_ENDPOINT}?action=login")
    suspend fun login(@Body body: Map<String, String>): ApiResponse<LoginData>

    @GET("${Constants.API_ENDPOINT}?action=my_roles")
    suspend fun myRoles(): ApiResponse<List<Role>>

    @GET("${Constants.API_ENDPOINT}?action=search")
    suspend fun search(
        @Query("q") query: String,
        @Query("role_id") roleId: Int
    ): ApiResponse<List<CaseItem>>

    @POST("${Constants.API_ENDPOINT}?action=claim_case")
    suspend fun claimCase(@Body body: ClaimRequest): ApiResponse<ClaimResult>

    @GET("${Constants.API_ENDPOINT}?action=me")
    suspend fun me(): ApiResponse<UserProfile>

    @POST("${Constants.API_ENDPOINT}?action=logout")
    suspend fun logout(): ApiResponse<Any>
}

object ApiClient {

    private var tokenProvider: (() -> String?)? = null

    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenProvider?.invoke()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            original.newBuilder()
                .header("Accept", "application/json")
                .build()
        }
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: AmarApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AmarApi::class.java)
    }
}
