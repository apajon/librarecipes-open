#!/bin/bash

root_dir="$(pwd)"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "Root directory: $root_dir"
echo "Script directory: $script_dir"
cd "$script_dir"
PYTHONPATH=. streamlit run app/Home.py
if [ $? -ne 0 ]; then
    echo "Failed to start the Streamlit app."
    exit 1
fi
echo "Streamlit app started successfully."
cd "$root_dir"
echo "Back to root directory: $root_dir"
echo "To stop the app, press Ctrl+C."
echo "You can also run the app directly with 'streamlit run app/Home.py' from the root directory."
