package com.github.gradle.android.i18n.import

/**
 * Import task configuration.
 */
data class ImportConfig(

        /**
         * Default android locale.
         */
        val defaultLocale: String,

        /**
         * Import all sheets when using `xls`/`xlsx` source file.
         *
         * Default is `false`.
         */
        val allSheets: Boolean,

        /**
         * Sheet names filter regex when using `xls`/`xlsx` source file and [allSheets] is set to `true`.
         *
         * Default is `.*` to read all sheets.
         */
        val sheetNameRegex: Regex

)