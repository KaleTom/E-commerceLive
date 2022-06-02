现在各大互联网`APP`都标配电商直播带货了，没有直播带货开发经验都感觉自己跟不上技术的进步。今天快速实现一个直播带货`APP`，深入理解整个直播带货开发流程。我们最终实现效果如下：
![最终效果](最终效果.gif)
 

按照惯例，为了快速实现，我们继续基于即构实时通话`SDK`进行开发。在正式开发之前，我们先理一下直播带货开发流程。 

> 1. 初始化`即构SDK`，
> 2. 房主创建房间`ID`，并进入房间。 观众根据房间`ID`进入房间
> 3. 房主推实时视频流，观众拉实时视频流


注意，我们只实现直播带货功能，具体的商品详情、支付等暂时不去实现。

# 1 准备工作


## 1.1 集成即构实时音视频SDK

`SDK`集成方式请直接参考官方文档[https://doc-zh.zego.im/article/195](https://doc-zh.zego.im/article/195), 这里不过多描述。

## 1.2 初始化SDK引擎
这里我们把所有调用即构`SDK`的`API`封装到`Zego`类中，并使用单例模式调用。其中初始化`SDK`引擎工作放入到构造函数中：
```java
private Zego(Application app) {
    ZegoEngineProfile profile = new ZegoEngineProfile();
    profile.appID = KeyCenter.APPID;
    profile.scenario = ZegoScenario.GENERAL;  // 通用场景接入
    profile.application = app;
    mEngine = ZegoExpressEngine.createEngine(profile, null);
}
```
这里有`APPID`参数需要前往控制台[https://console.zego.im](https://console.zego.im)创建一个项目获取。

# 2 创建房间与登录房间

登录画面如下：

![创建房间与登录房间](创建房间与登录房间.jpg)


## 2.1 房主创建房间
### 2.1.1 验证房间ID的有效性
在创建房间之前，需要房主提供房间号，当然了，这一步可以由后台自动生成。作为一个`Demo`，我们暂时让房主自己设置。为了避免房间号冲突，我们需要先验证当前房间号是否已存在，如果有个人服务器自然很轻松判断。如果没有个人服务器，可以调用即构提供的服务器端API接口查询当前房间的人数，具体调用方法可以前往[https://doc-zh.zego.im/article/8780](https://doc-zh.zego.im/article/8780)查询，也可以直接参考复用本文提供的源码。
> 简单来说，使用服务器端`API`就是访问一个`http`址，返回对应的`JSON`参数。

### 2.1.2 使用ID创建房间并登录
我们将登录房间函数封装到`Zego`类里面。登录房间代码如下。
> 注意，无须显式创建房间，如果指定的房间ID不存在，则会自动创建。
```java
public boolean loginRoom(String userId, String userName, String roomId, String token) {
    ZegoUser user = new ZegoUser(userId, userName);
    ZegoRoomConfig config = new ZegoRoomConfig();
    config.token = token; // 请求开发者服务端获取
    config.isUserStatusNotify = true;
    mEngine.loginRoom(roomId, user, config);
    return true;
}
```
注意到此函数需要传入`token`参数。`token`参数是采用**对称算法**生成。其大致原理如下：
> 1. 生成一个随机数，并将**有效时长**等其他相关参数，按照固定格式排列得到未加密版的`token`。
> 2. 使用密钥（在即构官方控制台中获取，每个APPID对应一个密钥）并使用对称加密算法加密，得到加密版的`token`，这个`token`是给客户端登录时使用的。

具体的代码实现操作请参考文末提供的源码，这里不再过多描述。

需要注意的是，为了安全考虑，`token`的生成操作最好放到个人服务器中，以免泄露密钥。


## 2.2 观众登录房间
观众登录房间的方式与2.1.2中描述的一致，实现代码也一致，即房主与观众可以复用同一套登录函数。

# 3 推流与拉流


## 3.1 房主推流

房主进入房间后，需要做如下事情：
> 1. 申请摄像头、语音的手机权限。
> 2. 开启摄像头，本地预览画面。
> 3. 推流。将本地实时画面推向即构服务器，由即构服务器做直播流数据分发。

申请摄像头等权限这里不描述，不清楚的可以直接查看文末源码或相关文档。

## 3.1.1 开启摄像头并预览

实时画面预览效果如下：
![实时画面预览](实时画面预览.jpg)


如果从零开始做个摄像头实时画面预览需要大量代码量，即构`SDK`早已将这行工作封装好，我们只需提供一个已经在`ContentView`中布局好的`TextureView`即可。示例代码如下：
```java
ZegoCanvas canvas = new ZegoCanvas(textureView);
canvas.viewMode = ASPECT_FILL;
mEngine.startPreview(canvas);
```

## 3.1.2 推流与停止推流
推流更简单，直接调用即构`SDK`一行代码：
```java
   mEngine.startPublishingStream(streamId);
```
指定视频流的唯一`ID`，传给`startPublishingStream`函数即可。停止推流直接调用`stopPublishingStream()`函数：
```java
mEngine.stopPublishingStream();
```


## 3.2 拉流预览
与本地预览实时画面一样，即构`SDK`将拉取远程视频流也封装到了极致，只需一行代码即可。我们在调用的时候仅需指定`TextureView`与对应的流`ID`：
```java
ZegoCanvas canvas = new ZegoCanvas(textureView);
canvas.viewMode = ASPECT_FILL;
mEngine.startPlayingStream(streamId, canvas);
```

## 3.3 播放实时画面统一封装
根据拉流与推流的介绍，我们知道，其实播放实时画面得时候（房主和观众都一样）最多仅需`TextureView`与对应的流`ID`两个参数，因此我们把这两个参数封装到`PreviewItem`中：
```java

public class PreviewItem {
    public TextureView tv;
    public String streamId;

    public PreviewItem(TextureView tv, String streamId) {
        this.tv = tv;
        this.streamId = streamId;
    }
}
```
然后封装`playPreview`函数, 不管是房主还是观众，都可以统一调用这个函数：

```java
public void playPreview(PreviewItem pi, boolean isMyself) {
        ZegoCanvas canvas = new ZegoCanvas(pi.tv);
        canvas.viewMode = ASPECT_FILL;
        //不管有没有推流，先停止推流
        mEngine.stopPublishingStream();
        if (isMyself) {//本地预览
            mEngine.startPublishingStream(pi.streamId);
            mEngine.startPreview(canvas);
        } else {//拉取视频流
            //拉取远程视频流
            mEngine.startPlayingStream(pi.streamId, canvas);
        }
    }

```
如果是房主，则在预览画面的同时，执行推流任务。如果是观众，直接拉流即可。


# 4 其他工作
由于本文没有采用个人后台服务器做一些权限控制，因此会存在安全风险。如果是线上`APP`，请务必记得将敏感操作放到自己的后台服务器中执行。


## 4.1 观众如何得知房主的视频流ID ？
在本文中，对于每个用户，如果需要推流，则将其推流的`ID`设置为其`userID`，**强烈建议线上产品不要这么做，最好是由个人服务器生成，推荐`RoomID_UserID_后缀`形式，以避免串流**。用户在监听到`onRoomStreamUpdate`回调信息后，可以得到新增（或退出）的视频流ID。

## 4.2 如何获取房主ID？直播间的商品信息？直播间的名称信息？
同样的问题，如果有个人服务器，直接访问服务器查询相关数据库即可获取。但没有个人服务器怎么办？这里我们通过房主监听每个用户登录房间回调函数+房间内实时消息来实现。具体可描述如下：
> 房主监听登录房间回调函数，如果有用户登录房间，则发送商品信息、房主userID、房间名称等数据消息。

发送消息可以调用`sendCustomCommand`函数实现：
```java
public void sendMsg(String roomId, ArrayList<ZegoUser> userList, Msg msg) {
    String msgPack = msg.toString();
    // 发送自定义信令，`toUserList` 中指定的用户才可以通过 
	// onIMSendCustomCommandResult 收到此信令.
    // 若 `toUserList` 参数传 `null` 则 SDK 将发送该信令给房间内所有用户
    mEngine.sendCustomCommand(roomId, msgPack, userList, new IZegoIMSendCustomCommandCallback() {
        @Override
        public void onIMSendCustomCommandResult(int errorCode) {}
    });
}
```


# 5 源码分享

[http://xxx.xxx](http://xxx.xxx)














