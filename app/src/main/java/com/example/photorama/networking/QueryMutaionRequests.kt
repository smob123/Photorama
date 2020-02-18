package com.example.photorama.networking

import android.content.Context
import android.content.Intent
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.photorama.LoginActivity
import com.example.photorama.heplerObjects.CacheHandler

/**
 * responsible for making query, and mutation requests to the server.
 */
class QueryMutationRequests<D : Operation.Data, T : Operation.Data, V : Operation.Variables>(private val context: Context) {

    /**
     * send a query request to the server.
     * @param query the query request
     * @param onCompleted callback that returns the result of the query
     */
    fun runQuery(
        query: Query<D, T, V>,
        onCompleted: (err: String?, res: T?) -> Unit
    ) {
        // server's response
        var res: T? = null
        // stores any error messages returned
        var err: String? = null

        // make the query request
        MApolloClient().setupApollo().query(
            query
        ).enqueue(object : ApolloCall.Callback<T>() {
            override fun onFailure(e: ApolloException) {
                err = e.message.toString()
                onCompleted(err, res)
            }

            override fun onResponse(response: Response<T>) {
                if (response.data() != null) {
                    res = response.data()
                }

                if (response.errors().size > 0) {
                    val message = response.errors()[0].message()
                    if (message!!.contains("Authentication Error")) {
                        logout()
                    }

                    err = response.errors()[0].toString()
                }

                onCompleted(err, res)
            }
        })
    }

    /**
     * send a mutation request to the server.
     * @param mutation the mutation to request
     * @param onCompleted callback that returns the result of the mutation
     */
    fun runMutation(
        mutation: Mutation<D, T, V>,
        onCompleted: (err: String?, res: T?) -> Unit
    ) {
        // server's response
        var res: T? = null
        // stores any error messages returned
        var err: String? = null

        // make the query request
        MApolloClient().setupApollo().mutate(
            mutation
        ).enqueue(object : ApolloCall.Callback<T>() {
            override fun onFailure(e: ApolloException) {
                err = e.message.toString()
                onCompleted(err, res)
            }

            override fun onResponse(response: Response<T>) {
                if (response.data() != null) {
                    res = response.data()
                }

                if (response.errors().size > 0) {
                    val message = response.errors()[0].message()
                    if (message!!.contains("Authentication Error")) {
                        logout()
                    }

                    err = response.errors()[0].toString()
                }

                onCompleted(err, res)
            }
        })
    }

    private fun logout() {
        Mutations(context).logout(onCompleted = { err, res -> })
        CacheHandler(context).deleteCacheDir()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }
}
