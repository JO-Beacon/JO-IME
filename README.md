<h1 align="center">JO-IME</h1>

<p align="center">
  <a href="README.en.md">English</a> · <a href="README.zh-TW.md">繁體中文</a>
</p>

**JO-IME** 是独立维护的 Android 输入法，基于 [Rime](https://rime.im/) 引擎，从 [CyIME](https://github.com/Cyletix/CyIME)（其上游为 [Xime](https://github.com/ximeiorg/Xime)）分支而来。

不做通用输入法：中文只做全拼 26 键，语言只有中文和英文，设置项能少就少。

## 下载与安装

要求 Android 9.0（API 28）及以上，安装包只有 ARM64。

1. 在 [Releases](https://github.com/JO-Beacon/JO-IME/releases) 下载安装包：
   - **轻量版** `JO-IME-<版本>-arm64-v8a.apk`：不带语音、手写、联想模型，需要时在「扩展 › 模型」下载。
   - **完整版** `JO-IME-<版本>-full-arm64-v8a.apk`：内置手写、联想（完整＋轻量）、SenseVoice、Paraformer 和声纹模型，装上就能离线用。
2. 安装后在系统设置里启用 **JO-IME**，并把它设为当前输入法。
3. 以后在「设置 › 关于 › 检查更新」更新，会下载和现在同一种（轻量／完整）的安装包交给系统安装。

## 功能

**中文**

- 全拼 26 键，词库用雾凇拼音。
- 联想、候选拼音／释义注释、繁体输出、生僻字、时间日期、符号名。
- 手写、语音、拍照识字都在本机完成。

**英文**

- 26 键，整词补全；选词后自动加空格，标点后自动加空格。

**手写与语音**

- 手写：中文、英文、数学公式（识别成 LaTeX）。
- 语音：本地离线识别，可下载 SenseVoice、Paraformer 等模型；也支持填自己的在线接口。
- 字幕：把周围的话实时转成字幕，自动分说话人，可保存记录。

**键盘与外观**

- 主题配色、玻璃效果、按键音与振动、按键字号。
- 语言键切换、长按空格说话或移光标、滑动手势。

**词库与数据**

- 自己的短语、导入词库、删掉的候选、查词。
- 剪贴板历史与跨设备同步。
- 备份与恢复、性能与省电。

**扩展**

- 「扩展」里管理模型和插件：下载、安装、卸载。

## 构建

```bash
# 克隆项目（含子模块）
git clone --recursive https://github.com/JO-Beacon/JO-IME.git

# 已克隆过则初始化子模块
git submodule update --init --recursive

# 调试包 / 发布包
./gradlew assembleDebug
./gradlew assembleRelease
```

需要 JDK 17 和 NDK `29.0.14206865`。构建时会联网下载雾凇拼音词库和 sherpa-onnx 运行库，按记录的 SHA-256 校验后打进安装包。日常出包用仓库根目录的 `packager.cmd`（双击打开图形界面，带参数是命令行），预设见 `scripts/packager/config.json`；版本规则见 [打包与版本规则](docs/packaging.md)。

技术栈：Kotlin · Jetpack Compose · Material 3 · librime（JNI / C++）· ONNX Runtime · sherpa-onnx · OpenCV

## 项目关系

- 源码从 [CyIME](https://github.com/Cyletix/CyIME) 分支而来，CyIME 又从 [Xime](https://github.com/ximeiorg/Xime)（Kingz Cheung / kingzcheung 及贡献者）分支而来。
- 为了方便与上游对照，源码包名 `com.kingzcheung.xime`、插件标识、`xime.yaml` / `xime.custom.yaml`、扩展协议都沿用上游命名；对外的应用名和包名是 JO-IME 自己的。

## 贡献

见 [CONTRIBUTING.md](CONTRIBUTING.md)：先提 Issue，再提最小修改的 PR。

## 鸣谢

JO-IME 用到了下面这些项目、数据和模型，感谢作者们的工作。安装包里的许可证全文和版权声明见 `app/src/main/assets/licenses/`。

| 项目 | 作者 | 用在哪里 | 许可证 |
| --- | --- | --- | --- |
| [CyIME](https://github.com/Cyletix/CyIME) | Cyletix 及贡献者 | JO-IME 的直接上游：界面、Rime 接入、输入法服务 | GPL-3.0 |
| [Xime](https://github.com/ximeiorg/Xime) | Kingz Cheung（kingzcheung）及贡献者 | CyIME 从它 fork 而来 | GPL-3.0 |
| Xime 手写与联想模型：[ochwpro](https://www.modelscope.cn/models/bikeand/ochwpro)、[predictive-text-small](https://www.modelscope.cn/models/bikeand/predictive-text-small) | Xime 项目 | 中文手写识别、智能联想 | MIT、Apache-2.0、CC BY-NC-SA 4.0 |
| [Rime（librime）](https://github.com/rime/librime) | 佛振及 Rime 社区 | 输入引擎 | BSD-3-Clause |
| [OpenCC](https://github.com/BYVoid/OpenCC) | BYVoid | 简繁转换 | Apache-2.0 |
| [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) | k2-fsa | 本地语音识别 | Apache-2.0 |
| [ONNX Runtime](https://github.com/microsoft/onnxruntime) | Microsoft | 手写、联想、识字的模型推理 | MIT |
| [雾凇拼音 rime-ice](https://github.com/iDvel/rime-ice) | iDvel（Dvel） | 拼音词库和方案 | GPL-3.0 |
| [LCCC](https://github.com/thu-coai/CDial-GPT) | 清华大学 CoAI 小组 | 训练联想模型 | MIT |
| [ECDICT](https://github.com/skywind3000/ECDICT)、[CC-CEDICT](https://cc-cedict.org/) | skywind3000；MDBG | 候选中英释义 | MIT、CC BY-SA 4.0 |
| [SenseVoice](https://github.com/FunAudioLLM/SenseVoice) | 阿里巴巴 FunAudioLLM | 语音识别 | FunASR 模型许可 |
| [Paraformer](https://github.com/modelscope/FunASR) | 阿里巴巴达摩院 FunASR | 边说边出字的语音识别 | Apache-2.0 |
| [3D-Speaker CAM++](https://github.com/modelscope/3D-Speaker) | 阿里巴巴 3D-Speaker | 字幕区分说话人 | Apache-2.0 |
| [PP-OCRv5](https://github.com/PaddlePaddle/PaddleOCR) | PaddlePaddle；RapidAI | 拍照识字 | Apache-2.0 |
| 三星键盘 | Samsung | 键盘交互和外观参考 | — |
| Gboard | Google | 键盘交互和外观参考 | — |
| [Typewise](https://www.typewise.app/) | Typewise | 蜂窝六边形布局参考 | — |

## 许可证

[GPLv3](LICENSE)。上游代码版权归其作者所有，本分支的修改由 JO-Beacon 维护。"Xime" 名称、Logo 及其他品牌资产不在开源授权范围内，见 [TRADEMARKS.md](TRADEMARKS.md)。
