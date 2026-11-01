# 打包与安装

当前版本 **0.1.0**（`versionCode 1`），以 `app/build.gradle.kts` 为唯一来源。

## 版本规则

JO-IME 从 0.1.0 重新起算，与上游 CyIME 的 1.x 版本号脱钩。

| 本次交付内容 | 编号规则 | 例子 |
|---|---|---|
| 兼容的问题修复、性能改善、交互打磨 | PATCH 加一 | 0.1.0 → 0.1.1 |
| 新增用户可用功能 | MINOR 加一，PATCH 归零 | 0.1.9 → 0.2.0 |
| 不兼容的配置、数据或插件契约变更 | MAJOR 加一，其余归零 | 0.3.4 → 1.0.0 |

- `versionName` 给人看，`versionCode` 给系统判断升级顺序，两者分开维护。
- `versionCode` 单调递增，不按日期重算；同一版本号不覆盖已交付的内容。
- 日常编辑、测试和失败重试不改版本号。

## 产物命名

```
JO-IME-<版本>-<abi>.apk           轻量版
JO-IME-<版本>-full-<abi>.apk      完整版（内置离线模型）
JO-IME-<版本>-debug-<abi>.apk     调试包
```

ABI 由 `splits.abi` 决定，另有 universal 包。完整版用 `-PbundleModels=true` 构建，内置模型清单见 `scripts/prepare-bundled-models.py` 的 `INCLUDED`。

## 构建

```bash
./gradlew assembleDebug            # 调试包
./gradlew assembleRelease          # 轻量正式包
./gradlew assembleRelease -PbundleModels=true   # 完整版
```

需要 JDK 17 和 NDK `29.0.14206865`。构建时会联网下载雾凇拼音词库和 sherpa-onnx 运行库，按记录的 SHA-256 校验后打进安装包。

## 签名

存在 `app/keystore.properties` 时用它签名（字段：`storeFile` 或 `keyBase64`、`storePassword`、`keyAlias`、`keyPassword`）。没有时 Release 用调试签名，不能覆盖正式版。

## 发布

1. 确认 `app/build.gradle.kts` 的 `versionName` / `versionCode`。
2. 打 tag 并推送：`git tag v0.1.0 && git push origin v0.1.0`。
3. `.github/workflows/release.yml` 会构建轻量与完整两版 APK，上传到该 tag 的**草稿** Release；确认无误后在 GitHub 上手动发布。

## 本地脚本

```powershell
# 构建正式包（默认）/ 调试包
./scripts/build-apk.ps1
./scripts/build-apk.ps1 -BuildType Debug

# 安装当前版本的包（自动识别架构和文件名）
./scripts/install-debug.ps1
./scripts/install-release.ps1
```

`build-apk.ps1` 会把旧包移入 `app/build/outputs/apk/archive/<时间>/<类型>/`，不删除历史包，也不运行 `clean`；构建失败会报错，不会把旧包当成新包。成功后打印实际路径并生成 `latest.json`（版本、类型、路径、SHA256）。

安装脚本只选当前源码版本对应的安装包，拒绝旧包或多设备歧义；`-Action uninstall` 是显式卸载，会删除应用数据。
