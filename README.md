<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Neko music" width="120" height="120">
</p>

<h1 align="center">Neko music</h1>

<p align="center">
  <strong>网易云音乐播放器 · 小天才电话手表与 OPPO Watch 3 Pro 适配</strong>
</p>

<p align="center">
  <a href="../../releases/latest"><img src="https://img.shields.io/github/v/release/FOXcode-sys/Neko-163Music-Pro?style=flat-square" alt="Latest Release"></a>
  <img src="https://img.shields.io/badge/platform-Android%206.0%2B-brightgreen?style=flat-square" alt="Platform">
  <img src="https://img.shields.io/badge/screen-320%C3%97360-blue?style=flat-square" alt="Screen">
  <img src="https://img.shields.io/github/license/FOXcode-sys/Neko-163Music-Pro?style=flat-square" alt="License">
</p>

<p align="center">
  直接调用网易云音乐 API，无需第三方中间服务器。<br>
  为 320×360 手表屏幕精心适配，所有界面均支持手势与表冠操作。<br>
  本项目基于 <a href="https://github.com/9xhk-1/163MusicPro">163MusicPro</a> 修改，应用名与图标已更换为 <strong>Neko music</strong>。<br>
  原项目官网：<a href='https://163.imoow.com'>https://163.imoow.com</a>
</p>

---

## ✨ 功能特性

| 功能 | 说明 |
|------|------|
| 🔍 **在线搜索** | 搜索网易云音乐全曲库，支持分页加载 |
| ▶️ **音乐播放** | 上一首 / 下一首 / 暂停 / 播放，自动切歌 |
| 📝 **歌词同步** | 在线获取 LRC 歌词，逐行高亮滚动显示 |
| ❤️ **收藏管理** | 本地 / 云端收藏，数据持久化，重装自动恢复 |
| ⬇️ **离线下载** | 下载歌曲到本地，支持离线播放 |
| 🔔 **铃声设置** | 截取歌曲片段设为手表铃声 |
| 📊 **排行榜** | 浏览网易云热门榜单 |
| 📜 **播放历史** | 自动记录最近 200 首播放记录 |
| ⚡ **倍速播放** | 0.1x – 5.0x 变速，支持音调不变 / 音调随速度改变 |
| 🎲 **播放模式** | 列表循环 / 单曲循环 / 随机播放 |
| ⏱ **定时关闭** | 定时自动停止播放 |
| 🔊 **音量控制** | 自定义音量叠加层，1.5s 自动消失 |
| 🔑 **多种登录** | 扫码登录 / Cookie 登录 / 短信登录 / 密码登录 |
| 🛡️ **后台保活** | 前台服务 + WakeLock，锁屏和后台不被杀死 |
| 👤 **个人中心** | 查看账号信息、VIP 状态与有效期 |
| 🎵 **私人漫游** | 登录后获取个性化推荐歌曲 |
| 🎡 **表冠操作** | 旋转表冠滚动列表与歌词 `新增` |
| 👈 **右滑返回** | 全页面快速右滑返回上一级 `新增` |
| 📶 **无网络提示** | 断网时给出明确提示，不发起无效请求 `新增` |

## 🐱 相对上游的适配改动

以下改动针对 **OPPO Watch 3 Pro** 及更通用的安卓手表环境而做，均已在源码中实现。

### 1. 图标 / 应用名

- 应用名（启动器、关于页、通知栏、MediaSession）统一改为 **Neko music**
- 启动图标已替换为猫娘图片，生成了 mdpi ~ xxxhdpi 全部密度的普通图标与自适应图标（Android 8+）
- **未改动**：本地存储路径仍是 `/sdcard/163Music/...`（下载、缓存等），没有跟着改名，避免影响已下载文件的组织方式

### 2. 全局右滑返回上一级

- 项目本就有一套"快速右滑 = 返回"的手势模式（`MoreActivity` 的横向位移 > 80px、纵向 < 200px、速度 > 200 触发 `finish()`）
- 把这套逻辑收进了 `BaseWatchActivity` 基类，并把原来直接继承 `AppCompatActivity` 的 19 个二级页面（关于、登录、专辑详情、我的歌单、个人中心、排行榜、更新等）统一改成继承 `BaseWatchActivity`，全部自动获得右滑返回
- 播放主页（`MainActivity`）和更多菜单（`MoreActivity`）保留各自原有的手势实现，额外补上了表冠支持

