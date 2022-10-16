package com.sdp.mymemory.models

// 'data class' is a feature in Kotlin that has standard functionality of classes as well as some utility functions that are often mechanically derivable from the data.
// We are declaring this data class to represent every attribute of a data card.
data class MemoryCard(
    // Represents uniqueness of a memory card i.e., the underlying resource id.
    val identifier: Int,
    var isFaceUp: Boolean = false, // Value changes so we declare it as 'var'.
    var isMatched: Boolean = false
)