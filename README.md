[![CircleCI](https://circleci.com/gh/denis-colliot/gradle-android-i18n.svg?style=svg)](https://circleci.com/gh/denis-colliot/gradle-android-i18n)

# Introduction

The **androidI18n** gradle plugin automaticallly generates `strings.xml`
resources files from a given translations (i18n) source file.

The plugin provides the following gradle tasks:

- **`androidI18nImport`**: imports the i18n translations from the source file and generates the android resources for each supported locale :
  - `values/strings.xml`
  - `values-fr/strings.xml`
  - `values-es/strings.xml`

- **`androidI18nExport`**: *(WIP: coming soon)*


# Installation

Follow the instructions described [here](https://plugins.gradle.org/plugin/com.github.gradle.android-i18n).


# Configuration

Simply add this configuration block to your `build.gradle` file:
```
androidI18n {

   // Source file containing i18n translations.
   sourceFile = '/path/to/source/file.xls'
   
   // Default android locale corresponding to 'values' directory.
   defaultLocale = 'en'
}
```

An empty (or blank) `sourceFile` simply disabled plugin execution (convenient for Continuous Integration).


# Supported sources

## Supported URI

### Samba URI
Pattern: `smb://<domain>;<login>:<password>@<host>/<path_to_file>`

The plugin uses [**jcifs**](https://jcifs.samba.org/) to establish a connection with the remote samba URI.

[Jcifs client properties](https://jcifs.samba.org/src/docs/api/overview-summary.html#scp) can be provided
through gradle project properties using prefix `androidI18n.*`:
```
androidI18n.jcifs.smb.client.dfs.disabled=true
androidI18n.jcifs.smb.client.responseTimeout=5000
```

### Windows UNC
Pattern: `\\<host>\<path_to_file>`


## Supported file types
For the moment, the plugin only supports `.xls` and `.xlsx` source file types. Files must respect following structure:

| key | fr | en | es |
|-----|----|----|----|
| home_text_1 | Bonjour le monde ! | Hello world! | ¡Hola Mundo! |
| home_text_2 | Avec un argument : # | With an argument: # | Con un argumento : # |
| plurals_key:one | Voiture | Car | Coche |
| plurals_key:other | Voitures | Cars | Coches |


# Publish plugin to the Gradle Plugin Portal

## Official guide

https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/

## TL;DR

The following configuration should be set in your `~/.gradle/gradle.properties` file:

```shell script
gradle.publish.key=<api_key>
gradle.publish.secret=<secret>
```

Then simply run following command:

```shell script
$ ./gradlew publishPlugins
```