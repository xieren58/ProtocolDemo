package cn.izis.boardmonitor.core;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import cn.izis.boardmonitor.listener.BoardConnectListener;
import cn.izis.boardmonitor.listener.BoardDataListener;
import cn.izis.boardmonitor.protocol.BoardProtocol;
import cn.izis.boardmonitor.receiver.PLMultiLibReceiver;
import tw.com.prolific.pl2303multilib.PL2303MultiLib;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 棋盘连接
 */
public class BoardConnector {

    /**
     * 处理的返回数据种类
     */
    public enum ReceiveType {
        /**
         * 处理返回的所有以~开头，#结尾的数据  ~?[^~#]*#
         */
        ALL("~?[^~#]*#"),
        /**
         * 只处理符合标准的数据  ~?[A-Z]{3}[^~#]*#
         */
        STANDARD("~?[A-Z]{3}[^~#]*#");

        public final String regex;

        ReceiveType(String regex) {
            this.regex = regex;
        }
    }

    private static final String ACTION_USB_PERMISSION = "cn.izis.boardmonitor.core.BoardConnector.USB_PERMISSION";
    private static final int deviceIndex = 0;
    static boolean mDebug = false;
    private int delayTime = 80;         // 50 尝试改成30试试，处理得太慢，缓冲区信息被挤掉？  // 40改100
    private ReceiveType receiveType = ReceiveType.STANDARD;
    private PL2303MultiLib.BaudRate mBaudRate = PL2303MultiLib.BaudRate.B115200;
    private PL2303MultiLib.DataBits mDataBits = PL2303MultiLib.DataBits.D8;
    private PL2303MultiLib.Parity mParity = PL2303MultiLib.Parity.NONE;
    private PL2303MultiLib.StopBits mStopBits = PL2303MultiLib.StopBits.S1;
    private PL2303MultiLib.FlowControl mFlowControl = PL2303MultiLib.FlowControl.XONXOFF;  // 是否有效？
    private PL2303MultiLib mSerialMulti;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Context mContext;
    private BoardConnectListener mConnectListener;
    private BoardDataListener mDataListener;
    private PLMultiLibReceiver mPlMultiLibReceiver;

    private BoardConnector(Context context) {
        this.mContext = context;
    }

    private void log(String message) {
        if (mDebug)
            Log.d("===隐智电子棋盘===：", message);
    }

