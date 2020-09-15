package com.github.gradle.android.i18n.model

/**
 * This class represents an Android Gradle module in the context of i18n import/export.
 *
 * @param name the module name eg `libraries/core-android`
 * @param translations the translations. 1 translation ⇔ 1 language
 */
data class ModuleData(
    val name: String,
    val translations: List<TranslationData>
) {
    companion object {
        const val DEFAULT_NAME: String = "android-i18n"
    }
}