# Smart Pantry Manager

Java Android app that stores pantry quantities locally with SQLite and suggests only recipes whose every ingredient is available in sufficient quantity. It seeds 18 recipes on first database creation. SQLite is suitable for a small offline pantry without an account or network connection.

## Run

Open this folder in Android Studio with JDK 17 and Android SDK 35 installed. Allow Gradle sync, then run `app` on an emulator or Android device (API 23+). The Gradle wrapper is not bundled; Android Studio can use an installed Gradle 8.9 distribution. No location or network permissions are used.

## Main features

Pantry list with custom ListView adapter; add, edit, delete and validation; suggested recipes, details, settings. Names are lowercased, trimmed and simple plurals normalized. Grams/kilograms and millilitres/litres are converted to base quantities. Distinct dimensions are not equated. Ingredients with the same normalized name and compatible unit are summed. The simple plural rules are illustrative; they do not cover every English irregular plural or ingredient synonym.

## Verify manually before submission

1. Add bread 2 piece and tomato 1 piece: Tomato Toast appears. Delete tomato: it disappears.
2. Add rice 0.05 kg: Tomato Rice still needs 0.075 kg rice and a tomato, so it stays hidden. Change rice to 0.08 kg and add tomato 1 piece: it appears.
3. Add a negative or zero quantity, invalid date, or blank name: saving is rejected.
4. Close and reopen the app: pantry data remains.
5. Capture screenshots of every actual screen and function; narrate your own 5-7 minute video and record genuine development commits.

This starter has not been built or run in the supplied environment, which has no Android SDK. Verify the build, functions, and screens in Android Studio before using it as evidence.
