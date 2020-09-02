package cn.izis.boardmonitor.listener;

/**
 * 棋盘连接监听
 */
public interface BoardConnectListener {
    /**
     * 连接成功
     */
    void onConnectBoardSuccess();

    /**
     * 连接失败
     */
    void onConnectBoardFail();

    /**
     * 连接断开
     *
     * 有可能会触发多次
     */
    void onBoardDisConnect();
}
