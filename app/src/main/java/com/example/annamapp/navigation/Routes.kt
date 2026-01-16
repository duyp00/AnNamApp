package com.example.annamapp.navigation

import kotlinx.serialization.Serializable

/**
 * Defines all possible navigation destinations in the app in a type-safe manner.
 * Using @Serializable allows navigation to pass these objects as arguments.
 */
@Serializable
sealed interface Routes {
    @Serializable
    data object Home: Routes

    @Serializable
    data object Study: Routes

    @Serializable
    data object Add: Routes

    @Serializable
    data object Search: Routes

    @Serializable
    data object LogIn: Routes

    @Serializable
    data class SearchResults(
        val englishQuery: String,
        val englishEnabled: Boolean,
        val englishWholeWord: Boolean,
        val vietnameseQuery: String,
        val vietnameseEnabled: Boolean,
        val vietnameseWholeWord: Boolean
    ): Routes

    //This is a data class because it carries data.
    @Serializable
    data class CardDetail(val en: String, val vn: String): Routes

    @Serializable
    data class TokenScreen(val email: String): Routes
}