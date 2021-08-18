# Girl Scout Cookie Locator - Front-End

## Description

- This repo contains an android studio project called 'Girl Scout Cookie Locator'
- The purpose of this app is to help users find locations where girl-scouts are selling cookies
- By allowing users to pin cookie locations and verify active locations, cookies can hopefully be found much easier

## Dependencies

- implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
- implementation 'androidx.core:core-ktx:1.6.0'
- implementation 'androidx.appcompat:appcompat:1.3.1'
- implementation 'com.google.android.material:material:1.4.0'
- implementation 'com.google.android.gms:play-services-maps:17.0.1'
- implementation 'com.google.android.gms:play-services-location:18.0.0'
- implementation 'com.squareup.okhttp3:okhttp:5.0.0-alpha.2'
- implementation 'androidx.constraintlayout:constraintlayout:2.1.0'
- implementation 'com.squareup.retrofit2:retrofit:2.9.0'
- implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
- implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
- implementation 'com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2'
- implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.23'
- coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
- testImplementation 'junit:junit:4.+'
- androidTestImplementation 'androidx.test.ext:junit:1.1.3'
- androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'

## Instructions for Set-Up

- Clone repo
- Install dependencies
- Install and emulator/or use your phone
- Generate a google maps api key for android
- Finally you will need to create a file in res/values called 'google_maps_api.xml' which will contain your maps api key. It should look something like:

```
<resources>
    <!--
    TODO: Before you run your application, you need a Google Maps API key.

    To get one, follow this link, follow the directions and press "Create" at the end:

    https://console.developers.google.com/flows/enableapi?apiid=maps_android_backend&keyType=CLIENT_SIDE_ANDROID&r=C6:9E:BD:11:55:50:B9:B1:0B:B5:F2:50:FD:45:82:91:BF:E1:F8:AE%3Bcom.example.girlscoutcookielocator

    You can also add your credentials to an existing key, using these values:

    Package name:
    com.example.girlscoutcookielocator

    SHA-1 certificate fingerprint:
    C6:9E:BD:11:55:50:B9:B1:0B:B5:F2:50:FD:45:82:91:BF:E1:F8:AE

    Alternatively, follow the directions here:
    https://developers.google.com/maps/documentation/android/start#get-key

    Once you have your key (it starts with "AIza"), replace the "google_maps_key"
    string in this file.
    -->
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR-API-KEY-HERE</string>
</resources>
```