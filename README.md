
* [隐智科技电子棋盘](#%E9%9A%90%E6%99%BA%E7%A7%91%E6%8A%80%E7%94%B5%E5%AD%90%E6%A3%8B%E7%9B%98)
  * [集成](#%E9%9B%86%E6%88%90)
  * [核心类](#%E6%A0%B8%E5%BF%83%E7%B1%BB)
  * [连接](#%E8%BF%9E%E6%8E%A5)
  * [断开连接](#%E6%96%AD%E5%BC%80%E8%BF%9E%E6%8E%A5)
  * [发送消息](#%E5%8F%91%E9%80%81%E6%B6%88%E6%81%AF)
  * [底层协议（常用部分）](#%E5%BA%95%E5%B1%82%E5%8D%8F%E8%AE%AE%E5%B8%B8%E7%94%A8%E9%83%A8%E5%88%86)
    * [指定当前对局为几路棋盘](#%E6%8C%87%E5%AE%9A%E5%BD%93%E5%89%8D%E5%AF%B9%E5%B1%80%E4%B8%BA%E5%87%A0%E8%B7%AF%E6%A3%8B%E7%9B%98)
    * [主动请求全盘数据](#%E4%B8%BB%E5%8A%A8%E8%AF%B7%E6%B1%82%E5%85%A8%E7%9B%98%E6%95%B0%E6%8D%AE)
    * [点亮行棋指示灯](#%E7%82%B9%E4%BA%AE%E8%A1%8C%E6%A3%8B%E6%8C%87%E7%A4%BA%E7%81%AF)
    * [棋盘点亮灯](#%E6%A3%8B%E7%9B%98%E7%82%B9%E4%BA%AE%E7%81%AF)
    * [关闭所有指示灯](#%E5%85%B3%E9%97%AD%E6%89%80%E6%9C%89%E6%8C%87%E7%A4%BA%E7%81%AF)
    * [黑方拍钟](#%E9%BB%91%E6%96%B9%E6%8B%8D%E9%92%9F)
    * [白方拍钟](#%E7%99%BD%E6%96%B9%E6%8B%8D%E9%92%9F)
    * [底层是否自动发送全盘数据](#%E5%BA%95%E5%B1%82%E6%98%AF%E5%90%A6%E8%87%AA%E5%8A%A8%E5%8F%91%E9%80%81%E5%85%A8%E7%9B%98%E6%95%B0%E6%8D%AE)
    * [读秒倒计时](#%E8%AF%BB%E7%A7%92%E5%80%92%E8%AE%A1%E6%97%B6)
    * [棋盘报警](#%E6%A3%8B%E7%9B%98%E6%8A%A5%E8%AD%A6)
    * [断开连接](#%E6%96%AD%E5%BC%80%E8%BF%9E%E6%8E%A5-1)

# 隐智科技电子棋盘

## 集成

- demo app下载：[https://github.com/yzkj2213/ProtocolDemo/tree/master/app/apk](https://github.com/yzkj2213/ProtocolDemo/tree/master/app/apk)
- 开发工具包下载地址：
    - [v0.1](http://app.izis.cn/GoWebService/boardmonitor_v0.1.aar)

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
  - 顺序以黑方左手边为第一个位置，向右依次递增，每行都是从左边开始
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