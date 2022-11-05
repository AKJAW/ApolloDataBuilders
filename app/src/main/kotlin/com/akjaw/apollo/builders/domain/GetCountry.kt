package com.akjaw.apollo.builders.domain

import android.util.Log
import com.akjaw.apollo.builders.domain.model.Country
import com.akjaw.apollo.builders.domain.model.CountryConverter
import com.akjaw.apollo.builders.generated.CountryQuery
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.LoggingInterceptor

class GetCountry(
    private val apolloClient: ApolloClient = productionClient,
    private val countryConverter: CountryConverter = CountryConverter(),
) {

    suspend fun execute(code: String): Country? {
        val apolloResponse = try {
            apolloClient.query(CountryQuery(code)).execute()
        } catch (e: Exception) {
            return null
        }
        if (apolloResponse.hasErrors()) return null

        val countrySchema = apolloResponse.data?.country ?: return null
        return countryConverter.convert(countrySchema)
    }
}

private val productionClient = ApolloClient.Builder()
    .serverUrl("https://countries.trevorblades.com/")
    .addHttpInterceptor(LoggingInterceptor { Log.d("API", it) })
    .build()
