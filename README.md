# Smart Pantry Manager

Java Android app for recording pantry ingredients and suggesting only recipes for which every required ingredient is available in sufficient quantity. Includes 18 seeded recipes, five Activities, a custom ListView adapter and persistent pantry CRUD.

## Database choice

SQLite via SQLiteOpenHelper keeps the pantry on the device, works offline, requires no user account and suits this small relational dataset. `recipe` has many `recipe_ingredient` rows. Pantry rows are compared with recipe requirements through normalized names and compatible units. Settings use SharedPreferences.

## Build and run

Requirements: JDK 17, Android SDK Platform 35, Android build tools and Gradle 8.9. Minimum device API is 23. A Gradle wrapper is not bundled.

1. Download the repository ZIP and extract it, or clone the repository.
2. Open the project root in Android Studio. Configure JDK 17 and an installed Gradle 8.9 distribution, then sync.
3. Select an emulator or USB-debugging-enabled Android device and run the `app` configuration.
4. Alternatively, with the SDK path configured, run `gradle assembleDebug lintDebug`. Install `app/build/outputs/apk/debug/app-debug.apk` using `adb install -r`.

## Automated verification

The GitHub Actions workflow builds and lints the app and runs UI checks on an API 35 emulator. The first successful verified run was commit `38c8019` on 28 September 2026, with 0 lint errors and 7 warnings. Its artifact contains the debug APK, lint reports and actual screenshots.

- `python3 test_matching.py`: requires JDK 17 and Python 3, but no Android SDK. Compiles the production Matcher with the actual data-holder definitions extracted from PantryDb. Checks each of the 18 recipes with exact quantities, an empty pantry, each required ingredient removed, and each quantity halved. Also covers case/whitespace/plurals, duplicate rows, kg/g, l/ml and incompatible dimensions. This isolates matching; it does not exercise SQLite.
- `python3 ci_smoke.py`: requires a running emulator, adb and the built APK. **Clears this app's test data** before checking CRUD, matching, quantity validation, details, restart persistence and settings. Screenshots and results go to `evidence/`.

## Demonstration sequence

1. Add bread 2 piece. Tomato Toast must not appear.
2. Add tomatoes 1 piece. Tomato Toast appears; open its full details.
3. Edit tomatoes to 0.5 piece. Tomato Toast disappears.
4. Restore tomatoes to 1 piece, close and reopen the app, then confirm the entries remain.
5. Delete tomatoes and confirm the suggestion disappears.
6. Add rice 0.05 kg and tomato 1 piece. Tomato Rice remains hidden. Change rice to 0.08 kg and it appears.
7. Show zero-quantity validation and the persisted settings toggle.

## Design and limitations

`PantryActivity` refreshes its database-backed list in `onResume`. `EditIngredientActivity` receives an item ID through an Intent. `SuggestedActivity` recalculates eligible recipes and passes a recipe ID to `RecipeDetailActivity`. All main navigation buttons also link to `SettingsActivity`.

Matching converts mass to grams and volume to millilitres, and sums compatible pantry rows. Every recipe requirement must be satisfied. Simple plural rules do not support every irregular word or synonym. Expiry dates support an in-app reminder but do not automatically remove expired items from matching. Recipes are educational examples; preparation methods and food suitability require user judgment. No mapping, GPS, payment or network features are used.

## Development record

Initial source and emulator workflow were uploaded on 28 September 2026. Subsequent work added recipe regression coverage, made it a CI gate and corrected this setup/verification documentation. Commit dates reflect the actual work and have not been backdated. The short history should not be represented as a longer development period.
