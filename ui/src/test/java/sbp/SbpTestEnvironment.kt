package sbp

import kotlinx.coroutines.*
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.payment.SbpPaymentProcess
import ru.tinkoff.acquiring.sdk.redesign.sbp.ui.SbpPaymentViewModel
import ru.tinkoff.acquiring.sdk.redesign.sbp.util.NspkBankProvider
import ru.tinkoff.acquiring.sdk.redesign.sbp.util.SbpBankAppsProvider
import ru.tinkoff.acquiring.sdk.requests.GetQrRequest
import ru.tinkoff.acquiring.sdk.requests.GetStateRequest
import ru.tinkoff.acquiring.sdk.requests.InitRequest
import ru.tinkoff.acquiring.sdk.requests.performSuspendRequest
import ru.tinkoff.acquiring.sdk.responses.GetQrResponse
import ru.tinkoff.acquiring.sdk.responses.InitResponse
import ru.tinkoff.acquiring.sdk.utils.ConnectionChecker
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager

val nspkApps = setOf("ru.nspk.sbpay")


/**
 * Created by i.golovachev
 */
internal class SbpTestEnvironment(
    val connectionChecker: ConnectionChecker = mock {
        on { isOnline() } doReturn true
    },
    val bankAppsProvider: SbpBankAppsProvider = SbpBankAppsProvider { _, _ -> nspkApps.toList() },
    val nspkBankProvider: NspkBankProvider = NspkBankProvider { nspkApps },

    // env
    val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    val processJob: Job = SupervisorJob(),
    val paymentId: Long = 1,
    val deeplink: String = "https://qr.nspk.ru/test_link",


    // requests
    val initRequest: InitRequest = mock(),
    val getQrRequest: GetQrRequest = mock(),
    val getState: GetStateRequest = mock()
) {
    val sdk: AcquiringSdk = mock {
        on { init(any()) } doReturn initRequest
        on { getQr(any()) } doReturn getQrRequest
        on { getState(any()) } doReturn getState
    }

    val sbpPaymentProgress = SbpPaymentProcess(sdk, bankAppsProvider, nspkBankProvider, CoroutineScope( dispatcher + processJob))
    val viewModel: SbpPaymentViewModel = SbpPaymentViewModel(
        connectionChecker,
        sbpPaymentProgress,
        CoroutineManager(dispatcher, dispatcher)
    )

    suspend fun setInitResult(throwable: Throwable) {
        val result = Result.failure<InitResponse>(throwable)

        whenever(initRequest.performSuspendRequest())
            .doReturn(result)
    }

    suspend fun setInitResult(definePaymentId: Long? = null) {
        val response = InitResponse(paymentId = definePaymentId)
        val result = Result.success(response)

        whenever(initRequest.performSuspendRequest())
            .doReturn(result)
    }

    suspend fun setGetQrResult(throwable: Throwable) {
        val result = Result.failure<GetQrResponse>(throwable)

        whenever(getQrRequest.performSuspendRequest())
            .doReturn(result)
    }

    suspend fun setGetQrResult(deeplink: String) {
        val response = GetQrResponse(data = deeplink)
        val result = Result.success(response)

        whenever(getQrRequest.performSuspendRequest())
            .doReturn(result)
    }
}

internal fun SbpTestEnvironment.runWithEnv(
    given: suspend SbpTestEnvironment.() -> Unit,
    `when`: suspend SbpTestEnvironment.() -> Unit,
    then: suspend SbpTestEnvironment.() -> Unit
) {
    runBlocking {
        launch { given.invoke(this@runWithEnv) }.join()
        launch { `when`.invoke(this@runWithEnv) }.join()
        launch { then.invoke(this@runWithEnv) }.join()
    }
}
