package eu.thomaskuenneth.codescantoclipboard

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class CodeScanToClipboardTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile.run {
            state = Tile.STATE_INACTIVE
            this.
            updateTile()
        }
    }

    override fun onClick() {
        val resultIntent = Intent(this, CodeScanToClipboardActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val resultPendingIntent: PendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(resultPendingIntent)
        } else {
            startActivityAndCollapse(resultIntent)
        }
    }
}
