#!/bin/bash

# Original Script: https://gist.github.com/GR8DAN/da9240b26e6727f53ee4bc68108c6c65

echo "Clean a Android Studio project ready for importing and zipping pure code"
echo "See http://tekeye.uk/android/export-android-studio-project"
echo "Modify this file to meet project requirements, it only does the basics"
echo "Edited by Mudlej to run on Linux and keep tests and some gradle files"

read -p "Press any key to continue? (ctrl+c to cancel) " prompt

echo "Remove Gradle code, added back in on import"
rm -rf .gradle

echo "Remove IDE files"
rm -rf .idea
rm *.iml
rm local.properties

echo "Remove build folders, will be recreated"
rm -rf build
rm -rf app/build

#echo "Remove Gradle Wrapper, will be added back in"
#rm -rf gradle

echo "Remove Git ignore files"
rm .gitignore

#echo "Remove other Gradle files"
#rm gradle.properties
#rm gradle?.*

echo "Remove libs folder"
rm -rf app/libs

#echo "Remove ProGuard rules"
#rm app/proguard-rules.pro

#echo "Remove test code"
#rm -rf app/src/androidTest
#rm -rf app/src/test

echo "Do not forget to edit build.gradle in the app directory"
echo "Finished."
