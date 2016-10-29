package uk.co.droidcon.hack.bstf

class BstfComponent {

    companion object {

        var gameManager: BstfGameManager? = null

        fun setBstfGameManager(gameManager: BstfGameManager) {
            this.gameManager = gameManager
            gameManager.startGame()
        }

        fun getBstfGameManager() : BstfGameManager {
            if (gameManager == null) {
                throw UnsupportedOperationException("the bstf game manager has not been set yet!")
            }

            return gameManager!!
        }
    }
}