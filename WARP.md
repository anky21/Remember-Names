# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project overview

Android app ("Remember Names") built with Gradle as a single `app` module. The app lets users store and organize people as "connections" with photos, notes, and tags.

Key technologies:
- Java Android app with some Kotlin stdlib dependency (no Kotlin source yet)
- Gradle Android plugin 8.x; compile/target SDK 34, min SDK 21
- Dagger 2 for dependency injection (`root` package)
- RxJava 2 + RxAndroid (`Single`-based async flows)
- Schematic for generating a `ContentProvider` and schema (`data.source.local`)
- ButterKnife for view binding, EventBus for UI events, Glide for image loading
- Firebase Analytics, Crashlytics, FCM, and Google Mobile Ads

## Common Gradle commands (run from repo root)

Build / assemble:
- Full build: `./gradlew build`
- Clean build: `./gradlew clean build`
- Debug APK only: `./gradlew :app:assembleDebug`
- Install debug on connected device/emulator: `./gradlew :app:installDebug`

Testing:
- Run JVM unit tests: `./gradlew :app:testDebugUnitTest`
- Run a single unit test class: `./gradlew :app:testDebugUnitTest --tests 'me.anky.connectid.ExampleUnitTest'`
  - Replace the fully qualified class name as needed for additional tests.
- Instrumented tests (if added under `app/src/androidTest`): `./gradlew :app:connectedDebugAndroidTest`

Lint / quality:
- Android lint for all variants: `./gradlew :app:lint`
- Lint debug only: `./gradlew :app:lintDebug`

Signing:
- Release builds use `keystore.properties` loaded in `app/build.gradle`. To assemble a signed release APK, ensure that file exists and then run: `./gradlew :app:assembleRelease`.

## High-level architecture

### Modules and packages

- Root project
  - `build.gradle`: Gradle settings and plugin versions (Android, Kotlin, Google services, Crashlytics).
  - `settings.gradle`: includes only the `app` module.
- `app` module
  - `app/build.gradle`: Android config (SDK versions, signing, lint, dependencies).
  - `src/main/AndroidManifest.xml`: defines `ConnectidApplication`, main activities, Firebase/Ads metadata, and Schematic-generated provider.
  - `src/main/res`: standard Android resources (layouts, drawables, menus, strings, styles, etc.).
  - `src/main/java/me/anky/connectid` and subpackages constitute the main app code.

### Application and dependency injection (Dagger 2)

- `root/ConnectidApplication` extends `Application`:
  - Initializes `FirebaseAnalytics` and a Dagger `ApplicationComponent` in `onCreate()`.
  - Sets a permissive `StrictMode` VM policy (for file URIs when exporting CSVs).
  - Provides global access to `ApplicationComponent` and `FirebaseAnalytics` via `getAppInstance()` and `getAnalyticsInstance()`.
- `root/ApplicationModule` is the central DI module:
  - Provides a singleton `Context` and `SharedPreferences` (namespaced as `"shared-prefs"`).
  - Constructs a singleton `ConnectionsLocalRepository` and exposes it as the `ConnectionsDataSource` implementation.
  - Provides presenter instances for each activity (`ConnectionsActivity`, `DetailsActivity`, `EditActivity`, `EditTagActivity`, `TagsActivity`, `SelectedConnectionsActivity`) using their `*MVP.Presenter` interfaces.
- `root/ApplicationComponent` wires it together:
  - Annotated with `@Singleton` and `@Component(modules = { ApplicationModule.class })`.
  - Exposes `inject(...)` methods for activities, presenters, and utilities that require DI.
  - Exposes `SharedPrefsHelper` so it can be injected where needed.

**Implication for new code:** new screens that follow the existing pattern should define their own `ActivityMVP` interfaces and presenters, then add `@Provides` methods and `inject(...)` targets in `ApplicationModule` and `ApplicationComponent` respectively.

### Presentation layer: MVP pattern per screen

Each major screen follows a lightweight MVP structure:
- `connections/ConnectionsActivityMVP`, `details/DetailsActivityMVP`, `edit/EditActivityMVP`, `editTag/EditTagActivityMVP`, `tags/TagsActivityMVP`, `selectedConnections/SelectedConnectionsActivityMVP` define `View` and `Presenter` interfaces.
- `*Activity` classes:
  - Extend `AppCompatActivity`.
  - Implement their respective `View` interfaces.
  - Are injected with their presenter via Dagger (`@Inject` field).
  - Bind views using ButterKnife (`@BindView`, `@OnClick`).
  - Handle Android lifecycle, navigation, menu actions, and view-only logic.
- `*ActivityPresenter` classes:
  - Implement the corresponding presenter interfaces.
  - Depend on `ConnectionsDataSource` for data operations.
  - Use RxJava (`Single`, `CompositeDisposable`) to perform async work on `Schedulers.io()` and observe on `AndroidSchedulers.mainThread()`.
  - Call back into the `View` via the typed interface methods (e.g., `displayConnections`, `displayNoConnections`, `displayError`).
  - Use `Utilities.logFirebaseError(...)` for structured Firebase error logging.

