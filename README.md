# MJ PDF (system)
This is a fork of [MJ PDF Reader](https://gitlab.com/mudlej_android/mj_pdf_reader) from Gitlab to fix compilation issues and to allow installing it as a system app in custom ROMs.
Changes made:<br>
1. Added repository which hosts com.google.android.flexbox:0.2.7 to app/build.gradle
2. Updated Android Gradle Plugin (AGP)
3. Removed launcher icon and internet permission
4. Some other changes in main build.gradle
5. Added `-dontwarn top.defaults.checkerboarddrawable.CheckerboardDrawable` to app/proguard-rules.pro
6. Removed intro
7. "High quality rendering" is turned on by default
