package com.akjaw.apollo.builders.domain

import com.akjaw.apollo.builders.generated.CountryQuery
import com.akjaw.apollo.builders.generated.fragment.LanguageFragment
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.http.HttpBody
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.api.toJsonString
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.LoggingInterceptor
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
internal class GetCountryTest {

    companion object {
        private const val CODE = "GB"
    }

    private lateinit var httpInterceptor: CountryMockInterceptor
    private lateinit var systemUnderTest: GetCountry

    @Before
    fun setUp() {
        httpInterceptor = CountryMockInterceptor()
        val apolloClient = ApolloClient.Builder()
            .serverUrl("https://countries.trevorblades.com/")
            .addHttpInterceptor(LoggingInterceptor { println(it) })
            .addHttpInterceptor(httpInterceptor)
            .build()
        systemUnderTest = GetCountry(
            apolloClient,
        )
    }

    @Test
    fun `Return null on API server Error`() = runTest {
        httpInterceptor.response = CountryMockInterceptor.Response.SERVER_ERROR

        val result = systemUnderTest.execute(CODE)

        result.shouldBeNull()
    }

    @Test
    fun `Return null on Network Error`() = runTest {
        httpInterceptor.response = CountryMockInterceptor.Response.NETWORK_ERROR

        val result = systemUnderTest.execute(CODE)

        result.shouldBeNull()
    }

    @Test
    fun `Return null on Success but no value`() = runTest {
        httpInterceptor.response = CountryMockInterceptor.Response.SUCCESS_NULL_VALUE

        val result = systemUnderTest.execute(CODE)

        result.shouldBeNull()
    }
}

class CountryMockInterceptor : HttpInterceptor {

    enum class Response {
        SUCCESS,
        SUCCESS_NULL_VALUE,
        SERVER_ERROR,
        NETWORK_ERROR,
    }

    var response = Response.SUCCESS

    override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
        val operation = request.headers.first { it.name == "X-APOLLO-OPERATION-NAME" }.value

        return if (operation == "Country") {
            when (response) {
                Response.SUCCESS -> {
                    Responses.SUCCESS.toHttpResponse()
                }

                Response.SUCCESS_NULL_VALUE -> Responses.SUCCESS_NULL_SCHEMA.toHttpResponse()
                Response.SERVER_ERROR -> Responses.SERVER_ERROR.toHttpResponse()
                Response.NETWORK_ERROR -> HttpResponse.Builder(statusCode = 500).build()
            }
        } else {
            chain.proceed(request)
        }
    }

    private fun String.toHttpResponse(): HttpResponse {
        return HttpResponse.Builder(statusCode = 200)
            .body(this.encodeUtf8())
            .build()
    }
}

object Responses {

    val SUCCESS =
        CountryQuery.Data(
            CountryQuery.CountrySchema(
                name = "United Kingdom",
                languagesSchemas = listOf(
                    CountryQuery.LanguagesSchema(
                        __typename = "Language",
                        languageFragment = LanguageFragment(
                            name = "English"
                        )
                    )
                ),
            )
        ).toDataJson()

    const val SUCCESS_NULL_SCHEMA = """{"data":{"countrySchema":null}}"""

    const val SERVER_ERROR =
        """{"errors":[{"message":"Cannot query field \"capitals\" on type \"Country\". Did you mean \"capital\"?","extensions":{"code":"GRAPHQL_VALIDATION_FAILED"}}]}"""

    private fun Operation.Data.toDataJson() =
        """{"data":${this.toJsonString()}}"""
}
