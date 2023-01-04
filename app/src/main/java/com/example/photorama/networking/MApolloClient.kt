package com.example.photorama.networking

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * @author Sultan
 * initiates apollo client connection.
 */
class MApolloClient {

    /**
     * establishes connection to the server.
     * @return ApolloClient that can be used to make queries
     */
    fun setupApollo(): ApolloClient {
        // server's url
        val BASE_URL = ServerDomain().httpUrl()

        // create a LoggingInterceptor
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        // create an http client with a 1 minute timeout
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()

        // build and return the ApolloClient
        return ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(client)
            .build()
    }
}
