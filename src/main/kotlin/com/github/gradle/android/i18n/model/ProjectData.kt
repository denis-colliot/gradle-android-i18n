package com.github.gradle.android.i18n.model

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
     * | \_key1: value1-overriden
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
     *   \_key1: value1-overriden
     *   \_key2: value2
     * ```
     */
    fun deduplicated(sourceModuleName: String): ProjectData {
        TODO("Not yet implemented")
    }
}
