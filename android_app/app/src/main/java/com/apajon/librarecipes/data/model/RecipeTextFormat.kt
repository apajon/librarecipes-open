package com.apajon.librarecipes.data.model

/**
 * Utility for converting recipes to/from a simple human-readable text format.
 *
 * Format:
 * ```
 * Titre: Crêpes bretonnes
 * Préparation: 15 min
 * Cuisson: 20 min
 * Portions: 4
 * Catégories: dessert, crêpes
 * Tags: facile, rapide
 * Source: Maison
 *
 * Ingrédients:
 * - 250 g Farine
 * - 3 pièce Œufs
 * - 50 cl Lait
 * - Sel (optionnel, alt: fleur de sel)
 *
 * Étapes:
 * 1. Mélanger la farine et les œufs
 * 2. Ajouter le lait progressivement
 * 3. Cuire dans une poêle chaude
 * ```
 */
object RecipeTextFormat {

    private const val KEY_TITRE = "Titre"
    private const val KEY_PREPARATION = "Préparation"
    private const val KEY_CUISSON = "Cuisson"
    private const val KEY_PORTIONS = "Portions"
    private const val KEY_CATEGORIES = "Catégories"
    private const val KEY_TAGS = "Tags"
    private const val KEY_SOURCE = "Source"
    private const val SECTION_INGREDIENTS = "Ingrédients:"
    private const val SECTION_ETAPES = "Étapes:"

    /**
     * Format a RecipeDetail as human-readable text.
     */
    fun formatRecipe(recipe: RecipeDetail): String {
        return buildString {
            appendLine("$KEY_TITRE: ${recipe.nom}")

            recipe.preparation?.let { appendLine("$KEY_PREPARATION: $it min") }
            recipe.cuisson?.let { appendLine("$KEY_CUISSON: $it min") }
            recipe.portions?.let { appendLine("$KEY_PORTIONS: $it") }

            if (recipe.categories.isNotEmpty()) {
                appendLine("$KEY_CATEGORIES: ${recipe.categories.joinToString(", ")}")
            }
            if (recipe.tags.isNotEmpty()) {
                appendLine("$KEY_TAGS: ${recipe.tags.joinToString(", ")}")
            }

            recipe.source?.let { source ->
                appendLine("$KEY_SOURCE: ${formatSource(source)}")
            }

            if (recipe.ingredients.isNotEmpty()) {
                appendLine()
                appendLine(SECTION_INGREDIENTS)
                recipe.ingredients.forEach { ingredient ->
                    appendLine("- ${formatIngredient(ingredient)}")
                }
            }

            if (recipe.etapes.isNotEmpty()) {
                appendLine()
                appendLine(SECTION_ETAPES)
                recipe.etapes.sortedBy { it.numero }.forEachIndexed { index, etape ->
                    appendLine("${index + 1}. ${etape.description}")
                }
            }
        }.trimEnd()
    }

    private fun formatSource(source: SourceDetail): String {
        return when (source.type) {
            "url" -> source.url ?: "Site web"
            "book" -> buildString {
                append(source.bookTitle ?: "Livre")
                source.bookAuthors?.let { append(" — $it") }
                source.bookPage?.let { append(" (p. $it)") }
            }
            else -> "Maison"
        }
    }

