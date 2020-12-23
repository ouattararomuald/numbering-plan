[![](https://img.shields.io/badge/code--style-square-green.svg)](https://github.com/square/java-code-styles)
[![](https://img.shields.io/maven-central/v/com.ouattararomuald/numbering.svg)](https://search.maven.org/search?q=g:com.ouattararomuald%20a:numbering)
[![](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.ouattararomuald/numbering.svg)](https://oss.sonatype.org/content/repositories/snapshots/)

# numbering-plan

This library helps you migrate exiting phone numbers format to new phone number formats.

## Usage

First you start by defining the migration rules for you country

```kotlin
val ivoryCoastPlanFactory: CountryPlan = CountryPlan.Builder()
    .setOldPhoneNumberSize(8)
    .setInternationalCallingCode("225")
    .setDigitMapperPosition(Position.START) // Position.END
    .setMigrationType(MigrationType.PREFIX) // MigrationType.POSTFIX
    .setPrefixesMapper(
      mapOf(
        "07" to "07", // Orange
        "08" to "07", // eg: 08 XX XX XX => 07 08 XX XX XX (if MigrationType.prefix is used) => 08 XX XX XX 07 (if MigrationType.postfix is used)
        "09" to "07",
        "04" to "05", // MTN
        "05" to "05",
        "06" to "05",
        "01" to "01", // MOOV
        "02" to "01",
        "03" to "01"
      )
    ).build()
```

Then you pass those rules to `NumberingPlan` and you can start the migration:

```kotlin
val ivoryCoastPlanFactory: CountryPlan = ...
val numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
val newPhoneNumbers = numberingPlan.migrate(mapOf(
  "userId-1" to "08060709",
  "userId-2" to "06060709",
  "userId-3" to "03060701",
  "userId-4" to " 03 060 701 ",
  "userId-5" to " 03-060-701",
  "userId-6" to "zezae/03-060-701",
  "userId-7" to ")'.03-060-701"
))
```

After the migration `newPhoneNumbers` will be equal to:

```kotlin
mapOf(
  "userId-1" to "002250708060709",
  "userId-2" to "002250506060709",
  "userId-3" to "002250103060701",
  "userId-4" to "002250103060701",
  "userId-5" to "002250103060701"
)
```

Invalid phone numbers are removed.

## Download

Download the [latest JAR](https://search.maven.org/search?q=g:com.ouattararomuald%20AND%20a:numbering) or grab via Gradle:

```groovy
implementation 'com.ouattararomuald:numbering:0.1'
```

or Maven:

```xml
<dependency>
  <groupId>com.ouattararomuald</groupId>
  <artifactId>numbering</artifactId>
  <version>0.1</version>
</dependency>
```

Snapshots of the development version are available in [Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/).

## Known limitations

- [x] Migrates only valid phone numbers.
- [x] Can add new digits before or after the old phone number. We decided to not handle in between insertions.
- [x] Can only look for digits to replace at the start or the end of old phone numbers. We think this shouldn’t be a problem (generally) but as of now we decided to not handle such cases.
- [x] It is synchronous. This is a choice that will let you pick the any library you want to handle async tasks.

## Contributing

Contributions you say? Yes please!

**Bug report?**

If at all possible, please attach a minimal sample project or code which reproduces the bug.
Screenshots are also a huge help if the problem is visual.

**Send a pull request!**

If you're fixing a bug, please add a failing test or code that can reproduce the issue.

## License

```
Copyright 2020 Ouattara Gninlikpoho Romuald

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
