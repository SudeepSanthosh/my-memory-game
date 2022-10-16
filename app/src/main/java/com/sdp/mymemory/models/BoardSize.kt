package com.sdp.mymemory.models

// An enum can optionally take in a value
enum class BoardSize(val numCards: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);

    // Determines Width of the memory game
    fun getWidth(): Int {
        // 'this' is referring to the board size on which we are operating.
        // 'when' statement is like switch statement where it evaluates a list of conditions and runs one when its true.
        return when (this) {
            EASY -> 2 // 2 cards wide
            MEDIUM -> 3 // 3 cards wide
            HARD -> 4 // 4 cards wide
        }
    }

    // Determines Height of the memory game
    fun getHeight(): Int {
        // Height is fully determined after getting value of numCards and Width.
        return numCards / getWidth()
    }

    // Determines number of pairs of images present in the memory game
    fun getNumPairs(): Int {
        return numCards / 2
    }
}