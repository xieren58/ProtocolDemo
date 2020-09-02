package cn.izis.boardmonitor.protocol;

/**
 * 电子棋盘协议
 */
public class BoardProtocol {
    /**
     * 下发的指令
     */
    public static class Down{
        /**
         * 发送指令指定当前对局为几路棋盘
         *
         * @param boardSize 棋盘路数
         * @return 底层指令
         */
        public static String boardSize(int boardSize) {
            if (boardSize != 9 && boardSize != 13 && boardSize != 15 && boardSize != 19) {
                throw new RuntimeException("boardSize must be one of 9，13，15，19");
            }
            if (boardSize < 10) {
                return "~BOD0" + boardSize + "#";
            } else {
                return "~BOD" + boardSize + "#";
            }
        }

        /**
         * 请求全盘信息
         */
        public static String allChess() {
            return "~STA#";
        }

        /**
         * 黑方白方的指示灯
         *
         * @param bw 1黑 2白
         */
        public static String lamp(int bw) {
            return "~LED" + bw + "1#";
        }

        /**
         * 断开连接
         */
        public static String disConnect() {
            return "~CAL#";
        }

        /**
         * 底层发滴滴警告声音
         */
        public static String warning() {
            return "~AWO#";
        }

        /**
         * 关闭所有指示灯
         */
        public static String closeAllLamp() {
            return "~RGC#";
        }

        /**
         * 底层是否主动发全盘变化
         *
         * @param send true表示棋盘发送变化时主动发送数据，反之false
         */
        public static String autoSendAllChess(boolean send) {
            return "~CTS" + (send ? 1 : 0) + "#";
        }

        /**
         * 读秒提示音
         */
        public static String secondWarning() {
            return "~AWS#";
        }

        /**
         * 基础时间用完提示音
         */
        public static String baseTimeWarning() {
            return "~AWT#";
        }

        /**
         * 点亮棋盘某个位置指示灯
         *
         * @param position 1-361
         * @param colorR   红色值
         * @param colorG   绿色值
         * @param colorB   蓝色值
         */
        public static String lampPosition(int position, int colorR, int colorG, int colorB) {
            if (!inRange(position, 1, 361)) {
                throw new RuntimeException("position must be in 1..361");
            }
            if (!inRange(colorR, 1, 255) ||
                    !inRange(colorG, 1, 255) ||
                    !inRange(colorB, 1, 255)) {
                throw new RuntimeException("color must be in 1..255");
            }
            String p = intToString(position);
            String r = intToString(colorR);
            String g = intToString(colorG);
            String b = intToString(colorB);
            return "~SHP" + p + ",r" + r + "g" + g + "b" + b + ",1#";
        }

        /**
         * 同时亮多个指示灯
         *
         * @param positions 需要亮灯的位置，必须是361长串，格式为"001201000200"；
         *                  1表示亮绿灯  2表示亮红灯
         * @param lightType 亮灯的亮度。1表示低亮，2表示中亮，3表示高亮。只有在亮灯个数小于50的情况下有效
         */
        public static String lampMultiple(String positions, int lightType) {
            if (positions.length() != 361) {
                throw new RuntimeException("positions's length must be 361");
            }
            int num = 0;
            for (int i = 0; i < positions.length(); i++) {
                char c = positions.charAt(i);
                if (c != '0' && c != '1' && c != '2') {
                    throw new RuntimeException("positions's must be match of [0-2]{361}");
                }

                if (c != '0') {
                    num++;
                }
            }

            if (num >= 50) {
                return "~SAW" + positions + "#";
            } else {
                if (lightType == 3) {
                    return "~SAL" + positions + "#";
                } else if (lightType == 2) {
                    return "~SAM" + positions + "#";
                } else {
                    return "~SAW" + positions + "#";
                }
            }
        }

        /**
         * 棋盘显示一个对号（一般做题时使用）
         */
        public static String showRight() {
            return "~RLT#";
        }

        /**
         * 棋盘显示一个叉号（一般做题时使用）
         */
        public static String showError() {
            return "~RLW#";
        }

        /**
         * 棋盘显示一个OK（一般做题时使用）
         */
        public static String showOK() {
            return "~RLO#";
        }

        private static boolean inRange(int source, int min, int max) {
            return source >= min && source <= max;
        }

        private static String intToString(int position) {
            String p;
            if (position < 10)
                p = "00" + position;
            else if (position < 100)
                p = "0" + position;
            else
                p = String.valueOf(position);
            return p;
        }
    }

    /**
     * 底层反馈的指令
     */
    public static class Up{
        /**
         * 黑方拍钟
         */
        public static String clickBlack() {
            return "~BKY#";
        }

        /**
         * 白方拍钟
         */
        public static String clickWhite() {
            return "~WKY#";
        }

        /**
         * 黑方拍钟  双击
         */
        public static String doubleClickBlack() {
            return "~BTK#";
        }

        /**
         * 白方拍钟  双击
         */
        public static String doubleClickWhite() {
            return "~WTK#";
        }

    }
}
