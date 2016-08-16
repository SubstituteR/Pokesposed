package com.rileystrickland.pokesposed;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import com.google.android.gms.maps.model.LatLng;
import com.rileystrickland.pokesposed.service.messageCode;

public class networkCommands {

    public static boolean send(Messenger messenger, Message message)
    {
        try {
            messenger.send(message);
            return true;
        }catch (Throwable e){
            return false;
        }
    }

    public static boolean sendNewLatLng(Messenger messenger, LatLng latlng)
    {
        Message message = Message.obtain(null, messageCode.MSG_ADD_LL);
        Bundle bundle = new Bundle();
        bundle.putDouble("x", latlng.latitude);
        bundle.putDouble("y", latlng.longitude);
        message.setData(bundle);
        return send(messenger, message);
    }

    public static boolean sendClearLatLng(Messenger messenger)
    {
        Message message = Message.obtain(null, messageCode.MSG_CLEAR_LL);
        return send(messenger, message);
    }

    public static boolean sendUndoLatLng(Messenger messenger)
    {
        Message message = Message.obtain(null, messageCode.MSG_UNDO_LL);
        return send(messenger, message);
    }

    public static boolean sendFirstCleared(Messenger messenger)
    {
        Message message = Message.obtain(null, messageCode.MSG_DEL_LL);
        return send(messenger, message);
    }

    public static boolean sendFirstCycled(Messenger messenger)
    {
        Message message = Message.obtain(null, messageCode.MSG_CYC_LL);
        return send(messenger, message);
    }

    public static boolean sendPosition(Messenger messenger, LatLng position, float bearing)
    {
        Message message = Message.obtain(null, messageCode.MSG_POS);
        Bundle bundle = new Bundle();
        bundle.putDouble("x", position.latitude);
        bundle.putDouble("y",  position.longitude);
        bundle.putFloat("b", bearing);
        message.setData(bundle);
        return send(messenger, message);
    }

    public static boolean sendDisconnect(Messenger messenger)
    {
        Message message = Message.obtain(null, messageCode.MSG_DSC);
        return send(messenger, message);
    }

    public static boolean sendPlayPause(Messenger messenger)
    {
        Message message = Message.obtain(null, messageCode.MSG_PP);
        return send(messenger, message);
    }


    public static boolean sendPrefernces(Messenger messenger, Bundle bundle)
    {
        Message message = Message.obtain(null, messageCode.MSG_PREF);
        message.setData(bundle);
        return send(messenger, message);
    }
}
