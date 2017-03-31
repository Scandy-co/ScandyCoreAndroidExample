# ScandyCoreAndroidExample
The `ScandyCoreAndroidExample` shows how you can use the magic that is Scandy Core in an Android Studio Project.

This sample uses ScandyCore to download a mesh and present it the viewer.

# Setup
## Step 1: Install Android Studio

* install [Android Studio](https://developer.android.com/studio/index.html)
  * if you're on Mac and have [`brew`](http://brew.sh/):

        ```bash
        brew cask install android-studio
        ```
* Open Android Studio

## Step 2: Android SDK

Make sure your Android Studio has all the necesary Android SDK dependencies installed.

\* = *Optional dependency*

In Android Studio go to `Tools -> Android -> SDK Manager`

From `SDK Platforms` tab

* *Android 5.0**
* *Android 5.1**
* *Android 6.0**
* Android 7.0
* Android 7.1.1

From `SDK Tools` tab

* Android SDK Build-Tools
* *CMake**
* LLDB
* Android SDK Platform-Tools #.#.# *(versions change)*
* Android SDK Tools #.#.# *(versions change)*
* Android Support Library, rev #.#.# *(versions change)*
* *NDK**
* Support Repository
  * ConstraintLayout for Android
  * Solver for ConstraintLayout
  * Android Support Repository
  * Google Repository




## Step 3: Add ScandyCoreAndroid as a dependency
Largely pulled this how from [Android Studio docs](https://developer.android.com/studio/projects/android-library.html#AddDependency)

* Open **AndroidFragmentDemo**
* Go to: `File -> New -> New Module`
* Select **Import .JAR/.AAR Package**, then click **Next**
* Set:
  * `File name:` Use the `...` and navigate to where you downloaded `ScandyCoreAndroid.aar` and select it.
  * `Subproject name:` **scandycoreandroid**
* Open `settings.gradle` and make sure it looks like this:

    ```gradle
    include ':app', ':scandycoreandroid'
    ```
* Open `app/build.gradle`
  * Make sure it has this in it towards the bottom:

        ```gradle
        dependencies {
            // All your other dependencies up here...
            compile project(":scandycoreandroid")
        }
        ```
  * `ScandyCoreAndroid` currently only supports `armeabi-v7a` achitecture. So make sure `defaultConfig` has this in it too:

        ```gradle
        defaultConfig {
            // All the other defaultConfig stuff...
            ndk {
                  abiFilters "armeabi-v7a"
                }
        }
        ```
* Sync Project with Gradle Files

## Step 4: Add your license

Update the contents of `app/src/main/assets/scandycore_license.json` with your license JSON.

# Building, Running, and Installing

* Click the big green arrow!

Or from the command line (from this `dir`):

```bash
./gradlew assembleDebug
# assuming the above command was successful
adb install -r ./app/build/outputs/apk/app-debug.apk
```
