#!/usr/bin/env bash
set -euo pipefail

# Répertoires
root_dir="$(pwd)"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Aller dans le dossier Android contenant gradlew
pushd "$script_dir/android_app" >/dev/null

usage() {
  cat <<'EOF'
Usage: build_gradle.sh [--menu] [--action <n|name>] [options]

Actions:
  1 | build                Build (par défaut)
  2 | clean-build          Clean puis build
  3 | refresh-build        Build avec --refresh-dependencies
  4 | clean                Clean uniquement
  5 | stop-daemon          Arrêter les daemons Gradle
  6 | full-refresh         Stop daemons, purge caches (~/.gradle + ./.gradle), clean, build --refresh-dependencies
  7 | assembleDebug        Exécute ./gradlew assembleDebug (sans build préalable)
  8 | assembleRelease      Exécute ./gradlew assembleRelease (sans build préalable)
  9 | total-refresh-build  Stop daemons, supprime ./.gradle et ./app/build, puis build --refresh-dependencies
 10 | total-refresh-debug  Stop daemons, supprime ./.gradle et ./app/build, puis assembleDebug --refresh-dependencies

Options:
  -a, --action <val>           Numéro (1-10) ou nom d'action ci-dessus
      --menu                   Afficher un menu interactif pour choisir l'action
      --full-refresh           Raccourci pour --action full-refresh
      --total-refresh-build    Raccourci pour --action total-refresh-build
      --total-refresh-debug    Raccourci pour --action total-refresh-debug
      --total-refresh          Alias de --total-refresh-debug (compat)
      --clean                  Raccourci pour --action clean
      --refresh-deps           Raccourci pour --action refresh-build
      --stop                   Raccourci pour --action stop-daemon
      --assemble-debug         Raccourci pour --action assembleDebug
      --assemble-release       Raccourci pour --action assembleRelease
  -y, --yes                    Ne pas demander de confirmation pour les actions destructrices
  -h, --help                   Afficher cette aide

Exemples:
  ./build_gradle.sh                     # build simple (défaut)
  ./build_gradle.sh --action 2          # clean + build
  ./build_gradle.sh --action 6          # full refresh
  ./build_gradle.sh --action 9          # total refresh (build)
  ./build_gradle.sh --action 10         # total refresh (assembleDebug)
  ./build_gradle.sh --menu              # menu interactif
  ./build_gradle.sh --assemble-debug    # assembleDebug seul
EOF
}

menu=false
action="build"
assume_yes=false

# Parsing des arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --menu)
      menu=true; shift ;;
    -a|--action)
      [[ $# -ge 2 ]] || { echo "Erreur: --action requiert une valeur"; usage; popd >/dev/null; exit 2; }
      action="$2"; shift 2 ;;
    --full-refresh)
      action="full-refresh"; shift ;;
    --total-refresh-build)
      action="total-refresh-build"; shift ;;
    --total-refresh-debug|--total-refresh)
      action="total-refresh-debug"; shift ;;
    --clean)
      action="clean"; shift ;;
    --refresh-deps|--refresh-build)
      action="refresh-build"; shift ;;
    --stop|--stop-daemon)
      action="stop-daemon"; shift ;;
    --assemble-debug)
      action="assembleDebug"; shift ;;
    --assemble-release)
      action="assembleRelease"; shift ;;
    -y|--yes)
      assume_yes=true; shift ;;
    -h|--help)
      usage; popd >/dev/null; exit 0 ;;
    *)
      echo "Option non reconnue: $1"; echo; usage; popd >/dev/null; exit 2 ;;
  esac
done

# Normaliser action si numérique
case "$action" in
  1) action="build" ;;
  2) action="clean-build" ;;
  3) action="refresh-build" ;;
  4) action="clean" ;;
  5) action="stop-daemon" ;;
  6) action="full-refresh" ;;
  7) action="assembleDebug" ;;
  8) action="assembleRelease" ;;
  9) action="total-refresh-build" ;;
  10|10) action="total-refresh-debug" ;;
  *) : ;;
esac

