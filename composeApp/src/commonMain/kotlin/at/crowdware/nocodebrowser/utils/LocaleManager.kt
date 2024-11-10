/****************************************************************************
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeBrowser.
 *
 *  NoCodeBrowser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeBrowser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeBrowser.  If not, see <http://www.gnu.org/licenses/>.
 *
 ****************************************************************************/

package at.crowdware.nocodebrowser.utils

import java.util.*
import java.util.ResourceBundle

object LocaleManager {
    private var currentLocale: Locale? = null
    private var language: String? = ""
    private var index: Int = -1
    private val language_codes = listOf("en", "de", "es", "fr", "pt", "eo")
    private val languages: MutableList<String> = mutableListOf()

    // Setzt die Sprache basierend auf dem Sprachcode
    fun setLocale(lang: String) {
        language = lang
        index = language_codes.indexOf(language)
        // Hier kannst du die Sprache in einer Konfigurationsdatei oder einer anderen persistenten Lösung speichern
        updateResources(lang)
    }

    // Setzt die Sprache basierend auf dem Index
    fun setLocale(index: Int) {
        if(index < 0 || index > 5) return
        language = language_codes[index]
        // Hier kannst du die Sprache in einer Konfigurationsdatei oder einer anderen persistenten Lösung speichern
        updateResources(language!!)
    }

    // Gibt die aktuelle Sprache zurück
    fun getLanguage(): String { return language!! }

    // Gibt den Index der aktuellen Sprache zurück
    fun getLanguageIndex(): Int { return index }

    // Gibt alle verfügbaren Sprachen zurück
    fun getLanguages(): List<String> {
        return languages.toList()
    }

    // Initialisiert die verfügbaren Sprachen
    fun init() {
        languages.clear()
        languages.add("English")
        languages.add("German")
        languages.add("Spanish")
        languages.add("French")
        languages.add("Portuguese")
        languages.add("Esperanto")

        if (currentLocale == null) {
            language = "en" // Standardwert
            index = language_codes.indexOf(language)
            currentLocale = Locale(language!!)
        }
    }

    // Aktualisiert die Ressourcen basierend auf der ausgewählten Sprache
    private fun updateResources(language: String) {
        if(language.isEmpty()) return
        val locale = Locale(language)
        Locale.setDefault(locale)

        // Hier kannst du anpassen, wie du die Ressourcen für deine Anwendung verwaltest
        // Beispielsweise kannst du mit ResourceBundles arbeiten, um sprachabhängige Ressourcen zu laden
        val bundle = ResourceBundle.getBundle("Messages", locale)

        // Wenn du GUI-Elemente hast, die aktualisiert werden müssen, kannst du hier mit deinen Ressourcen arbeiten
        // Beispiel: label.text = bundle.getString("greeting")
    }

    // Diese Methode stellt sicher, dass der aktuelle Locale-Objekt immer verwendet wird
    fun wrapContext() {
        if (currentLocale == null)
            init()

        if(currentLocale != null) {
            Locale.setDefault(currentLocale!!)
            // Wenn du weitere Einstellungen benötigst, kannst du sie hier hinzufügen
        }
    }
}