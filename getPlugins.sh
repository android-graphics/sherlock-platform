#!/bin/bash

# Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
# This script clones the Jetbrains/android repository. Note that the tag
# of the android repo must match the tag of the IntelliJ repo, otherwise
# there can be build errors.

# This value does not contain the actual snapshot value.
readonly AS_BUILD_NUMBER="$(sed 's/\.SNAPSHOT$//' build.txt)"

# When we pull a new IntelliJ platform, we must update this SNAPSHOT value.
# This can be automated (e.g., search all git tags for this prefix), but
# the benefit doesn't seem worth the time spent on automating it.
readonly SNAPSHOT="121"

readonly TAG="idea/${AS_BUILD_NUMBER}.${SNAPSHOT}"

echo "Cloning Jetbrains/android repository using the following tag: ${TAG}"

if [ ! -d "android" ]; then
  if [ "$1" == "--shallow" ]; then
    git clone git://git.jetbrains.org/idea/android.git android --depth 1 --branch "${TAG}"
  else
    echo "Warning: Cloning with the entire history. Use the --shallow flag to clone faster."
    git clone git://git.jetbrains.org/idea/android.git android --branch "${TAG}"
  fi
else
  echo "Android directory already exists. Skipping clone."
fi