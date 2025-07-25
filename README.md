# Kotlin Num2Words

[![Version](https://img.shields.io/static/v1?label=GitHubPackages&message=0.1.0-SNAPSHOT&color=blue&logo=github)](https://github.com/typosbro/num2words/packages)

A Kotlin library to convert numbers (cardinals, ordinals, currency, years) into their word representations. This project is a **Kotlin adaptation inspired by the functionality of the Python `num2words` library.**

**⚠️ Disclaimer: This is my first public library and is currently UNSTABLE and under active development. Expect API changes and potential bugs. Use in production with caution!**

## Language Support

*   ✅ **English (en)**: Currently, only English is supported.

## Features (Planned & In-Progress)

*   Convert cardinal numbers to words (e.g., 123 -> "one hundred and twenty-three")
*   Convert ordinal numbers to words (e.g., 3 -> "third")
*   Convert numbers to ordinal numeric strings (e.g., 3 -> "3rd")
*   Convert numbers to year representations (e.g., 1995 -> "nineteen ninety-five")
*   Convert numbers to currency representations (e.g., 123.45 USD -> "one hundred and twenty-three dollars and forty-five cents")
*   Initial focus on JVM, with potential for future Kotlin Multiplatform support (Android, iOS, JS).

## Current Status

*   Basic conversion for English cardinals, ordinals, years, and currency is implemented for JVM.
*   Published to GitHub Packages.
*   **API is not stable.**
*   Testing is ongoing.

## Installation / Adding to your project

This library is currently published to GitHub Packages.

To add it to your Gradle project (`build.gradle.kts`):

1.  **Add the GitHub Packages repository:**

    In your project's `settings.gradle.kts`:
    ```kotlin
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            mavenCentral()
            maven {
                name = "GitHubPackagesTyposbroNum2words"
                url = uri("https://maven.pkg.github.com/typosbro/num2words")
            }
        }
    }
    ```

2.  **Add the dependency:**

    In your module's `build.gradle.kts`:
    ```kotlin
    dependencies {
        implementation("io.github.typosbro:num2words:0.1.0-SNAPSHOT") // Replace with the desired version
        // ... other dependencies
    }
    ```
    *(Check the [Packages section of this repository](https://github.com/typosbro/num2words/packages) for the latest version.)*

## Basic Usage Example

```kotlin
// Ensure you have imported the necessary class from the library
import io.github.typosbro.En // Assuming En is your English converter class

fun main() {
    val converter = En()

    println("Cardinal:")
    println("0: " + converter.toCardinal(0))
    println("123: " + converter.toCardinal(123))
    println("-42: " + converter.toCardinal(-42))

    println("\nOrdinal:")
    println("3: " + converter.toOrdinal(3))
    println("21: " + converter.toOrdinal(21))

    println("\nCurrency (USD):")
    println("150.75: " + converter.toCurrency(150.75, currency = "USD"))

    println("\nYear:")
    println("2024: " + converter.toYear(2024))
}
```

## Contributing

Contributions are very welcome! As this is an early-stage project and my first library, I'd appreciate any help, feedback, or suggestions.

Here are some ways you can contribute:

* Reporting Bugs: If you find a bug, please open an issue. Include steps to reproduce it.

* Suggesting Features or Enhancements: Have an idea? Let me know by opening an issue.

* Improving Documentation: If something in the README or code comments is unclear, feel free to suggest improvements.

* Adding Test Cases: More tests are always good!

## Code Contributions:

* Fork the repository.

* Create a new branch for your feature or bug fix.

* Make your changes.

* Add tests for your changes.

* Ensure the existing tests pass.

* Submit a pull request.

Please feel free to open an issue to discuss any changes you'd like to make before starting significant work.

## License

This project is currently not licensed. A license (likely MIT or Apache 2.0) will be added soon.

Thank you for checking out Kotlin Num2Words!

**Key changes:**

*   Added "This project is a **Kotlin adaptation inspired by the functionality of the Python `num2words` library.**" to the introduction.
*   Added a new "Language Support" section highlighting that only English is currently supported.
*   Mentioned adapting rules from the Python library in the "Future Ideas" and "Contributing" sections for new languages.
