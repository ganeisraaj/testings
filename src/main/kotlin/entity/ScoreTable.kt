package entity

/**
 * Represents the possible score categories of a player's card combination.
 *
 * The values correspond to standard poker hand rankings and are ordered
 * from lowest to highest strength. The ordering may be used for comparison
 * operations when determining the winning player.
 *
 * The available score categories are:
 * - HIGHCARD
 * - PAIR
 * - TWOPAIR
 * - SET
 * - STRAIGHT
 * - FLUSH
 * - FULLHOUSE
 * - FOUROFAKIND
 * - STRAIGHTFLUSH
 * - ROYALFLUSH
 */
enum class ScoreTable {
    NONE,
    HIGHCARD,
    PAIR,
    TWOPAIR,
    SET,
    STRAIGHT,
    FLUSH,
    FULLHOUSE,
    FOUROFAKIND,
    STRAIGHTFLUSH,
    ROYALFLUSH
}