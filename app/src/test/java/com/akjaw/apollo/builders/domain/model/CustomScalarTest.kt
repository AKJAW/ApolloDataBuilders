package com.akjaw.apollo.builders.domain.model

import com.akjaw.apollo.builders.generated.CountryWithCustomScalarQuery
import com.akjaw.apollo.builders.generated.type.__Schema
import com.akjaw.apollo.builders.generated.type.buildCountry
import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.Test

class CustomScalarTest {

    @Test
    fun `Does not throw exception with resolver`() {
        shouldNotThrowAny {
            CountryWithCustomScalarQuery.Data(MyFakeResolver()) {
                country = buildCountry {
                    name = "Custom"
                }
            }
        }
    }

    @Test
    fun `Throws an exception without resolver`() {
        shouldThrow<IllegalStateException> {
            CountryWithCustomScalarQuery.Data {
                country = buildCountry {
                    name = "Custom"
                }
            }
        }
    }
}

class MyFakeResolver : FakeResolver {

    private val delegate = DefaultFakeResolver(__Schema.all)

    override fun resolveLeaf(context: FakeResolverContext): Any {
        return if (context.mergedField.type.leafType().name == "CustomScalar") {
            return ""
        } else {
            delegate.resolveLeaf(context)
        }
    }

    override fun resolveListSize(context: FakeResolverContext): Int {
        return delegate.resolveListSize(context)
    }

    override fun resolveMaybeNull(context: FakeResolverContext): Boolean {
        return delegate.resolveMaybeNull(context)
    }

    override fun resolveTypename(context: FakeResolverContext): String {
        return delegate.resolveTypename(context)
    }
}
