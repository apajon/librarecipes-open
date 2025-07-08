ROOT_DIR="$(pwd)"

# cp scripts/install_repo/pre-commit .git/hooks/
# Vérifier si pre-commit est déjà installé
if [ ! -f .git/hooks/pre-commit ] || ! grep -q "pre-commit" .git/hooks/pre-commit 2>/dev/null; then
    echo "Installation de pre-commit..."
    pre-commit install
    pre-commit install --overwrite
else
    echo "Pre-commit est déjà installé."
fi

# Vérifier si .pre-commit-config.yaml existe et n'est pas encore versionné
if [ -f .pre-commit-config.yaml ] && ! git ls-files --error-unmatch .pre-commit-config.yaml >/dev/null 2>&1; then
    echo "Ajout du fichier de configuration pre-commit au git..."
    git add .pre-commit-config.yaml
    git commit -m "initialize pre-commit"
else
    echo "Pre-commit est déjà configuré."
fi
echo "Exécution de pre-commit sur tous les fichiers..."
pre-commit run --all-files || true

# Reviens dans le dossier d'origine
cd "$ROOT_DIR"
