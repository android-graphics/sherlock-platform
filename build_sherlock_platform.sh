#!/usr/bin/env bash
set -eu

PROG_DIR="$(cd "$(dirname "$0")" && pwd)"

OUT="${PROG_DIR}/out/sherlock-platform"

readonly AS_BUILD_NUMBER="$(sed 's/\.SNAPSHOT$//' build.txt)"

BUILD_PROPERTIES=(
  "-Dintellij.build.output.root=${OUT}"
  "-Dbuild.number=${AS_BUILD_NUMBER}"
  "-Dintellij.build.dev.mode=false"
  "-Dcompile.parallel=true"
  "-Dintellij.build.skip.build.steps=repair_utility_bundle_step,mac_dmg,mac_sign,mac_sit,windows_exe_installer,linux aarch64,windows aarch64,mac x64"
  "-Dintellij.build.incremental.compilation=true"
  "-Dintellij.build.incremental.compilation.fallback.rebuild=false"
)

"${PROG_DIR}/platform/jps-bootstrap/jps-bootstrap.sh" "${BUILD_PROPERTIES[@]}" "${PROG_DIR}" intellij.idea.community.build SherlockPlatformBuild