`ConnectionsActivity` specifically:
- Acts as the main entry point (`LAUNCHER` in `AndroidManifest.xml`).
- Injects `SharedPrefsHelper` and `ConnectionsActivityPresenter`.
- Configures RecyclerView + adapter, navigation drawer, and FAB.
- Persists sort order in `SharedPrefsHelper` (`Utilities.SORTBY`) and delegates to the presenter for reloading data.
- Integrates AdMob banner ads and a "rate this app" dialog using the `RateMeDialog` library.

`EditActivity` and tagging flows:
- `EditActivity` handles both creating and editing a connection, and integrates:
  - Glide for image loading.
  - A `PickerFragment` dialog (`edit/PickerFragment`) for choosing a picture.
  - `Utilities.displayTags(...)` to render tag chips.
  - EventBus (`SetToUpdateTagTable`) for updating tag mappings when a connection changes.
  - Interstitial ads shown every Nth save via `SharedPreferences` counters.
- `EditTagActivity`/`TagsActivity` let users manage tags and view connections by tag, built on the same MVP pattern.

### Data layer: models, repository, and content provider

**Models (`data` package):**
- `ConnectidConnection` and `ConnectionTag` are plain Java objects with fields matching the DB schema (e.g., connection names, image name, meet venue, appearance, description, tags, and tag-to-connection ID associations).
- `ConnectionsDataSource` defines the data contract used by presenters:
  - CRUD for `ConnectidConnection`.
  - CRUD for `ConnectionTag`.
  - Batch tag insertion helpers.
  - Read operations return RxJava `Single<T>`.

**Local repository (`data.source.local`):**
- `ConnectionsLocalRepository` implements `ConnectionsDataSource` using the app's `ContentProvider`:
  - Reads and writes via `context.getContentResolver()`.
  - Uses `ConnectidProvider.Connections` and `ConnectidProvider.Tags` URIs.
  - Returns `Single` values via `Single.fromCallable(...)` to integrate with the Rx-based presenters.
  - Relies on `Utilities.SORT_ORDER_OPTIONS` for ordering connection queries based on the user's selected sort option.

**Schema and provider (Schematic):**
- `ConnectidDatabase` (annotated with `@Database`) defines:
  - `CONNECTIONS` and `TAGS` tables via `ConnectidColumns` and `TagsColumns`.
  - A schema version and an `@OnUpgrade` migration path that adds the tags table and corresponding column to the connections table.
- `ConnectidProvider` (annotated `@ContentProvider`) defines:
  - `content://me.anky.connectid.data.source.local.ConnectidProvider` as the authority (also referenced in `AndroidManifest.xml`).
  - `Connections` and `Tags` static inner classes with `CONTENT_URI` and `withId(...)` helpers.

**Shared preferences (`SharedPrefsHelper`):**
- Thin wrapper with typed `put`/`get` methods for `String`, `int`, `float`, `boolean`, injected via Dagger.
- Used for persisting sort order and various one-off flags (e.g., whether the default profile image has been saved).

### Utilities and cross-cutting concerns

- `Utilities` centralizes common behavior:
  - Image save/load and resizing (`loadImageFromStorage`, `saveToInternalStorage`, `resizeBitmap`).
  - Tag layout rendering into a `RelativeLayout` (`displayTags(...)`), using dynamic `TextView` creation and line-wrapping based on available width.
  - Keyboard show/hide helpers.
  - Sort order constants `SORTBY` and `SORT_ORDER_OPTIONS` used by both the UI and `ConnectionsLocalRepository`.
  - Firebase Analytics helpers (`logFirebaseError`, `logFirebaseEvents`, `logFirebaseEventWithNoParams`) which pull `FirebaseAnalytics` from `ConnectidApplication`.
- `Utils` subpackage:
  - `DialogUtils`: helpers for common confirmation dialogs (e.g., export CSV).
  - `FileUtils` + `SqliteExporter`: support exporting the SQLite DB as CSV and sharing it (used from `ConnectionsActivity`).
- `service/FcmService` and `service/MyNotificationManager` handle Firebase Cloud Messaging and notification presentation.

### External services and configuration

- Firebase/Google services:
  - `google-services.json` is checked into `app/` and applied via the `com.google.gms.google-services` plugin.
  - Firebase Analytics, Messaging, Invites, and Crashlytics are configured through Gradle dependencies and manifest metadata.
- Google Mobile Ads:
  - Banner ads are integrated in `ConnectionsActivity` via `AdView`.
  - Interstitial ads are shown from `EditActivity` after a certain number of "save" actions.

### Testing

- Example unit test under `app/src/test/java/me/anky/connectid/ExampleUnitTest.java` verifies basic JUnit wiring.
- Standard pattern for additional unit tests:
  - Place under `app/src/test/java/...`.
  - Run with `./gradlew :app:testDebugUnitTest` or filter via `--tests` as noted above.

This summary should give future Warp instances enough context to navigate the project structure, understand the MVP + Dagger + RxJava architecture, and run the essential Gradle commands for build, test, and lint workflows.
