package arcs.android.host

import android.content.Context
import arcs.android.host.prod.ProdArcHostService
import arcs.core.host.ParticleRegistration
import arcs.core.host.SchedulerProvider
import arcs.core.host.SimpleSchedulerProvider
import arcs.core.host.TestingJvmProdHost
import arcs.core.storage.StoreManager
import arcs.sdk.android.storage.ServiceStoreFactory
import arcs.sdk.android.storage.service.testutil.TestConnectionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
class TestProdArcHostService : ProdArcHostService() {
    override val coroutineContext = Dispatchers.Default
    override val arcSerializationCoroutineContext = Dispatchers.Default
    val schedulerProvider = SimpleSchedulerProvider(coroutineContext)

    override val arcHost = TestingAndroidProdHost(
        this,
        schedulerProvider
    )

    override val arcHosts = listOf(arcHost)

    override fun onDestroy() {
        runBlocking {
            arcHost.stores.reset()
        }
    }

    class TestingAndroidProdHost(
        val context: Context,
        schedulerProvider: SchedulerProvider,
        vararg particles: ParticleRegistration
    ) : TestingJvmProdHost(schedulerProvider, *particles) {

        @ExperimentalCoroutinesApi
        override val stores = StoreManager(
            activationFactory = ServiceStoreFactory(
                context,
                connectionFactory = TestConnectionFactory(context)
            )
        )
    }
}
