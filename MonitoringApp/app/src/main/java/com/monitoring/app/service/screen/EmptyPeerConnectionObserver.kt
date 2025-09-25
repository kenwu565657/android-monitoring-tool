package com.monitoring.app.service.screen

import android.util.Log
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver

open class EmptyPeerConnectionObserver : PeerConnection.Observer {
    private val TAG = "PeerConnectionObserver"

    override fun onSignalingChange(state: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChange: $state")
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange: $state")
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange: $receiving")
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChange: $state")
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        Log.d(TAG, "onIceCandidate: $candidate")
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
        Log.d(TAG, "onIceCandidatesRemoved: ${candidates?.size}")
    }

    override fun onAddStream(stream: MediaStream?) {
        Log.d(TAG, "onAddStream: $stream")
    }

    override fun onRemoveStream(stream: MediaStream?) {
        Log.d(TAG, "onRemoveStream: $stream")
    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        Log.d(TAG, "onDataChannel: $dataChannel")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded")
    }

    override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
        Log.d(TAG, "onAddTrack: receiver=$receiver, streams=${streams?.size}")
    }
}