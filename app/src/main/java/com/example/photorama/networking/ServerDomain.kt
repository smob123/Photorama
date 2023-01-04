package com.example.photorama.networking

import okhttp3.HttpUrl

/**
 * @author Sultan
 * the urls to the server.
 */

class ServerDomain {

    /**
     * returns the server's GraphQL's url as an HttpUrl
     * @return server's url
     */
    fun httpUrl(): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("photorama-server.herokuapp.com")
            .addPathSegment("graphql")
            .build()
    }

    /**
     * the server's main domain.
     * @return the server's main domain as a string
     */
    fun baseUrlString(): String {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("photorama-server.herokuapp.com")
            .build()
            .toString()

        // drop the backslash "/" from the end of the url
        return url.dropLast(1)
    }
}