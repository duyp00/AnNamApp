package com.example.annamapp.navigation

import kotlinx.serialization.Serializable

/**
 * Defines all possible navigation destinations in the app in a type-safe manner.
 * Using @Serializable allows navigation to pass these objects as arguments.
 */
@Serializable
sealed interface Routes {
    /**
     * Home screen. No arguments.
     * We use a 'data object' for simplicity as it's a singleton.
     */
    @Serializable
    data object Home : Routes

    /**
     * Study screen. No arguments.
     */
    @Serializable
    data object Study : Routes

    /**
     * Add Card screen. No arguments.
     */
    @Serializable
    data object Add : Routes

    /**
     * Search/View All screen. No arguments.
     */
    @Serializable
    data object Search : Routes

    /**
     * Card Detail screen.
     * @param cardId The unique ID of the card to display.
     * This is a 'data class' because it carries data.
     */
    @Serializable
    data class CardDetail(val cardId: Int) : Routes
}