### 3. 表冠（旋转编码器）适配，含歌词滚动

- 新增 `RotaryInputHelper` 工具类：监听安卓标准的 `MotionEvent.ACTION_SCROLL` + `SOURCE_ROTARY_ENCODER` + `AXIS_SCROLL`（安卓框架级标准机制，非厂商私有 API），自动找到当前页面上的 ScrollView / ListView / RecyclerView 并转换成滚动
- 已接入：`BaseWatchActivity`（所有继承它的页面自动支持表冠滚动列表）、`MoreActivity`、播放页内嵌歌词浮层（`MainActivity`）、独立歌词页 `LyricsActivity`
- 歌词页：转动表冠 = 手动滑动歌词，会和触摸滑动一样正确触发"阻塞模式"（暂停自动跟随高亮，一段时间无操作后恢复）

> ⚠️ **需要真机验证**：OPPO Watch 3 Pro 的表冠没有公开的第三方开发文档，以上实现基于安卓标准机制推断（ColorOS Watch 官方定位是"基于 Android 原生打造"）。如果装机后发现转动方向反了，改一行代码即可：`RotaryInputHelper.java` 里的 `DIRECTION` 常量，`1` 改成 `-1`。

### 4. 系统内置播放器适配

- 原来的前台服务通知是"隐形"的（专门给小天才手表 XTC 卡片用），现在升级成了标准的 `Notification.MediaStyle` + `MediaSession` 通知：歌名、歌手、上一首 / 播放暂停 / 下一首三个按钮
- 这是安卓系统级"正在播放"控制卡片（状态栏、控制中心、锁屏）识别第三方 App 播放状态的标准方式，ColorOS Watch 理论上能识别
- 小天才专用的 XTC 卡片逻辑原样保留，其它系统会自动忽略

> ⚠️ 同样没有 OPPO 官方文档可以确认，建议真机验证；如果系统播放卡片没反应，大概率是 ColorOS Watch 用了另一套私有机制。

### 5. 搜狗手表输入法（2.8）适配

- 搜索框：加了 `imeOptions="actionSearch"`，搜狗输入法的"搜索 / 回车"键现在可以直接触发搜索并收起键盘，不用再点小小的搜索按钮
- 密码登录 / 短信登录：手机号输入框回车 = 跳到下一个输入框；密码 / 验证码输入框回车 = 直接登录
- 标准 `EditText` 本身对任何系统输入法（包括搜狗手表版）都是通用的，这次改动主要是让"回车 / 搜索"键触发正确的动作

### 6. 专辑背景图比例修复

- 问题根源：设置里"自定义背景 → 使用专辑封面"这个功能，是把几乎总是正方形的专辑封面直接 `setBackground(BitmapDrawable)` 整张拉伸铺满播放页 / 歌词页，而 `BitmapDrawable` 默认拉伸填满（不裁剪），正方形图硬套到手表的竖长屏幕上自然会变形
- 修复：新增 `BackgroundUtil.cropToScreenAspect()`，设置背景前先按屏幕宽高比居中裁剪（效果等同于 `CENTER_CROP`），裁掉多余部分而不是拉伸，图片比例不再失真。该方法是所有背景应用入口（播放主页、歌词页、换歌自动换背景）的唯一调用点，一处修复全部生效
- 自定义图片背景（选择图片）也顺带用了同一个裁剪逻辑，同样受益

### 7. 无网络提示

- 新增 `NetworkUtils.isConnected()`，在**搜索**（首次搜索 + 歌曲 / 歌单 / MV 三个 tab 的翻页加载更多）发起网络请求前先检查网络
- 无网络时会弹出 Toast：**"无网络连接喵～"**，不会真的发起请求

## 📱 建议的真机测试重点

