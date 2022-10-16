package com.sdp.mymemory.models

import com.sdp.mymemory.utils.DEFAULT_ICONS

// Delegating the task of creating memory cards to MemoryGame class.
class MemoryGame (private val boardSize: BoardSize) {

    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numFlips = 0 // To count number of flips made in the game.
    private var indexOfSingleSelected : Int? = null // Initially memory game has 0 flipped cards.

    init {
        // Default icons are shuffled and we take pairs based on number of cards in the game.
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())

        // Now we want to double up the number of images, we end up with two copies of an image.
        val randomisedImages = (chosenImages + chosenImages).shuffled()

        //'map{ } takes a data item (image) and performs an operation and stores in another new list; Creates new MemoryCard object.
        cards = randomisedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int) : Boolean{
        numFlips++
        val card = cards[position]

        // 0 cards are flipped over => flip selected card.
        // 1 card is flipped over => flip over a card + check if images match.
        // 2 cards are flipped over => restore cards first + flip selected card over.
        var foundMatch = false
        if(indexOfSingleSelected == null)
        {
            // 0 cards or 2 cards are flipped over.
            restoreCards()
            indexOfSingleSelected = position
        }

        else
        {
            // 1 card is flipped over.
             foundMatch = checkForMatch(indexOfSingleSelected!!,position) // to force indexOfSingleSelected to be a non-null int we put '!!' (non-null assertion).
             indexOfSingleSelected = null
        }
        card.isFaceUp = !card.isFaceUp // will be opposite of what it previously was.
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if(cards[position1].identifier != cards[position2].identifier)
            return false
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }


    private fun restoreCards() {
        for(card : MemoryCard in cards){
            if(!card.isMatched){
                card.isFaceUp = false
            }
        }

    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numFlips/2
    }
}