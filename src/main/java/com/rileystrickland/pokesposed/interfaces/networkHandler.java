package com.rileystrickland.pokesposed.interfaces;

import android.os.Handler;
import android.os.Message;
import com.rileystrickland.pokesposed.service.messageCode;

//TODO Move Handler into generic class and have application use event based programming (e.g. onConnected)
public class networkHandler extends Handler {

    private networkListener networklistener;

    public void networkHandler(networkListener networklistener)
    {
        this.networklistener = networklistener;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {

            case messageCode.MSG_CON:
                networklistener.networkConnected(message.replyTo);
                break;
            case messageCode.MSG_DSC:
                networklistener.networkDisconnected(message.replyTo);
                break;
        }
    }

}