    private fun formatIngredient(ingredient: IngredientDetail): String {
        return buildString {
            ingredient.quantite?.let { qty ->
                val formatted = if (qty == qty.toLong().toFloat()) {
                    qty.toLong().toString()
                } else {
                    qty.toString()
                }
                append("$formatted ")
            }
            ingredient.unite?.takeIf { it.isNotBlank() }?.let { append("$it ") }
            append(ingredient.nom)
            if (!ingredient.indispensable) {
                append(" (optionnel")
                ingredient.alternatives?.takeIf { it.isNotBlank() }?.let { append(", alt: $it") }
                append(")")
            } else {
                ingredient.alternatives?.takeIf { it.isNotBlank() }?.let { append(" (alt: $it)") }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Parsing
    // -----------------------------------------------------------------------

    /**
     * Parse human-readable text into a RecipeCreate.
     * Returns null if the text cannot be parsed (missing Titre).
     */
    fun parseRecipe(text: String): RecipeCreate? {
        val lines = text.lines()
        if (lines.isEmpty()) return null

        var nom: String? = null
        var preparation: Int? = null
        var cuisson: Int? = null
        var portions: Int? = null
        var categories: List<String> = emptyList()
        var tags: List<String> = emptyList()
        var sourceText: String? = null
        val ingredients = mutableListOf<IngredientCreate>()
        val etapes = mutableListOf<String>()

        var currentSection: String? = null // "ingredients" or "etapes"

        for (rawLine in lines) {
            val line = rawLine.trim()

            // Detect section headers
            if (line.equals(SECTION_INGREDIENTS, ignoreCase = true) ||
                line.equals("Ingrédients :", ignoreCase = true)) {
                currentSection = "ingredients"
                continue
            }
            if (line.equals(SECTION_ETAPES, ignoreCase = true) ||
                line.equals("Étapes :", ignoreCase = true)) {
                currentSection = "etapes"
                continue
            }

            // Empty line does not change section
            if (line.isBlank()) continue

            when (currentSection) {
                "ingredients" -> {
                    parseIngredientLine(line)?.let { ingredients.add(it) }
                }
                "etapes" -> {
                    parseStepLine(line)?.let { etapes.add(it) }
                }
                else -> {
                    // Header key:value pairs
                    val titreVal = parseKeyValue(line, KEY_TITRE)
                    val prepVal = parseKeyValue(line, KEY_PREPARATION)
                    val cuissonVal = parseKeyValue(line, KEY_CUISSON)
                    val portionsVal = parseKeyValue(line, KEY_PORTIONS)
                    val categoriesVal = parseKeyValue(line, KEY_CATEGORIES)
                    val tagsVal = parseKeyValue(line, KEY_TAGS)
                    val sourceVal = parseKeyValue(line, KEY_SOURCE)

                    if (titreVal != null) {
                        nom = titreVal
                    } else if (prepVal != null) {
                        preparation = extractMinutes(prepVal)
                    } else if (cuissonVal != null) {
                        cuisson = extractMinutes(cuissonVal)
                    } else if (portionsVal != null) {
                        portions = portionsVal.trim().toIntOrNull()
                    } else if (categoriesVal != null) {
                        categories = splitCsv(categoriesVal)
                    } else if (tagsVal != null) {
                        tags = splitCsv(tagsVal)
                    } else if (sourceVal != null) {
                        sourceText = sourceVal
                    }
                }
            }
        }

        val finalNom = nom?.trim()
        if (finalNom.isNullOrBlank()) return null

        val source = sourceText?.let { parseSource(it) }

        return RecipeCreate(
            nom = finalNom,
            preparation = preparation,
            cuisson = cuisson,
            portions = portions,
            ingredients = ingredients,
            etapes = etapes,
            categories = categories,
            tags = tags,
            source = source
        )
    }

    // -----------------------------------------------------------------------
    // Parsing helpers
    // -----------------------------------------------------------------------

    private fun parseKeyValue(line: String, key: String): String? {
        // Accept "Key: value" or "Key : value"
        val prefixes = listOf("$key:", "$key :")
        for (prefix in prefixes) {
            if (line.startsWith(prefix, ignoreCase = true)) {
                return line.substring(prefix.length).trim()
            }
        }
        return null
    }

    private fun extractMinutes(value: String): Int? {
        // "15 min" → 15 ; "15" → 15
        return value.replace(Regex("[^\\d]"), "").trim().toIntOrNull()
    }

    private fun splitCsv(value: String): List<String> {
        return value.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    /**
     * Parse an ingredient line.
     * Accepts: "- 250 g Farine", "- Sel (optionnel)", "- 3 pièce Œufs (alt: oeufs bio)"
     */
    private fun parseIngredientLine(line: String): IngredientCreate? {
        // Strip leading "- " or "– "
        val cleaned = line.removePrefix("-").removePrefix("–").trim()
        if (cleaned.isBlank()) return null

        var indispensable = true
        var alternatives: String? = null

        // Extract parenthetical suffix
        var mainPart = cleaned
        val parenMatch = Regex("""\(([^)]+)\)\s*$""").find(cleaned)
        if (parenMatch != null) {
            mainPart = cleaned.substring(0, parenMatch.range.first).trim()
            val parenContent = parenMatch.groupValues[1]
            val parts = parenContent.split(",").map { it.trim() }
            for (part in parts) {
                if (part.equals("optionnel", ignoreCase = true)) {
                    indispensable = false
                } else if (part.startsWith("alt:", ignoreCase = true)) {
                    alternatives = part.substringAfter(":").trim()
                }
            }
        }

        // Try to parse: [quantity] [unit] name
        val tokens = mainPart.split(Regex("\\s+"), limit = 3)

        val quantite: Float?
        val unite: String?
        val nom: String

        if (tokens.size >= 2 && tokens[0].toFloatOrNull() != null) {
            quantite = tokens[0].toFloat()
            // Check if second token is a known unit
            if (tokens.size >= 3 && isKnownUnit(tokens[1])) {
                unite = tokens[1]
                nom = tokens[2]
            } else {
                unite = null
                nom = tokens.drop(1).joinToString(" ")
            }
        } else {
            quantite = null
            unite = null
            nom = mainPart
        }

        if (nom.isBlank()) return null

        return IngredientCreate(
            nom = nom,
            quantite = quantite,
            unite = unite,
            indispensable = indispensable,
            alternatives = alternatives
        )
    }

    private fun isKnownUnit(token: String): Boolean {
        return token.lowercase() in listOf(
            "g", "kg", "ml", "cl", "l",
            "c.", "c.à", // partial matches for "c. à c.", "c. à s."
            "tasse", "pièce", "pincée", "gousse", "botte", "tranche"
        ) || MeasurementUnits.units.any {
            it.isNotBlank() && it.equals(token, ignoreCase = true)
        }
    }

    /**
     * Parse a step line. Accepts "1. Do something" or plain "Do something".
     */
    private fun parseStepLine(line: String): String? {
        val cleaned = line.removePrefix("-").trim()
        // Remove leading number + dot: "1. " → ""
        val withoutNumber = cleaned.replace(Regex("""^\d+\.\s*"""), "").trim()
        return withoutNumber.ifBlank { null }
    }

    /**
     * Parse source text to SourceCreate.
     */
    private fun parseSource(text: String): SourceCreate {
        val trimmed = text.trim()
        return when {
            trimmed.equals("Maison", ignoreCase = true) ||
            trimmed.equals("Recette maison", ignoreCase = true) -> {
                SourceCreate(type = "homemade")
            }
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) -> {
                SourceCreate(type = "url", valeur = trimmed)
            }
            else -> {
                // Treat as book title (possibly with author/page)
                SourceCreate(type = "book", valeur = trimmed.substringBefore(" — ").trim())
            }
        }
    }
}
