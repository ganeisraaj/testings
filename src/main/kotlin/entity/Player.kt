package entity

data class Player(
    val name: String,
    var actionsLeft: Int,
    var score: ScoreTable = ScoreTable.HIGHCARD,
    val hiddenCards: MutableList<Card> = mutableListOf(),
    val openCards: MutableList<Card> = mutableListOf()
)