<h1 align="center">JO-IME</h1>

<p align="center">
  <a href="README.en.md">English</a> · <a href="README.md">简体中文</a>
</p>

**JO-IME** 是獨立維護的 Android 輸入法，基於 [Rime](https://rime.im/) 引擎，從 [CyIME](https://github.com/Cyletix/CyIME)（其上游為 [Xime](https://github.com/ximeiorg/Xime)）分支而來。

不做通用輸入法：中文只做全拼 26 鍵，語言只有中文和英文，設定項目能少則少。

> 這是獨立維護的分支，不是 CyIME 官方版本。


## 下載與安裝

要求 Android 9.0（API 28）及以上，安裝包只有 ARM64。

1. 在 [Releases](https://github.com/JO-Beacon/JO-IME/releases) 下載安裝包：
   - **輕量版** `JO-IME-<版本>-arm64-v8a.apk`：不帶語音、手寫、聯想模型，需要時在「擴充 › 模型」下載。
   - **完整版** `JO-IME-<版本>-full-arm64-v8a.apk`：內建手寫、聯想（完整＋輕量）、SenseVoice、Paraformer 和聲紋模型，裝上就能離線用。
2. 安裝後在系統設定裡啟用 **JO-IME**，並把它設為目前輸入法。
3. 以後在「設定 › 關於 › 檢查更新」更新，會下載和現在同一種（輕量／完整）的安裝包交給系統安裝。

## 功能

**中文**

- 全拼 26 鍵，詞庫用霧凇拼音。
- 聯想、候選拼音／釋義註釋、繁體輸出、生僻字、時間日期、符號名。
- 手寫、語音、拍照識字都在本機完成。

**英文**

- 26 鍵，整詞補全；選詞後自動加空格，標點後自動加空格。

**手寫與語音**

- 手寫：中文、英文、數學公式（辨識成 LaTeX）。
- 語音：本機離線辨識，可下載 SenseVoice、Paraformer 等模型；也支援填自己的線上介面。
- 字幕：把周圍的話即時轉成字幕，自動分說話人，可儲存記錄。

**鍵盤與外觀**

- 主題配色、玻璃效果、按鍵音與震動、按鍵字號。
- 語言鍵切換、長按空白鍵說話或移游標、滑動手勢。

**詞庫與資料**

- 自己的短語、匯入詞庫、刪掉的候選、查詞。
- 剪貼簿歷史與跨裝置同步。
- 備份與還原、效能與省電。

**擴充**

- 「擴充」裡管理模型和插件：下載、安裝、解除安裝。

## 建置

```bash
# 複製專案（含子模組）
git clone --recursive https://github.com/JO-Beacon/JO-IME.git

# 已複製過則初始化子模組
git submodule update --init --recursive

# 除錯包 / 發佈包
./gradlew assembleDebug
./gradlew assembleRelease
```

需要 JDK 17 和 NDK `29.0.14206865`。建置時會連網下載霧凇拼音詞庫和 sherpa-onnx 執行庫，按記錄的 SHA-256 校驗後打進安裝包。日常出包用專案根目錄的 `packager.cmd`（雙擊開啟圖形介面，帶參數是命令列），預設見 `scripts/packager/config.json`；版本規則見 [打包與版本規則](docs/packaging.md)。

技術棧：Kotlin · Jetpack Compose · Material 3 · librime（JNI / C++）· ONNX Runtime · sherpa-onnx · OpenCV

## 專案關係

- 原始碼從 [CyIME](https://github.com/Cyletix/CyIME) 分支而來，CyIME 又從 [Xime](https://github.com/ximeiorg/Xime)（Kingz Cheung / kingzcheung 及貢獻者）分支而來。
- 為了方便與上游對照，原始碼套件名 `com.kingzcheung.xime`、插件識別碼、`xime.yaml` / `xime.custom.yaml`、擴充協定都沿用上游命名；對外的應用名稱和套件名是 JO-IME 自己的。

## 貢獻

見 [CONTRIBUTING.md](CONTRIBUTING.md)：先提 Issue，再提最小修改的 PR。

## 鳴謝

JO-IME 用到了下面這些專案、資料和模型，感謝作者們的工作。安裝包裡的授權全文和版權聲明見 `app/src/main/assets/licenses/`。

| 專案 | 作者 | 用在哪裡 | 授權 |
| --- | --- | --- | --- |
| [CyIME](https://github.com/Cyletix/CyIME) | Cyletix 及貢獻者 | JO-IME 的直接上游：介面、Rime 接入、輸入法服務 | GPL-3.0 |
| [Xime](https://github.com/ximeiorg/Xime) | Kingz Cheung（kingzcheung）及貢獻者 | CyIME 從它 fork 而來 | GPL-3.0 |
| Xime 手寫與聯想模型：[ochwpro](https://www.modelscope.cn/models/bikeand/ochwpro)、[predictive-text-small](https://www.modelscope.cn/models/bikeand/predictive-text-small) | Xime 專案 | 中文手寫辨識、智慧聯想 | MIT、Apache-2.0、CC BY-NC-SA 4.0 |
| [Rime（librime）](https://github.com/rime/librime) | 佛振及 Rime 社群 | 輸入引擎 | BSD-3-Clause |
| [OpenCC](https://github.com/BYVoid/OpenCC) | BYVoid | 簡繁轉換 | Apache-2.0 |
| [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) | k2-fsa | 本機語音辨識 | Apache-2.0 |
| [ONNX Runtime](https://github.com/microsoft/onnxruntime) | Microsoft | 手寫、聯想、識字的模型推論 | MIT |
| [霧凇拼音 rime-ice](https://github.com/iDvel/rime-ice) | iDvel（Dvel） | 拼音詞庫和方案 | GPL-3.0 |
| [LCCC](https://github.com/thu-coai/CDial-GPT) | 清華大學 CoAI 小組 | 訓練聯想模型 | MIT |
| [ECDICT](https://github.com/skywind3000/ECDICT)、[CC-CEDICT](https://cc-cedict.org/) | skywind3000；MDBG | 候選中英釋義 | MIT、CC BY-SA 4.0 |
| [SenseVoice](https://github.com/FunAudioLLM/SenseVoice) | 阿里巴巴 FunAudioLLM | 語音辨識 | FunASR 模型授權 |
| [Paraformer](https://github.com/modelscope/FunASR) | 阿里巴巴達摩院 FunASR | 邊說邊出字的語音辨識 | Apache-2.0 |
| [3D-Speaker CAM++](https://github.com/modelscope/3D-Speaker) | 阿里巴巴 3D-Speaker | 字幕區分說話人 | Apache-2.0 |
| [PP-OCRv5](https://github.com/PaddlePaddle/PaddleOCR) | PaddlePaddle；RapidAI | 拍照識字 | Apache-2.0 |
| 三星鍵盤 | Samsung | 鍵盤互動和外觀參考 | — |
| Gboard | Google | 鍵盤互動和外觀參考 | — |
| [Typewise](https://www.typewise.app/) | Typewise | 蜂巢六邊形佈局參考 | — |

## 授權

[GPLv3](LICENSE)。上游程式碼版權歸其作者所有，本分支的修改由 JO-Beacon 維護。「Xime」名稱、Logo 及其他品牌資產不在開源授權範圍內，見 [TRADEMARKS.md](TRADEMARKS.md)。
