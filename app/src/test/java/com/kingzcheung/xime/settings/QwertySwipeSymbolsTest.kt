package com.kingzcheung.xime.settings

import com.kingzcheung.xime.keyboard.GestureAction
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class QwertySwipeSymbolsTest {
    @Test fun savedSymbolsAreReadBackIncludingExplicitlyEmptySlots() {
        val prefs = mock<android.content.SharedPreferences>()
        val context = mock<android.content.Context>()
        whenever(context.getSharedPreferences("kime_settings", android.content.Context.MODE_PRIVATE)).thenReturn(prefs)
        whenever(prefs.contains("qwerty_symbols_zh_up.q")).thenReturn(true)
        whenever(prefs.getString("qwerty_symbols_zh_up.q", "")).thenReturn("★")
        whenever(prefs.contains("qwerty_symbols_zh_down.q")).thenReturn(true)
        whenever(prefs.getString("qwerty_symbols_zh_down.q", "")).thenReturn("")
        assertEquals(mapOf("up.q" to "★", "down.q" to ""), QwertySwipeSymbols.load(context, false))
        assertEquals(emptyMap<String, String>(), QwertySwipeSymbols.load(context, true))
    }
    @get:org.junit.Rule val temp = org.junit.rules.TemporaryFolder()
    private val defaults get() = requireNotNull(javaClass.classLoader?.getResource("xime.yaml")).readText()
    @org.junit.Before fun loadActualLayoutBindings() {
        val assets = mock<android.content.res.AssetManager>()
        whenever(assets.open("xime.yaml")).thenAnswer { defaults.byteInputStream() }
        whenever(assets.open("xime.custom.yaml")).thenThrow(java.io.IOException("No custom override"))
        val context = mock<android.content.Context>()
        whenever(context.assets).thenReturn(assets)
        whenever(context.filesDir).thenReturn(temp.root)
        whenever(context.getSharedPreferences("kime_settings", android.content.Context.MODE_PRIVATE))
            .thenReturn(mock<android.content.SharedPreferences>())
        KeysConfigHelper.loadConfig(context)
    }
    @org.junit.After fun resetActiveProfile() { KeysConfigHelper.setActiveKeyboardSchema("") }

    @Test fun fourteenKeyPresetIsDigitsThenSymbols() {
        assertEquals(listOf("qw", "er", "ty", "ui", "op", "as", "df", "gh", "jk", "l", "zx", "cv", "bn", "m"),
            FourteenKeySymbols.keys)
        // 前两排上方 1-0；第三排上方 、：！？。
        assertEquals(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "、", "：", "！", "？"),
            FourteenKeySymbols.keys.map { FourteenKeySymbols.up.getValue(it) })
        // 前两排下方半角运算/网址符号，@ # % * 在 Shift+2/3/5/8 的位置，（ ）在 9、0 下；第三排下方 …… ～ 《 》。
        assertEquals(listOf("+", "@", "#", "/", "%", "-", "_", "*", "（", "）", "……", "～", "《", "》"),
            FourteenKeySymbols.keys.map { FourteenKeySymbols.down.getValue(it) })
        // 28 个位置不重复，也不和空格两边的逗号句号重复。
        val all = FourteenKeySymbols.up.values + FourteenKeySymbols.down.values
        assertEquals(28, all.toSet().size)
        assertTrue(all.none { it in setOf("，", "。", ",", ".") })
    }

    @Test fun fourteenKeyEditorOverridesWinAndEmptyMeansNoSymbol() {
        val original = KeyGestureConfig(swipeUp = GestureDef(label = "Ω", value = "Ω"))
        // 自定义 YAML 给这个键写的 swipe_up 顶替预设；设置页的修改又顶替 YAML。
        assertEquals("Ω", FourteenKeySymbols.apply("qw", original, emptyMap())!!.swipeUp!!.value)
        assertEquals("★", FourteenKeySymbols.apply("qw", original, mapOf("up.qw" to "★"))!!.swipeUp!!.value)
        val cleared = FourteenKeySymbols.apply("m", null, mapOf("down.m" to ""))!!.swipeDown!!
        assertEquals(GestureAction.NONE, cleared.action)
        assertEquals("w → qw", "qw", FourteenKeySymbols.keyOf("w"))
        assertEquals("'", FourteenKeySymbols.keyOf("'"))
        assertEquals("earth", FourteenKeySymbols.keyOf("earth"))
        assertNull(FourteenKeySymbols.apply("'", null, emptyMap()))
    }

    @Test fun presetMatchesEveryRequestedKeyExactly() {
        val keys = "qwertyuiopasdfghjklzxcvbnm"
        assertEquals(26, QwertySwipeSymbols.up.size)
        // 中文：上方数字和常用全角符号，下方半角符号。英文上角标同位置换半角，下角标放补充符号（10-19）。
        assertEquals("1234567890～—…·¥、【】！（）：；《》？", keys.map { QwertySwipeSymbols.up.getValue(it.toString()) }.joinToString(""))
        assertEquals("!@#$%^&*()~-_'=\\[]/\"`:;<>?", keys.map { QwertySwipeSymbols.down.getValue(it.toString()) }.joinToString(""))
        assertEquals("1234567890~-…·¥\\[]!():;<>?", keys.map { QwertySwipeSymbols.englishUp.getValue(it.toString()) }.joinToString(""))
        assertEquals("!@#$%^&*()≈+_'=|{}/\"`×÷≤≥°", keys.map { QwertySwipeSymbols.englishDown.getValue(it.toString()) }.joinToString(""))
    }
    @Test fun editorOverridesYamlAndPreservesTapAndLongPress() {
        val original = KeyGestureConfig(tap = GestureDef(value = "a"), longPress = LongPressConfig())
        val yaml = KeyGestureConfig(swipeUp = GestureDef(value = "existing"))
        assertEquals("existing", QwertySwipeSymbols.apply("a", original, emptyMap(), yaml)!!.swipeUp!!.value)
        val edited = QwertySwipeSymbols.apply("a", original, mapOf("up.a" to "★", "down.a" to ""), yaml)!!
        assertEquals("★", edited.swipeUp!!.value)
        assertEquals(GestureAction.NONE, edited.swipeDown!!.action)
        assertEquals(original.tap, edited.tap)
        assertEquals(original.longPress, edited.longPress)
        assertNull(QwertySwipeSymbols.apply("space", null, emptyMap(), null))
    }
    @Test fun productionAccessorsAgreeOnChineseAndEnglishGestures() {
        KeysConfigHelper.setActiveKeyboardSchema("rime_ice")
        for (ascii in listOf(false, true)) for (key in QwertySwipeSymbols.keys.map(Char::toString)) {
            assertEquals(QwertySwipeSymbols.up(ascii)[key], KeysConfigHelper.getSwipeUpCommitValue(key, ascii))
            assertEquals(QwertySwipeSymbols.up(ascii)[key], KeysConfigHelper.getSwipeUpLabel(key, ascii))
            assertEquals(QwertySwipeSymbols.down(ascii)[key], KeysConfigHelper.getKeyGesture(key, ascii)!!.swipeDown!!.value)
            assertEquals(GestureAction.COMMIT, KeysConfigHelper.getSwipeDownAction(key, ascii))
        }
    }
}
