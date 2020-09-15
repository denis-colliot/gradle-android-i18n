package com.github.gradle.android.i18n.model

/**
 * This class represents a Gradle project in the context of i18n import/export.
 * @param modules a representation of the submodules of the project
 */
data class ProjectData(val modules: List<ModuleData>)
