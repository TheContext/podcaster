package io.thecontext.ci.model

/**
 * The basic ituenes data used for the rss feed's meta info
 */
data class ItunesConfig (
        val title : String,
        val link : String,
        val description: String,
        val language: String,
        val rssLink : String,
        val keywords: String,
        val subtitle : String,
        val summary : String,
        val imageUrl: String,
        val author : String,
        val categories : List<String>
)