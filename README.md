[![CircleCI](https://circleci.com/gh/denis-colliot/gradle-android-i18n.svg?style=svg)](https://circleci.com/gh/denis-colliot/gradle-android-i18n)

# Introduction

The **androidI18n** gradle plugin automaticallly generates `strings.xml`
resources files from a given translations (i18n) source file.

The plugin provides the following gradle tasks:

- **`androidI18nImport`**: imports the i18n translations from the source file and generates the android resources for each supported locale:
  - `values/strings.xml`
  - `values-fr/strings.xml`
  - `values-es/strings.xml`

- **`androidI18nExport`**: export the android resources for all locales to a XLSX file in the `build` directory of the current module. The path of the generated XLSX file is displayed in the build output:

    ```
    ./gradlew app:androidI18nExport
    [...]
    > Task :app:androidI18nExport
    Resources were exported to:
    /path/to/app/build/i18n.xlsx
    [...]
    ```

- **`androidI18nDeduplicate`**: remove duplicate keys between a given source module and the other modules in the Gradle project.

# Installation

Follow the instructions described [here](https://plugins.gradle.org/plugin/com.github.gradle.android-i18n).


# Configuration

Simply add this configuration block to your `build.gradle` file:
```
androidI18n {

   // Source file containing i18n translations.
   sourceFile = '/path/to/source/file.xls'
   
   // Default android locale corresponding to 'values' directory.
   defaultLocale = 'fr'
}
```

An empty (or blank) `sourceFile` simply disabled plugin execution (convenient for Continuous Integration).

# Usage

## Single module Gradle projects

When applied to the configuration of a single module eg `app`, the plugin operates in *single module* mode.

In this mode, the **export task** `androidI18nExport` will create an Excel spreadsheet with one sheet named `android-i18n` that contains all translation keys and the corresponding texts for each language.

<table>

<tr>
<td colspan="3">
<pre>
$ ./gradlew app:androidI18nExport
> Task :app:androidI18nExport
Resources were exported to:
/path/to/project/app/build/i18n_2020-09-15_11-50-50.xlsx
BUILD SUCCESSFUL in 6s
</pre>
</td>
</tr>

<tr>
<td>
<pre>
project
└── app
    └── src
        └── main
            └── res
                ├── values
                │   └── strings.xml
                └── values-en
                    └── strings.xml
</pre>
</td>

<td>⇒</td>

<td>
<img src="README_files/single-xlsx.png" width="700px">
</td>

</tr>
</table>

The **import task** `androidI18nImport` will take an Excel spreadsheet and generate a single set of `strings.xml` files (one file by language) into the directory structure of the single module.

<table>

<tr>
<td colspan="3">
<pre>
$ gradlew app:androidI18nImport -PandroidI18n.sourceFile=/path/to/project/build/i18n_2020-09-15_11-50-50.xlsx
BUILD SUCCESSFUL in 28s
</pre>
</td>
</tr>

<tr>

<td>
<img src="README_files/single-xlsx.png" width="700px">
</td>

<td>⇒</td>

<td>
<pre>
project
└── app
    └── src
        └── main
            └── res
                ├── values
                │   └── strings.xml
                └── values-en
                    └── strings.xml
</pre>
</td>

</tr>
</table>

## Multi module gradle projects

When applied to a project with several child modules, the plugin operates in the **multi module** mode.

In this mode, the **export task** `androidI18nExport` will create an Excel spreadsheet with several sheets named after the child modules. Each sheet corresponds to a child module and contains all the translation keys of this module and the corresponding texts for each language.

<table>
<tr>
<td colspan="3">
<pre>
$ ./gradlew :androidI18nExport
> Task :androidI18nExport
Resources were exported to:
/path/to/project/build/i18n_2020-09-15_14-13-00.xlsx
BUILD SUCCESSFUL in 7s
</pre>
</td>
</tr>

<tr>
<td>
<pre>
project-multi
├── app
│   └── src
│       └── main
│           └── res
│               ├── values
│               │   └── strings.xml
│               └── values-en
│                   └── strings.xml
└── features
    └── feature1
        └── src
            └── main
                └── res
                    ├── values
                    │   └── feature1_strings.xml
                    └── values-en
                        └── feature1_strings.xml
</pre>
</td>

<td>⇒</td>

<td><img src="README_files/multi-xlsx.png" width="600px"></td>
</tr>
</table>

The **import task** `androidI18nImport` will take an Excel spreadsheet and generate several sets of `strings.xml` files (one set by module, then one file by language) into the directory structure of each child module.

<table>
<tr>
<td colspan="3">
<pre>
$ ./gradlew :androidI18nImport -PandroidI18n.sourceFile=/path/to/project/build/i18n_2020-09-15_14-13-00.xlsx
BUILD SUCCESSFUL in 4s
</pre>
</td>
</tr>

<tr>

<td><img src="README_files/multi-xlsx.png" width="600px"></td>

<td>⇒</td>

<td>
<pre>
project-multi
├── app
│   └── src
│       └── main
│           └── res
│               ├── values
│               │   └── strings.xml
│               └── values-en
│                   └── strings.xml
└── features
    └── feature1
        └── src
            └── main
                └── res
                    ├── values
                    │   └── feature1_strings.xml
                    └── values-en
                        └── feature1_strings.xml
</pre>
</td>

</tr>
</table>

The **deduplication task** will remove duplicate keys between a given source module and the other modules in the Gradle project. NB: This task operates on the `*strings.xml` files of the project.

For example if the project has 2 modules:
```
project
\_app
| \_key1: value1-app
| \_key3: value3
\_feature1
    \_key1: value1-feature1
    \_key2: value2
```

Here is how you call the task:

```bash
./gradlew androidI18nDeduplicate -PandroidI18n.deduplicateFrom=app
```

The task transforms the project to:

```
project
\_app
| \_key3: value3
\_feature1
    \_key1: value1-app
    \_key2: value2
```

The key `key1` was in both `app` and `feature1` and its value in `app` was `value1-app` ⇒ it gets moved into `feature1`, retaining the value it had in `app`.

But `key3` was only in `app` so it stays in `app`.

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

# Develop

To make changes to this plugin and test them in your project, you can use Gradle's composite build feature:

1. Clone this repository

2. Add this to `settings.gradle` in your project:

    ```groovy
    includeBuild('/path/to/gradle-android-i18n') {
        dependencySubstitution {
            substitute module("gradle.plugin.com.github.gradle:android-i18n") with project(':')
        }
    }
    ```
3. Now you can launch the `androidI18nImport` as usual from your project:

    ```shell script
    cd /path/to/your/project
    ./gradlew app:androidI18nImport
    ```
