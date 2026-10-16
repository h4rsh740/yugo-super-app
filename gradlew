#!/usr/bin/env bash
GRADLE_BIN="/Users/harshsingh/.gradle/wrapper/dists/gradle-8.14.3-all/10utluxaxniiv4wxiphsi49nj/gradle-8.14.3/bin/gradle"
if [ -x "$GRADLE_BIN" ]; then
    exec "$GRADLE_BIN" "$@"
else
    echo "Gradle binary not found at $GRADLE_BIN"
    exit 1
fi
