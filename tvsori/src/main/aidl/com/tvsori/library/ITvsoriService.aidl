package com.tvsori.library;

import android.os.Bundle;


interface ITvsoriService {
    oneway void onEnterBroadcastRoom(boolean isSucc, long roomId, String errorMessage);
}
