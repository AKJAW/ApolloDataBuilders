package com.akjaw.apollo.builders.domain.model

import com.akjaw.apollo.builders.generated.CountryQuery
import com.akjaw.apollo.builders.generated.type.buildCountry
import com.akjaw.apollo.builders.generated.type.buildLanguage
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test

internal class CountryConverterTest {

    private lateinit var systemUnderTest: CountryConverter

    @Before
    fun setUp() {
        systemUnderTest = CountryConverter()
    }

// TODO resolver test???

    @Test
    fun `The country name is correctly converted`() {
        val schema = CountryQuery.Data {
            country = buildCountry {
                name = "Poland"
            }
        }.country!!

        val result = systemUnderTest.convert(schema)

        result.name shouldBe "Poland"
    }

    @Test
    fun `The languages names is correctly converted`() {
        val schema = CountryQuery.Data {
            country = buildCountry {
                languages = listOf(
                    buildLanguage {
                        name = "English"
                    },
                    buildLanguage {
                        name = "Polish"
                    },
                )
            }
        }.country!!

        val result = systemUnderTest.convert(schema)

        assertSoftly(result.language){
            get(0).name shouldBe "English"
            get(1).name shouldBe "Polish"
        }
    }

    @Test
    fun `Languages with null names are ignored`() {
        val schema = CountryQuery.Data {
            country = buildCountry {
                languages = listOf(
                    buildLanguage {
                        name = null
                    },
                )
            }
        }.country!!

        val result = systemUnderTest.convert(schema)

        result.language.shouldBeEmpty()
    }
}
