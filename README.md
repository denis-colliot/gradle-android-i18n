[![CircleCI](https://circleci.com/gh/denis-colliot/gradle-android-i18n.svg?style=svg)](https://circleci.com/gh/denis-colliot/gradle-android-i18n)

# Introduction

The **androidI18n** gradle plugin automaticallly generates `strings.xml`
resources files from a given translations (i18n) source file.

The plugin provides the following gradle tasks:

**`androidI18nImport`**: imports the i18n translations from the source file and generates the android resources for each supported locale :
- `values/strings.xml`
- `values-fr/strings.xml`
- `values-es/strings.xml`

**`androidI18nExport`**: *(WIP: coming soon)*


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


# Supported sources

### Supported URI 
- Samba URI : `smb://<domain>;<login>:<password>@<host>/<path_to_file>`
- Windows UNC : `\\<host>\<path_to_file>`

### Supported file types
For the moment, the plugin only supports `.xls` source file type. It must respect following structure:

| key | fr | en | es |
|-----|----|----|----|
| home_text_1 | Bonjour le monde ! | Hello world! | ¡Hola Mundo! |
| home_text_2 | Avec un argument : # | With an argument: # | Con un argumento : # |
| plurals_key:one | Voiture | Car | Coche |
| plurals_key:other | Voitures | Cars | Coches |