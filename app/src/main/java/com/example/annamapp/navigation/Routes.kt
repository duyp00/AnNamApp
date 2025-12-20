package com.example.annamapp.navigation

import kotlinx.serialization.Serializable

/**
 * Defines all possible navigation destinations in the app in a type-safe manner.
 * Using @Serializable allows navigation to pass these objects as arguments.
 */
@Serializable
sealed interface Routes {
    @Serializable
    data object Home

    @Serializable
    data object Study

    @Serializable
    data object Add

    @Serializable
    data object Search

    @Serializable
    data object LogIn

    @Serializable
    data class SearchResults(
        val englishQuery: String = "",
        val englishEnabled: Boolean = false,
        val englishWholeWord: Boolean = false,
        val vietnameseQuery: String = "",
        val vietnameseEnabled: Boolean = false,
        val vietnameseWholeWord: Boolean = false
    )

    /**
     * Card Detail screen.
     * @param cardId The unique ID of the card to display.
     * This is a 'data class' because it carries data.
     */
    @Serializable
    data class CardDetail(val cardId: Int?)

    @Serializable
    data class TokenScreen(val email: String)
}