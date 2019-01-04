# Iqra Android Client

Iqra is a tool meant to allow Muslims to search the Quran using speech recognition. This repo contains the code for the [Android client](https://play.google.com/store/apps/details?id=com.mmmoussa.iqra). There are also repos for the [website](https://github.com/Crescent-Labs/iqra-web) and [iOS client](https://github.com/Crescent-Labs/iqra-ios).

### Setup

In order to run the app, open the project in Android Studio. Before performing a gradle sync, a few steps need to be taken. First, rename the `sample-google-services.json` file in the `app` directory to `google-services.json`. Next, create a file named `gradle.properties` in the project root directory. Copy the following lines into the file:

```
IQRA_API_KEY=myAPIKey
IQRA_API_URL=http://127.0.0.1:5000/
```

You can now run a gradle sync and then run the app. To test the app's functionality, you will need to clone [the Iqra API server](https://github.com/Crescent-Labs/iqra-api) and then run it.

### Contributing

Contributions of patches and comments are welcome. If you'd like to contribute a new feature, create an issue for it first and only open a pull request once the feature request has been approved by [the project owner](https://github.com/mmmoussa).

### Installing APK directly

If you want to install the latest release of Iqra but do not have access to the Play store, the APK is available in this repo as `app-release.apk`.
