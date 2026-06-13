package com.mhss.app.mybrain.sync.util

import com.mhss.app.mybrain.sync.model.SyncTriggerMessage

const val DEFAULT_SYNC_PORT = 38300

const val ROUTE_PING = "/ping"
const val ROUTE_SYNC = "/sync"

const val PARAM_DEVICE_ID = "deviceId"
const val PARAM_IPS = "ips"
const val PARAM_PORT = "port"
const val PARAM_ENC_KEY = "encKey"
const val SYNC_DEEP_LINK_BASE_URI = "mybrain://sync"
const val SYNC_DEEP_LINK_PATTERN = "$SYNC_DEEP_LINK_BASE_URI?$PARAM_DEVICE_ID={$PARAM_DEVICE_ID}&$PARAM_IPS={$PARAM_IPS}&$PARAM_PORT={$PARAM_PORT}&$PARAM_ENC_KEY={$PARAM_ENC_KEY}"
val SYNC_TRIGGER_MESSAGE =
    SyncTriggerMessage
