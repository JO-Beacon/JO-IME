<h1 align="center">JO-IME</h1>

<p align="center">
  <a href="README.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a>
</p>

**JO-IME** is an independently maintained Android input method built on the [Rime](https://rime.im/) engine, branched from [CyIME](https://github.com/Cyletix/CyIME), itself a fork of [Xime](https://github.com/ximeiorg/Xime).

Not a general-purpose IME: Chinese is 26-key full pinyin only, the only languages are Chinese and English, and settings are kept to a minimum.

## Download and install

Requires Android 9.0 (API 28) or later. Packages are ARM64 only.

1. Download from [Releases](https://github.com/JO-Beacon/JO-IME/releases):
   - **Lite** `JO-IME-<version>-arm64-v8a.apk`: no voice, handwriting or prediction models; download them from "Extensions › Models" when needed.
   - **Full** `JO-IME-<version>-full-arm64-v8a.apk`: bundles handwriting, prediction (base + small), SenseVoice, Paraformer and speaker models — works offline out of the box.
2. After installing, enable **JO-IME** in system settings and make it your current keyboard.
3. Later, update from "Settings › About › Check for updates"; it downloads the same edition (Lite or Full) and hands it to the system installer.

## Features

**Chinese**

- 26-key full pinyin, powered by the rime-ice dictionary.
- Prediction, pinyin/gloss candidate comments, traditional output, rare characters, date and time, symbol names.
- Handwriting, voice and photo text recognition all run on the device.

**English**

- 26 keys, whole-word completion; a space is added after picking a word and after punctuation.

**Handwriting and voice**

- Handwriting: Chinese, English and math formulas (recognized as LaTeX).
- Voice: on-device recognition with downloadable SenseVoice, Paraformer and other models; you can also plug in your own online endpoint.
- Captions: live captions for the people around you, with speaker separation and saved transcripts.

**Keyboard and appearance**

- Color themes, glass effect, key sounds and vibration, key font size.
- Language key switching, long-press space to talk or move the cursor, swipe gestures.

**Dictionaries and data**

- Your own phrases, imported dictionaries, suppressed candidates, dictionary lookup.
- Clipboard history and cross-device sync.
- Backup and restore, performance and battery.

**Extensions**

- Models and plugins are managed in "Extensions": download, install, uninstall.

## Build

```bash
# Clone with submodules
git clone --recursive https://github.com/JO-Beacon/JO-IME.git

# Already cloned? Initialize submodules
git submodule update --init --recursive

# Debug / release
./gradlew assembleDebug
./gradlew assembleRelease
```

Requires JDK 17 and NDK `29.0.14206865`. The build downloads the rime-ice dictionaries and the sherpa-onnx runtime, verifies them against recorded SHA-256 hashes and packs them into the APK. For everyday builds use `packager.cmd` in the repository root (double-click for the GUI, or pass arguments on the command line); presets live in `scripts/packager/config.json`, and the version rules are in [packaging rules](docs/packaging.md).

Stack: Kotlin · Jetpack Compose · Material 3 · librime (JNI / C++) · ONNX Runtime · sherpa-onnx · OpenCV

## Project relationship

- The source is branched from [CyIME](https://github.com/Cyletix/CyIME), which in turn is a fork of [Xime](https://github.com/ximeiorg/Xime) (Kingz Cheung / kingzcheung and contributors).
- To keep comparison with upstream easy, the source package name `com.kingzcheung.xime`, plugin IDs, `xime.yaml` / `xime.custom.yaml` and the extension protocol keep their upstream names; the user-visible app name and package are JO-IME's own.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md): open an issue first, then a minimal pull request.

## Credits

JO-IME uses the projects, data and models below. Thanks to their authors. Full license texts and copyright notices ship in `app/src/main/assets/licenses/`.

| Project | Author | Used for | License |
| --- | --- | --- | --- |
| [CyIME](https://github.com/Cyletix/CyIME) | Cyletix and contributors | JO-IME's direct upstream: UI, Rime integration, IME service | GPL-3.0 |
| [Xime](https://github.com/ximeiorg/Xime) | Kingz Cheung (kingzcheung) and contributors | CyIME forked from it | GPL-3.0 |
| Xime handwriting and prediction models: [ochwpro](https://www.modelscope.cn/models/bikeand/ochwpro), [predictive-text-small](https://www.modelscope.cn/models/bikeand/predictive-text-small) | Xime project | Chinese handwriting, predictive text | MIT, Apache-2.0, CC BY-NC-SA 4.0 |
| [Rime (librime)](https://github.com/rime/librime) | 佛振 and the Rime community | Input engine | BSD-3-Clause |
| [OpenCC](https://github.com/BYVoid/OpenCC) | BYVoid | Simplified/Traditional conversion | Apache-2.0 |
| [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) | k2-fsa | On-device speech recognition | Apache-2.0 |
| [ONNX Runtime](https://github.com/microsoft/onnxruntime) | Microsoft | Inference for handwriting, prediction and OCR models | MIT |
| [rime-ice](https://github.com/iDvel/rime-ice) | iDvel (Dvel) | Pinyin dictionary and schema | GPL-3.0 |
| [LCCC](https://github.com/thu-coai/CDial-GPT) | Tsinghua CoAI | Training the prediction model | MIT |
| [ECDICT](https://github.com/skywind3000/ECDICT), [CC-CEDICT](https://cc-cedict.org/) | skywind3000; MDBG | Chinese/English candidate glosses | MIT, CC BY-SA 4.0 |
| [SenseVoice](https://github.com/FunAudioLLM/SenseVoice) | Alibaba FunAudioLLM | Speech recognition | FunASR model license |
| [Paraformer](https://github.com/modelscope/FunASR) | Alibaba DAMO FunASR | Streaming speech recognition | Apache-2.0 |
| [3D-Speaker CAM++](https://github.com/modelscope/3D-Speaker) | Alibaba 3D-Speaker | Speaker separation in captions | Apache-2.0 |
| [PP-OCRv5](https://github.com/PaddlePaddle/PaddleOCR) | PaddlePaddle; RapidAI | Photo text recognition | Apache-2.0 |
| Samsung Keyboard | Samsung | Keyboard interaction and appearance reference | — |
| Gboard | Google | Keyboard interaction and appearance reference | — |
| [Typewise](https://www.typewise.app/) | Typewise | Honeycomb layout reference | — |

## License

[GPLv3](LICENSE). Upstream code is copyrighted by its authors; changes in this branch are maintained by JO-Beacon. The "Xime" name, logo and other brand assets are outside the open-source grant — see [TRADEMARKS.md](TRADEMARKS.md).
