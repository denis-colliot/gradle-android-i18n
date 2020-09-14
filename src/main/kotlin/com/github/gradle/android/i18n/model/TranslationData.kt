package com.github.gradle.android.i18n.model

/**
 * This class aggregates all the internationalized labels corresponding to a language.
 * @param locale the name of the language eg `fr` or `en`
 * @param stringDataList a list of translated labels. All of these are in the considered language:
 * if [locale] is `fr`, all [StringData]s are in french.
 */
class TranslationData(val locale: String, val stringDataList: List<StringData>)
