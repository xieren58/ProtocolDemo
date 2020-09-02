package cn.izis.boardmonitor.listener;

import androidx.annotation.NonNull;

/**
 * 棋盘数据监听
 */
public interface BoardDataListener {
    void onReadData(@NonNull String readData);
}
