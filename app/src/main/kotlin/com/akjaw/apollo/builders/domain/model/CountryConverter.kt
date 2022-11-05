package com.akjaw.apollo.builders.domain.model

import com.akjaw.apollo.builders.generated.CountryQuery

class CountryConverter {

    fun convert(schema: CountryQuery.CountrySchema): Country {
        val language = schema.languagesSchema.mapNotNull { languageSchema ->
            languageSchema.languageFragment.name?.let { Language(it) }
        }
        return Country(schema.name, language)
    }
}
