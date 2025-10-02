#!/usr/bin/env bash

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 [patch|minor|major] [--push] [--dry-run]"
    exit 1
fi

LEVEL=$1
PUSH_FLAG=""
DRY_RUN_FLAG=""

# Options
for arg in "$@"; do
    case "$arg" in
        --push) PUSH_FLAG="--push" ;;
        --dry-run) DRY_RUN_FLAG="--dry-run" ;;
    esac
done

if [[ "$LEVEL" != "patch" && "$LEVEL" != "minor" && "$LEVEL" != "major" ]]; then
    echo "Invalid level: $LEVEL. Use patch, minor, or major."
    exit 1
fi

# Appelle bump2version
bump2version "$LEVEL" $DRY_RUN_FLAG

if [[ "$PUSH_FLAG" == "--push" ]]; then
    git push --tags
fi
