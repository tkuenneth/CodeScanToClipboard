package eu.thomaskuenneth.codescantoclipboard

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService

class CodeScanToClipboardTileService : TileService() {

    override fun onClick() {
        val resultIntent = Intent(this, CodeScanToClipboardActivity::class.java)
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
