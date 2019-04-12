package com.tvsori.library;

import android.os.Bundle;


interface ITvsoriServiceCallback {
    oneway void onEnterBroadcastRoom(boolean isSucc, long roomId, String errorMessage);
}
