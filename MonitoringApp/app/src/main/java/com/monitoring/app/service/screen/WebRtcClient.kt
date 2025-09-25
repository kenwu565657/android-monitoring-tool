package com.monitoring.app.service.screen

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.google.gson.Gson
import com.monitoring.app.MyApplication
import com.monitoring.app.model.WebSocketMessage
import com.monitoring.app.model.WebSocketMessageHeader
import com.monitoring.app.service.websocket.WebsocketContextHolder
import com.monitoring.app.utils.JsonUtils
import com.monitoring.app.utils.LogUtils
import org.webrtc.*

class WebRtcClient private constructor() {

    private val gson = Gson()
    private val eglBaseContext = EglBase.create().eglBaseContext
    private val mediaConstraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private var screenCapturer: VideoCapturer? = null
    private var localVideoTrack: VideoTrack? = null
    private var localStream: MediaStream? = null
    private val localTrackId = "local_video_track"
    private val localStreamId = "local_video_stream"

    private var peerConnection: PeerConnection? = null
    private val iceServer = listOf(
        PeerConnection.IceServer(
            "turn:10.0.2.2:3478",
            "myuser",
            "mypassword"
        )
    )

    private var remoteVideoView: SurfaceViewRenderer? = null
    private var surfaceViewRenderer: SurfaceViewRenderer? = null

    private val peerConnectionFactory by lazy {
        createPeerConnectionFactory()
    }

    fun initializeWebRtcClient(view: SurfaceViewRenderer, observer: PeerConnection.Observer) {
        peerConnection = createPeerConnection(observer)
        initSurfaceView(view)
    }

    fun startScreenSharing(view: SurfaceViewRenderer) {
        val displayMetrics = DisplayMetrics()
        val windowsManager = MyApplication.context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)

        val surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, eglBaseContext
        )

        screenCapturer = createScreenSharingHelper()

        val localVideoSource = peerConnectionFactory.createVideoSource(screenCapturer!!.isScreencast)

        screenCapturer!!.initialize(
            surfaceTextureHelper,
            MyApplication.context,
            localVideoSource.capturerObserver
        )
        screenCapturer!!.startCapture(320, 320, 10)

        localVideoTrack = peerConnectionFactory.createVideoTrack(localTrackId,localVideoSource)
        localVideoTrack!!.addSink(view)
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId)
        localStream!!.addTrack(localVideoTrack)
        peerConnection!!.addStream(localStream)
    }

    private fun createScreenSharingHelper(): VideoCapturer {
        return ScreenCapturerAndroid(screenPermissionIntent, object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
            }
        })
    }

    private fun initSurfaceView(surfaceViewRenderer: SurfaceViewRenderer) {
        this.surfaceViewRenderer = surfaceViewRenderer
        surfaceViewRenderer.run {
            setMirror(false)
            setEnableHardwareScaler(true)
            init(eglBaseContext,null)
        }
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = false
                disableNetworkMonitor = false
            })
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val peerConnectionObserver = object : EmptyPeerConnectionObserver() {
            override fun onIceCandidate(candidate: IceCandidate?) {
                observer.onIceCandidate(candidate)
            }

            override fun onAddStream(stream: MediaStream?) {
                stream?.videoTracks?.firstOrNull()?.let { videoTrack ->
                    android.util.Log.i("WebRtcClient", "Received remote video track")

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        videoTrack.addSink(surfaceViewRenderer)
                    }
                }
            }

            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                receiver?.track()?.let { track ->
                    if (track is VideoTrack) {
                        android.util.Log.i("WebRtcClient", "Received video track via onAddTrack")

                        if (surfaceViewRenderer != null) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                track.addSink(surfaceViewRenderer)
                            }
                        } else {
                            android.util.Log.e("WebRtcClient", "remoteVideoView not initialized, cannot add video track")
                        }
                    }
                }
            }
        }

        return peerConnectionFactory.createPeerConnection(
            iceServer,
            peerConnectionObserver
        )
    }

    fun createOffer(target: String) {
        peerConnection?.createOffer(
            object : EmptySdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription?) {
                    peerConnection?.setLocalDescription(object : EmptySdpObserver() {
                        override fun onSetSuccess() {
                            val sdpWithFingerprint = desc?.description
                            WebsocketContextHolder.sendMessage(
                                WebSocketMessage(
                                    header = WebSocketMessageHeader.WEBRTC_OFFER,
                                    destinationConnectionId = target,
                                    sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
                                    body = gson.toJsonTree(sdpWithFingerprint?: JsonUtils.EMPTY_JSON_STRING)
                                )
                            )
                        }

                        override fun onSetFailure(error: String?) {
                            LogUtils.e(TAG, "Set Local Description Failed: $error")
                        }

                    }, desc)
                }
            },
            mediaConstraint
        )
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription?) {
        peerConnection?.setRemoteDescription(object : EmptySdpObserver() {
            override fun onSetSuccess() {
                LogUtils.i(TAG, "Set Remote Description Success")
            }

            override fun onSetFailure(error: String?) {
                LogUtils.e(TAG, "Set Remote Description Failed: $error")
            }

        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        try {
            if (iceCandidate.sdpMid != null && iceCandidate.sdp != null) {
                peerConnection?.addIceCandidate(iceCandidate)
            } else {
                android.util.Log.e("WebRtcClient", "Invalid ICE candidate: ${iceCandidate.sdp}")
            }
        } catch (e: Exception) {
            android.util.Log.e("WebRtcClient", "Failed to add ICE candidate: ${e.message}")
            e.printStackTrace()
        }
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, target: String) {
        addIceCandidate(iceCandidate)
        WebsocketContextHolder.sendMessage(
            WebSocketMessage(
                header = WebSocketMessageHeader.WEBRTC_ICE_CANDIDATE,
                destinationConnectionId = null,
                sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
                body = gson.toJsonTree(iceCandidate)
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: WebRtcClient? = null

        fun getInstance(): WebRtcClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebRtcClient().also { INSTANCE = it }
            }
        }

        var surfaceView:SurfaceViewRenderer?=null
        var screenPermissionIntent : Intent ?= null

        private const val TAG = "WebRtcClient"
    }
}