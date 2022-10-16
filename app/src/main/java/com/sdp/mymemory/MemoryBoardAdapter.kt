package com.sdp.mymemory

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.sdp.mymemory.models.BoardSize
import com.sdp.mymemory.models.MemoryCard
import kotlin.math.min
// Prefixing the parameters with 'private val' allows us to use them within the class body.
// ViewHolder is an object which provides access to all views of one RecyclerView element.
class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,

    //'Int' represents one of the drawable resources
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN_SIZE = 10
        private const val TAG = "MemoryBoardAdapter"
    }

    // To change state of the cards on click we generally make use of an interface
    interface CardClickListener {
        fun onCardClicked (position: Int)
    }

    // onCreateViewHolder() is responsible for figuring out how to create one view of our RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // This is to find the width and height of each card in the RecyclerView
        val cardWidth = parent.width / boardSize.getWidth() - (2* MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2* MARGIN_SIZE)
        // To mandate that our Card is a square
        val cardSideLength = min(cardWidth,cardHeight)
        val view = LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)

        // To grab out the CardView from the inflated View and set the width and height of the view
        val layoutParams = view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = cardSideLength
        layoutParams.width = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        // Returning view inside a ViewHolder
        return ViewHolder(view)
    }

    // onBindViewHolder() is responsible for taking the data in the position and binding it to the ViewHolder.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    // getItemCount() is simply the number of elements in our RecyclerView.
    override fun getItemCount() = boardSize.numCards

    // A nested class marked as 'inner' can access members of its outer class.
    // Inner classes carry a reference to an object of an outer class.
    // We define this class (our own ViewHolder) to encapsulate the memory card View
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)


        fun bind(position: Int) {
            val memoryCard = cards[position]

            // To reference the images; Grab the current image and reference it under the image button.
            imageButton.setImageResource(if(memoryCard.isFaceUp) memoryCard.identifier else R.drawable.ic_launcher_background)

            // Reducing opacity of card if match found. Else keeping it as it is.
            imageButton.alpha = if(memoryCard.isMatched) .4f  else 1.0f
            val colorStateList = if(memoryCard.isMatched) ContextCompat.getColorStateList(context,R.color.color_grey) else null
            ViewCompat.setBackgroundTintList(imageButton,colorStateList) // used to set background of matched images to 'grey'.

            imageButton.setOnClickListener {
                Log.i(TAG, "Clicked on Position $position")
                cardClickListener.onCardClicked(position)
            }
        }
    }

}
