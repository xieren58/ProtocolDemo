[TOC]

# 隐智科技电子棋盘

## 集成

- 工具包下载地址：

## 核心类

- BoardConnector：棋盘连接、发送消息、接受消息。
  - BoardConnectListener：监听棋盘是否连接成功。
  - BoardDataListener：监听底层返回的信息，只会返回格式正常的数据。
- BoardProtocol：棋盘底层的协议封装。
  - 目前除了拍钟为底层返回之外，其他指令都是主动下发。

## 连接

示例代码：

```kotlin
    //boardSize表示连接成功后要设置的棋盘路数
	private fun connectBoard(boardSize:Int = 19) {
        boardConnector = BoardConnector.Builder(this)
            .connectListener(this)
            .dataListener(this)
            .build()
        boardConnector.connect(boardSize)
    }
```

## 断开连接

示例代码：

```kotlin
boardConnector.write(BoardProtocol.disConnect())
boardConnector.callDestroy()
```

## 发送消息

通过boardConnector的write方法向底层发送命令。

示例代码：

```kotlin
boardConnector.write(BoardProtocol.allChess())
```

## 底层协议（常用部分）

底层命令以`~`开头，以`#`结尾。BoardProtocol类内置了当前支持的大部分命令。

### 指定当前对局为几路棋盘

下发指令：

```java
    /**
     * 发送指令指定当前对局为几路棋盘
     *
     * @param boardSize 棋盘路数
     * @return 底层指令
     */
    public static String boardSize(int boardSize) {
        //...
    }
```

### 主动请求全盘数据

下发指令：

```java
    /**
     * 请求全盘信息
     */
    public static String allChess() {
        //...
    }
```

返回数据：

```java
~SDA0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000#
```

- ~SDA：数据头，表示该数据是全盘信息
- 中间数字：实时的棋盘数据
  - 0表示该位置为空白，1表示该位置有黑子，2表示该位置有白子
  - 数字长度为设置的棋盘总子数，19路为361，9路为81，依次类推
  - 顺序以黑方左手边为第一个位置，向右依次递增
- #：数据尾，表示该条数据到此结束

### 点亮行棋指示灯

下发指令：

```java
    /**
     * 黑方白方的指示灯
     *
     * @param bw 1黑 2白
     */
    public static String lamp(int bw) {
       //...
    }
```

### 棋盘点亮灯

下发指令：

```java
    /**
     * 点亮棋盘某个位置指示灯，可以自定义颜色
     *
     * @param position 1-361，对应棋盘位置
     * @param colorR   红色值
     * @param colorG   绿色值
     * @param colorB   蓝色值
     */
    public static String lampPosition(int position, int colorR, int colorG, int colorB) {
        //...
    }
```

### 关闭所有指示灯

下发指令：

```java
    /**
     * 关闭所有指示灯
     */
    public static String closeAllLamp() {
        //...
    }
```

### 黑方拍钟

返回指令：

```java
    /**
     * 黑方拍钟
     */
    public static String clickBlack() {
        //...
    }
```

### 白方拍钟

返回指令：

```java
    /**
     * 白方拍钟
     */
    public static String clickWhite() {
        //...
    }
```

### 底层是否自动发送全盘数据

下发指令：

```java
    /**
     * 底层是否主动发全盘变化
     * @param send true表示棋盘发送变化时主动发送数据，反之false
     */
    public static String autoSendAllChess(boolean send) {
        //...
    }
```

### 读秒倒计时

下发指令：

```java
    /**
     * 读秒提示音
     */
    public static String secondWarning() {
        //...
    }
```

### 棋盘报警

下发指令：

```java
    /**
     * 底层发滴滴警告声音
     */
    public static String warning() {
       //...
    }
```

### 断开连接

下发指令：

```java
    /**
     * 断开连接
     */
    public static String disConnect() {
        //...
    }
```