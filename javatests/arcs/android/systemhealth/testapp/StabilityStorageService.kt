package arcs.android.systemhealth.testapp

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import arcs.android.storage.ParcelableStoreOptions
import arcs.android.systemhealth.testapp.Dispatchers as ArcsDispatchers
import arcs.android.systemhealth.testapp.Executors as ArcsExecutors
import arcs.sdk.android.storage.service.StorageService
import arcs.sdk.android.storage.service.StorageServiceBindingDelegate
import kotlin.random.Random
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Arcs system-health-test storage service. Supports crashing itself when needed.
 */
class StabilityStorageService : StorageService() {
    override val coroutineContext =
        ArcsDispatchers.server + CoroutineName("StabilityStorageService")
    override val writeBackScope: CoroutineScope = CoroutineScope(
        ArcsExecutors.io.asCoroutineDispatcher() + SupervisorJob()
    )
    private val scope = CoroutineScope(coroutineContext)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null &&
            intent.hasExtra(EXTRA_CRASH) &&
            intent.getBooleanExtra(EXTRA_CRASH, false)) {
            scope.launch {
                delay(Random.nextLong(10, 1000))
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CRASH = "crash"

        fun createBindIntent(context: Context, storeOptions: ParcelableStoreOptions): Intent =
            Intent(context, StabilityStorageService::class.java).apply {
                action = storeOptions.actual.storageKey.toString()
                putExtra(EXTRA_OPTIONS, storeOptions)
            }

        fun createCrashIntent(context: Context): Intent =
            Intent(context, StabilityStorageService::class.java).apply {
                putExtra(EXTRA_CRASH, true)
            }
    }
}

/** Implementation of the [StorageServiceBindingDelegate] which uses [StabilityStorageService]. */
class StabilityStorageServiceBindingDelegate(
    private val context: Context
) : StorageServiceBindingDelegate {
    @Suppress("NAME_SHADOWING")
    override fun bindStorageService(
        conn: ServiceConnection,
        flags: Int,
        options: ParcelableStoreOptions?
    ): Boolean {
        val options = requireNotNull(options) {
            "ParcelableStoreOptions are required when binding to " +
                "the StabilityStorageService from a ServiceStore."
        }
        return context.bindService(
            StabilityStorageService.createBindIntent(context, options), conn, flags)
    }

    override fun unbindStorageService(conn: ServiceConnection) =
        context.unbindService(conn)
}
