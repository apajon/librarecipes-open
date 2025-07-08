#!/bin/bash

# Gestion de l'argument --dev
INSTALL_DEV=false
if [[ "$1" == "--dev" ]]; then
    INSTALL_DEV=true
fi

poetry config virtualenvs.in-project true
poetry run which python
poetry env activate
source .venv/bin/activate

if $INSTALL_DEV; then
    poetry install --no-root
else
    poetry install --no-root --without dev
fi
