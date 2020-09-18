package com.github.gradle.android.i18n.model

import com.github.gradle.android.i18n.import.cleanUpTranslatedText

/**
 * This class represents a Gradle project in the context of i18n import/export.
 * @param modules a representation of the submodules of the project
 */
data class ProjectData(val modules: List<ModuleData>) {

    /**
     * Copy this [ProjectData], removing duplicate keys between a given source module (aka main module) and the other modules in the project.
     *
     * For example if `projectData` represents a project with 2 modules:
     *
     * ```
     * projectData
     * \_app
     * | \_key1: value1-app
     * | \_key3: value3
     * \_feature1
     *   \_key1: value1-feature1
     *   \_key2: value2
     * ```
     *
     * Calling `projectData.deduplicated("app")` will return:
     *
     * ```
     * projectData
     * \_app
     * | \_key3: value3
     * \_feature1
     *   \_key1: value1-app
     *   \_key2: value2
     * ```
     */
    fun deduplicated(sourceModuleName: String): ProjectData {

        val (sourceModule, otherModules) = modules
            .partition { it.name == sourceModuleName }
            .let { (sourceModules, otherModules) -> sourceModules.first() to otherModules }

        val sourceModuleDeduplicated = sourceModule.withoutDuplicateKeys(otherModules)
        val otherModulesOverriden = otherModules.withOverridenValues(sourceModule)

        return ProjectData(listOf(sourceModuleDeduplicated) + otherModulesOverriden)
    }

    private fun ModuleData.withoutDuplicateKeys(
        otherModules: List<ModuleData>
    ): ModuleData = copy(translations = translations.map { sourceTranslationData ->
        sourceTranslationData.copy(
            stringDataList = sourceTranslationData.stringDataList
                .filter { sourceString ->
                    otherModules.none { otherModule ->
                        otherModule.translations
                            .find { it.locale == sourceTranslationData.locale }
                            ?.stringDataList
                            ?.any { it.name == sourceString.name } == true
                    }
                }.map { (key, text) ->
                    StringData(key, text?.cleanUpTranslatedText())
                })
    })

    private fun List<ModuleData>.withOverridenValues(
        sourceModule: ModuleData
    ): List<ModuleData> = this.map { otherModule ->
        otherModule.copy(translations = otherModule.translations.map { otherTranslation ->
            otherTranslation.copy(stringDataList = otherTranslation.stringDataList.map { string ->
                val text = sourceModule.translations
                    .find { it.locale == otherTranslation.locale }
                    ?.stringDataList
                    ?.find { it.name == string.name }
                    ?.text
                    ?: string.text
                string.copy(text = text?.cleanUpTranslatedText())
            })
        })
    }
}
