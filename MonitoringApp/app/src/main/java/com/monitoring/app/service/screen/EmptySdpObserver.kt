package com.monitoring.app.service.screen

import com.monitoring.app.utils.LogUtils
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class EmptySdpObserver : SdpObserver {
    companion object {
        const val TAG = "EmptySdpObserver"
    }

    override fun onCreateSuccess(p0: SessionDescription?) {
        TODO("Not yet implemented")
    }

    override fun onSetSuccess() {
        TODO("Not yet implemented")
    }

    override fun onCreateFailure(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onSetFailure(p0: String?) {
        LogUtils.e(TAG, "onSetFailure")
    }
}