1. 表冠转动方向、歌词页表冠滚动是否跟手
2. 各二级页面右滑返回是否顺畅、有没有跟原有的上下滑动手势"打架"
3. 系统控制中心 / 状态栏有没有出现"正在播放"卡片，按钮能不能控制播放
4. 搜狗输入法下，搜索框、登录页的回车 / 搜索键是否符合预期
5. 飞行模式下点搜索，确认弹出"无网络连接喵～"

## 📦 安装

### 从 Release 安装（推荐）

1. 前往 [**Releases**](../../releases/latest) 下载最新 APK
2. 将 APK 传输到手表（通过 ADB 或文件管理）
3. 在手表上安装并打开应用

### 从源码构建

**环境要求：** JDK 17、Android SDK 34

```bash
# 克隆仓库
git clone https://github.com/FOXcode-sys/Neko-163Music-Pro.git
cd Neko-163Music-Pro

# 构建调试版本
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk

# 构建签名发布版本
./gradlew assembleRelease
# APK → app/build/outputs/apk/release/app-release.apk
```

> 签名发布需配置环境变量：`KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`

也可以直接用仓库自带的 `.github/workflows`，推送到 GitHub 后由 Actions 云编译。

## 🚀 快速开始

1. 打开应用进入播放器主界面
2. **左滑** 查看歌词，**右滑** 关闭歌词 / 返回上一级
3. **转动表冠** 可滚动列表与歌词
4. 点击右上角 **⋯** 进入功能菜单
5. 在菜单中选择 **搜索**，输入歌曲名即可播放
6. 如需播放 VIP 歌曲，进入 **登录** 页面完成登录：
   - **扫码登录**：使用网易云音乐 App 扫描二维码
   - **Cookie 登录**：手动粘贴 Cookie

## 🏗 项目结构

```
app/src/main/java/com/qinghe/music163pro/
├── activity/          # UI 界面 (MainActivity, SearchActivity, BaseWatchActivity, ...)
├── api/               # 网易云 API 调用 (MusicApiHelper, NeteaseApiCrypto)
├── manager/           # 数据管理 (FavoritesManager, DownloadManager, HistoryManager)
├── model/             # 数据模型 (Song)
├── player/            # 播放器核心 (MusicPlayerManager)
├── service/           # 后台服务 (MusicPlaybackService)
└── util/              # 工具类 (RotaryInputHelper, NetworkUtils, BackgroundUtil, MusicLog, QrCodeGenerator)
```

## ⚙️ 技术规格

| 项目 | 值 |
|------|------|
| 应用名 | `Neko music` |
| 包名 | `com.qinghe.music163pro` |
| 最低 SDK | Android 6.0 (API 23) |
| 目标 SDK | Android 8.1 (API 27) |
| 编译 SDK | Android 14 (API 34) |
| 屏幕适配 | 320×360 (小天才手表) |
| 表冠支持 | `ACTION_SCROLL` + `SOURCE_ROTARY_ENCODER` |
| API 加密 | WeAPI (AES-128-CBC + RSA) / EAPI |
| 主要依赖 | appcompat 1.6.1、material 1.11.0、media3 1.4.1、constraintlayout 2.1.4 |

## 🔄 CI/CD

推送到 `main` 分支后，GitHub Actions 会自动构建 APK 并发布到 Releases（tag 形如 `v{versionName}-build{versionCode}`）。

| Workflow | 触发条件 | 作用 |
|----------|----------|------|
| `release.yml` | push `main` / 手动触发 | 构建并发布到 Releases |
| `build.yml` | PR / 非 `main` 分支 push / 手动触发 | 构建校验，上传 artifact |

**签名发布**：在 **Settings → Secrets and variables → Actions** 添加以下 Secrets：

| Secret | 说明 |
|--------|------|
| `KEYSTORE_BASE64` | 签名密钥库（`.jks`）的 Base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

未配置时会自动降级构建 debug APK，不会导致流程失败。

## 🙏 致谢

本项目基于 [**9xhk-1/163MusicPro**](https://github.com/9xhk-1/163MusicPro) 修改，遵循 MIT License，原作者版权信息见 [LICENSE](LICENSE)。

## ℹ️ 注意

本项目使用 vibe coding 辅助编写。