    /**
     * 连接电子棋盘
     *
     * @param boardSize 当前对局棋盘路数
     */
    public void connect(final int boardSize) {

        if (mSerialMulti == null) {
            mSerialMulti = new PL2303MultiLib((UsbManager) mContext.getSystemService(Context.USB_SERVICE), mContext, ACTION_USB_PERMISSION);//2018-5-31修改
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (pl2303LinkExist()) return;

                mSerialMulti.PL2303Enumerate();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        boolean res = mSerialMulti.PL2303OpenDevByUARTSetting(deviceIndex, mBaudRate, mDataBits, mStopBits, mParity, mFlowControl);
                        if (!res) {
                            log("打开连接失败");
                            if (mConnectListener != null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mConnectListener.onConnectBoardFail();
                                    }
                                });
                            }
                        } else {
                            log("打开" + mSerialMulti.PL2303getDevicePathByIndex(deviceIndex) + "成功!");

                            IntentFilter filter = new IntentFilter();
                            filter.addAction(mSerialMulti.PLUART_MESSAGE);
                            mPlMultiLibReceiver = new PLMultiLibReceiver(mSerialMulti, mConnectListener, deviceIndex);
                            mContext.registerReceiver(mPlMultiLibReceiver, filter);

                            writeCode(BoardProtocol.Down.boardSize(boardSize));
                            if (mConnectListener != null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mConnectListener.onConnectBoardSuccess();
                                    }
                                });
                            }
                            requestData();
                        }
                    }
                }, 300);
            }
        }, 300);
    }

    private void requestData() {
        executorService.submit(new Runnable() {
            private byte[] readByte = new byte[512]; // 4096
            private String readHub = ""; // 存储接受到的信息，全局变量

            @Override
            public void run() {
                while (true) {
                    sleep(40);

                    if (mSerialMulti != null) {
                        int readLen = 0;//一次读取到的数据长度
                        try {
                            readLen = mSerialMulti.PL2303Read(deviceIndex, readByte); // 从底层读取数据
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (readLen > 0) {
                            StringBuilder readData = new StringBuilder();
                            for (int j = 0; j < readLen; j++) {
                                readData.append((char) (readByte[j] & 0x000000FF));
                            }

                            String curReadData = readData.toString(); // 将获取的棋盘数据转换成字符串

                            log("===之前缓存数据:" + readHub);
                            log("===本次读取数据:" + curReadData);

                            readHub = String.format("%s%s", readHub, curReadData); // 得到最完整的读取池数据

                            int firstStartCharIndex = readHub.indexOf("~");

                            if (firstStartCharIndex > 0) {// 总池子不是以“~”开头，有故障。将开头数据摒弃。（-1，0，>0）
                                readHub = readHub.substring(firstStartCharIndex);
                            }

                            String totalCommands;
                            //开头必定为~号
                            //缓存中完整数据后的半截数据
                            String tempRest;
                            if (readHub.lastIndexOf("#") > 0) {//包含了#号（不包含则说明不包含一个完整的指令）
                                if (readHub.lastIndexOf("#") < readHub.length() - 1) {//包含了部分下一条指令
                                    tempRest = readHub.substring(readHub.lastIndexOf("#") + 1); // 得到最后的#号之后的半截数据
                                } else {
                                    tempRest = "";
                                }

                                totalCommands = readHub.substring(0, readHub.lastIndexOf("#") + 1); // 完整的指令集合
                            } else { // 不包含结束符号，说明指令尚不完整，继续等待下一帧。
                                continue; // 继续下一次循环
                            }

                            log("===返回指令，可能包含多个:" + totalCommands);

                            // ==================包含#号
                            // 解析数据，分解成一条条指令后交给前台调用者处理===================
                            Pattern r = Pattern.compile(receiveType.regex);
                            Matcher m = r.matcher(totalCommands);
                            LinkedHashSet<String> set = new LinkedHashSet<>();
                            while (m.find()) {
                                String group = m.group();
                                set.add(group);
                            }
                            log("===得到有效指令:" + set.toString());
                            for (String s : set) {
                                if (mDataListener != null)
                                    mDataListener.onReadData(s);// 刚好一条完整指令，则直接通知前台去处理即可
                            }

                            readHub = tempRest;
                        }
                    }
                }
            }
        });
    }

    private void sleep(long delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void callDestroy() {
        if (mSerialMulti != null) {
            if (mPlMultiLibReceiver != null)
                mContext.unregisterReceiver(mPlMultiLibReceiver);
            mSerialMulti.PL2303Release();
            mSerialMulti = null;
        }
        executorService.shutdown();
    }

    /**
     * 写入数据
     */
    public void writeCode(String code) {
        if (!pl2303LinkExist() || TextUtils.isEmpty(code)) {
            if (!pl2303LinkExist()) {
                if (mConnectListener != null)
                    mConnectListener.onBoardDisConnect();
            }
            return;
        }

        sleep(delayTime);

        if (mSerialMulti != null) {
            mSerialMulti.PL2303Write(deviceIndex, code.getBytes());
            log("写入指令：" + code);
        }
    }

    /**
     * 写入文件
     *
     * @param path 文件路径
     */
    public void writeFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (mSerialMulti != null) {
            log("写入文件：" + path);
            try {
                byte[] data = toByteArray(file);
                int max = data.length / 1024 + 1;
                for (int i = 0; i < max; i++) {
                    int length = Math.min(data.length - i * 1024, 1024);
                    byte[] msg = new byte[length];
                    System.arraycopy(data, i * 1024, msg, 0, length);
                    mSerialMulti.PL2303Write(deviceIndex, msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] toByteArray(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("file not exists");
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length())) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean pl2303LinkExist() {
        return mSerialMulti != null && mSerialMulti.PL2303IsDeviceConnectedByIndex(deviceIndex);
    }

    public static class Builder implements BoardConnectorBuilder<Builder, BoardConnector> {
        private BoardConnector boardConnector;

        public Builder(Context context) {
            boardConnector = new BoardConnector(context);
        }

        @Override
        public Builder debug(boolean debug) {
            mDebug = debug;
            return this;
        }

        @Override
        public Builder delayTime(int time) {
            boardConnector.delayTime = time;
            return this;
        }

        @Override
        public Builder baudRate(PL2303MultiLib.BaudRate baudRate) {
            boardConnector.mBaudRate = baudRate;
            return this;
        }

        @Override
        public Builder dataBits(PL2303MultiLib.DataBits dataBits) {
            boardConnector.mDataBits = dataBits;
            return this;
        }

        @Override
        public Builder parity(PL2303MultiLib.Parity parity) {
            boardConnector.mParity = parity;
            return this;
        }

        @Override
        public Builder stopBits(PL2303MultiLib.StopBits stopBits) {
            boardConnector.mStopBits = stopBits;
            return this;
        }

        @Override
        public Builder flowControl(PL2303MultiLib.FlowControl flowControl) {
            boardConnector.mFlowControl = flowControl;
            return null;
        }

        @Override
        public Builder receiveType(ReceiveType receiveType) {
            boardConnector.receiveType = receiveType;
            return this;
        }

        @Override
        public Builder connectListener(BoardConnectListener listener) {
            boardConnector.mConnectListener = listener;
            return this;
        }

        @Override
        public Builder dataListener(BoardDataListener listener) {
            boardConnector.mDataListener = listener;
            return this;
        }

        @Override
        public BoardConnector build() {
            return boardConnector;
        }
    }
}
