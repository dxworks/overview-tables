#!/usr/bin/env bash
set -euo pipefail

# Build the overview-tables package end to end:
#   1. Build the Java/Kotlin fat jar with Maven (produces target/overview-tables.jar)
#   2. Install Node dependencies
#   3. Assemble the npm package in dist/ (copies lib/*.js + the jar)
#
# Puppeteer's Chromium download is skipped: it is only needed at runtime by the
# `table-pics` command on a consumer machine, not to build/publish the package.
export PUPPETEER_SKIP_DOWNLOAD="${PUPPETEER_SKIP_DOWNLOAD:-true}"

mvn -q clean package -DskipTests
npm ci
npm run build
