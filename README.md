# Iqra Android Client

Iqra is a tool meant to allow Muslims to search the Quran using speech recognition. This repo contains the code for the [Android client](https://play.google.com/store/apps/details?id=com.mmmoussa.iqra). There are also repos for the [website](https://github.com/Crescent-Labs/iqra-web) and [iOS client](https://github.com/Crescent-Labs/iqra-ios).

### Setup

In order to run the app, open the project in Android Studio. Before performing a gradle sync, a few steps need to be taken. First, rename the `sample-google-services.json` file in the `app` directory to `google-services.json`. Next, create a file named `gradle.properties` in the project root directory. Copy the following line into the file:

```
IQRA_API_KEY=yourAPIKey
```

You'll need to replace the `IQRA_API_KEY` variable's value with a valid key. You can obtain a key for testing by sending an email to info@iqraapp.com with subject line "api key request". Please allow some time for your request to be processed and an api key sent back.

Once you've obtained an api key and placed it into `gradle.properties`, you can run a gradle sync and then run the app.

### Contributing

Contributions of patches and comments are welcome. If you'd like to contribute a new feature, create an issue for it first and only open a pull request once the feature request has been approved by [the project owner](https://github.com/mmmoussa).

### Installing APK directly

If you want to install the latest release of Iqra but do not have access to the Play store, the APK is available in this repo as `app-release.apk`.
