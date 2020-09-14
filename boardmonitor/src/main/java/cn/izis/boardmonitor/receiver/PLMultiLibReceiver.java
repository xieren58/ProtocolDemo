package cn.izis.boardmonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.izis.boardmonitor.listener.BoardConnectListener;
import tw.com.prolific.pl2303multilib.PL2303MultiLib;


public class PLMultiLibReceiver extends BroadcastReceiver {
    private PL2303MultiLib mSerialMulti;
    private BoardConnectListener mConnectListener;
    private int deviceIndex;

    public PLMultiLibReceiver(PL2303MultiLib mSerialMulti, BoardConnectListener mConnectListener, int deviceIndex) {
        this.mSerialMulti = mSerialMulti;
        this.mConnectListener = mConnectListener;
        this.deviceIndex = deviceIndex;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!=null && intent.getAction().equals(mSerialMulti.PLUART_MESSAGE)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String str = (String) extras.get(mSerialMulti.PLUART_DETACHED);
                try {
                    if (str == null) return;
                    int index = Integer.parseInt(str);
                    if (deviceIndex == index) {
                        mConnectListener.onBoardDisConnect();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
}