# Menu interactif
if [[ "$menu" == true ]]; then
  echo "Choisissez une action:" >&2
  echo "  1) build simple" >&2
  echo "  2) clean + build" >&2
  echo "  3) build avec --refresh-dependencies" >&2
  echo "  4) clean uniquement" >&2
  echo "  5) arrêter les daemons Gradle" >&2
  echo "  6) full refresh (purge caches)" >&2
  echo "  7) assembleDebug (sans build préalable)" >&2
  echo "  8) assembleRelease (sans build préalable)" >&2
  echo "  9) total refresh (build)" >&2
  echo " 10) total refresh (assembleDebug)" >&2
  read -rp "Votre choix [1-10]: " choice
  case "$choice" in
    1) action="build" ;;
    2) action="clean-build" ;;
    3) action="refresh-build" ;;
    4) action="clean" ;;
    5) action="stop-daemon" ;;
    6) action="full-refresh" ;;
    7) action="assembleDebug" ;;
    8) action="assembleRelease" ;;
    9) action="total-refresh-build" ;;
    10) action="total-refresh-debug" ;;
    *) echo "Choix invalide."; popd >/dev/null; exit 1 ;;
  esac
fi

confirm() {
  if [[ "$assume_yes" == true ]]; then
    return 0
  fi
  local prompt=${1:-"Confirmer ?"}
  read -rp "$prompt [y/N]: " ans
  [[ "$ans" =~ ^[Yy]$ ]]
}

run_build() { ./gradlew build; }
run_clean() { ./gradlew clean; }
run_clean_build() { ./gradlew clean && ./gradlew build; }
run_refresh_build() { ./gradlew build --refresh-dependencies; }
run_stop_daemon() { ./gradlew --stop || true; }
run_assemble_debug() { ./gradlew assembleDebug; }
run_assemble_release() { ./gradlew assembleRelease; }
run_assemble_debug_refresh() { ./gradlew assembleDebug --refresh-dependencies; }

# Exécuter l'action
case "$action" in
  build)
    echo "[Action] build"
    run_build
    ;;
  clean-build)
    echo "[Action] clean + build"
    run_clean_build
    ;;
  refresh-build)
    echo "[Action] build --refresh-dependencies"
    run_refresh_build
    ;;
  clean)
    echo "[Action] clean"
    run_clean
    ;;
  stop-daemon)
    echo "[Action] stop daemons"
    run_stop_daemon
    ;;
  full-refresh)
    echo "[Action] full refresh"
    run_stop_daemon
    if confirm "Supprimer les caches Gradle (~/.gradle/{caches,daemon,native,wrapper}) et ./.gradle du projet ?"; then
      rm -rf ~/.gradle/caches ~/.gradle/daemon ~/.gradle/native ~/.gradle/wrapper
      rm -rf .gradle
    else
      echo "Action annulée."
      popd >/dev/null
      cd "$root_dir" >/dev/null || true
      exit 1
    fi
    run_clean
    run_refresh_build
    ;;
  total-refresh-build)
    echo "[Action] total refresh (build)"
    run_stop_daemon
    if confirm "Supprimer ./.gradle et ./app/build du projet ?"; then
      rm -rf .gradle
      rm -rf app/build
    else
      echo "Action annulée."
      popd >/dev/null
      cd "$root_dir" >/dev/null || true
      exit 1
    fi
    run_refresh_build
    ;;
  total-refresh-debug|total-refresh)
    echo "[Action] total refresh (assembleDebug)"
    run_stop_daemon
    if confirm "Supprimer ./.gradle et ./app/build du projet ?"; then
      rm -rf .gradle
      rm -rf app/build
    else
      echo "Action annulée."
      popd >/dev/null
      cd "$root_dir" >/dev/null || true
      exit 1
    fi
    run_assemble_debug_refresh
    ;;
  assembleDebug)
    echo "[Action] assembleDebug (sans build préalable)"
    run_assemble_debug
    ;;
  assembleRelease)
    echo "[Action] assembleRelease (sans build préalable)"
    run_assemble_release
    ;;
  *)
    echo "Action inconnue: $action"; echo; usage; popd >/dev/null; cd "$root_dir" >/dev/null || true; exit 2 ;;
fi

# Retour au dossier d'origine
popd >/dev/null
cd "$root_dir" >/dev/null || true
