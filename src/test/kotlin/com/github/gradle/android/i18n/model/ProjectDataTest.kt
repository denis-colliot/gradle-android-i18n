package com.github.gradle.android.i18n.model

import org.junit.Assert.*
import org.junit.Test

class ProjectDataTest {

    @Test
    fun `should deduplicate by overriding values in other modules`() {

        val givenProjectData = ProjectData(
            listOf(
                ModuleData(
                    "app",
                    listOf(
                        TranslationData(
                            "fr",
                            listOf(
                                StringData("key1", "valeur1-surchargée"),
                                StringData("key3", "valeur3")
                            )
                        ),
                        TranslationData(
                            "en",
                            listOf(
                                StringData("key1", "value1-overriden"),
                                StringData("key3", "value3")
                            )
                        )
                    )
                ),
                ModuleData(
                    "feature1",
                    listOf(
                        TranslationData(
                            "fr",
                            listOf(
                                StringData("key1", "valeur1-base"),
                                StringData("key2", "valeur2")
                            )
                        ),
                        TranslationData(
                            "en",
                            listOf(
                                StringData("key1", "value1-base"),
                                StringData("key2", "value2")
                            )
                        )
                    )
                )
            )
        )

        val deduplicated = givenProjectData.deduplicated("app")

        assertEquals(
            ProjectData(
                listOf(
                    ModuleData(
                        "app",
                        listOf(
                            TranslationData(
                                "fr",
                                listOf(
                                    StringData("key1", "valeur1-surchargée"), // TODO: Source key removal
                                    StringData("key3", "valeur3")
                                )
                            ),
                            TranslationData(
                                "en",
                                listOf(
                                    StringData("key1", "value1-overriden"), // TODO: Source key removal
                                    StringData("key3", "value3")
                                )
                            )
                        )
                    ),
                    ModuleData(
                        "feature1",
                        listOf(
                            TranslationData(
                                "fr",
                                listOf(
                                    StringData("key1", "valeur1-surchargée"),
                                    StringData("key2", "valeur2")
                                )
                            ),
                            TranslationData(
                                "en",
                                listOf(
                                    StringData("key1", "value1-overriden"),
                                    StringData("key2", "value2")
                                )
                            )
                        )
                    )
                )
            ),
            deduplicated
        )
    }
}