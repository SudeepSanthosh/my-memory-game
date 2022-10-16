@file:Suppress("DEPRECATION")

package com.sdp.mymemory

import android.animation.ArgbEvaluator
import android.content.Intent
import android.icu.text.CaseMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sdp.mymemory.models.BoardSize
import com.sdp.mymemory.models.MemoryCard
import com.sdp.mymemory.models.MemoryGame
import com.sdp.mymemory.utils.DEFAULT_ICONS
import com.sdp.mymemory.utils.EXTRA_BOARD_SIZE

class MainActivity : AppCompatActivity() {

    // A companion object and its class can access each other's private members
    // It is used to define static variables
    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 286 // Value does not matter as long as it is a unique integer value inside your Activity
    }

    //'lateinit var' means they will be initialised later in the OnCreate method and not during its construction.
    private lateinit var sdpBoard: RecyclerView
    private lateinit var clRoot : ConstraintLayout
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter


    private var boardSize : BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setting up the Scaffolding i.e the temporary structure for our activity
        sdpBoard = findViewById(R.id.sdpBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        clRoot = findViewById(R.id.clRoot)

        setupBoard() // Function call to create and setup the memory game.
    }

    // Inflating the Menu XML to a view on MainActivity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    // Notifies us when user clicks on menu item.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.mi_refresh -> {
               if(memoryGame.getNumMoves()>0 && !memoryGame.haveWonGame()){
                   showAlertDialog("Quit your current game?",null,View.OnClickListener {
                       setupBoard()
                   })
               }
               else{
                   // setup game again
                   setupBoard()
               }
           }
           R.id.mi_newsize ->{
               showNewSizeDialog()
               return true
           }
           R.id.mi_custom ->{
               showCreationDialog()

           }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)

        // Defining RadioGroupSize and pulling it out of the boardSizeView
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Choose your own memory board",boardSizeView,View.OnClickListener {
            // Set a new value for the board size
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // Navigate to a new activity where they can choose what photos they'd like to include
                // Intents are used to navigate between activities. They are requests to the android system or application to perform some particular action
            val intent = Intent(this,CreateActivity::class.java)
            // This is to pass the desired Board Size into the CreateActivity class.
            // EXTRA_BOARD_SIZE is declared in Constants.kt as both MainActivity.kt and CreateActivity.kt require this constant value.
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            // We make use of startActivityForResult() to get data back from the new activity back to Main Activity.
            startActivityForResult(intent, CREATE_REQUEST_CODE)

        })

    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)

        // Defining RadioGroupSize and pulling it out of the boardSizeView
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        // To show the current size of Memory Board
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        // To choose new size of Memory Board
        showAlertDialog("Choose new size",boardSizeView,View.OnClickListener {
            // Set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String,view: View?,positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){_,_->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setupBoard() {
        // To reset the stats of the game when new game begins.
        when(boardSize){
            BoardSize.EASY -> {
                tvNumMoves.text ="Easy: 4 x 2"
                tvNumPairs.text ="Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text ="Medium: 6 x 3"
                tvNumPairs.text ="Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text ="Hard: 6 x 4"
                tvNumPairs.text ="Pairs: 0 / 12"
            }
        }
        // Setting initial color of the tvNumPairs TextView as Red.
        tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.progress_none))
        // Creating a Memory Game (logic passed in 'init' block inside MemoryGame file).
        memoryGame = MemoryGame(boardSize)

        //Adapter is responsible for binding the data set to the views of the RecyclerView.
        //Adapter is more involved than the LayoutManager because it is responsible for taking the underlying data-set and converting each data to views.
        //MemoryBoardAdapter is a new class that is gonna hold all the logic for the adapter.
        // 'randomisedImages' is passed as parameter to adapter to pass the images we wish to use for our game.
        // 'object' is specified to create anonymous class of type CardClickListener.
        adapter = MemoryBoardAdapter(this,boardSize,memoryGame.cards, object : MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })

        sdpBoard.adapter = adapter
        // It is a method in RecyclerView and is used for performance optimisation.
        // If size of RecyclerView depends on Adapter content then set it to 'true', else set it to 'false'.(doubt)
        sdpBoard.setHasFixedSize(true)

        //LayoutManager is a core feature of RecyclerView and is responsible for measuring and positioning the views
        //GridLayoutManager is a predefined layout manager that comes with Android and it gives the grid effect.
        //It has 2 parameters : context i.e,the activity we are referring to and spanCount i.e, the number of columns we require.
        sdpBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())
    }

    // Function to flip the card that is clicked.
    private fun updateGameWithFlip(position: Int) {
        // Error checking
        if(memoryGame.haveWonGame()) {
            // Alert the user of an invalid move
                // Snackbar is an Android component that shows up at the bottom of the screen and displays a message to the user.
                // clRoot is the root element on which the Snackbar message is anchored.
                    Snackbar.make(clRoot, "You already Won!", Snackbar.LENGTH_LONG).show()
                    return
        }

        if(memoryGame.isCardFaceUp(position)) {
            //Alert the user of an invalid move
                Snackbar.make(clRoot, "Invalid Move!", Snackbar.LENGTH_SHORT).show()
                return
        }

        // Actually flip over card.
        if(memoryGame.flipCard(position)){
                Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound} ")
                // To update the TextView when a pair is found.

                val color = ArgbEvaluator().evaluate(
                    memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                    ContextCompat.getColor(this,R.color.progress_none),
                    ContextCompat.getColor(this,R.color.progress_full)
                ) as Int
                tvNumPairs.setTextColor(color)
                tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            }

        if(memoryGame.haveWonGame())
        {
            Snackbar.make(clRoot,"You won! Congratulations", Snackbar.LENGTH_LONG).show()
        }

        // To update the TextView when more moves are made.
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
            // Notifies adapter to change the state of the clicked card.
            adapter.notifyDataSetChanged()
    }

}