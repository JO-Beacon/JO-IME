package com.kingzcheung.xime.settings

import com.kingzcheung.xime.keyboard.GestureAction
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.Mockito
import java.io.File

/**
 * 自建双字母布局的角标可以在符号设置页按布局改（B 站用户 10-31：自己合并的 14 键，下滑数字和符号改不了）。
 * 预设还是 [MergedKeySymbols] 那张表；改了的只影响这个布局，键盘和设置页读到的一样。
 */
class CustomLayoutSymbolsTest {
    @get:Rule val temp = TemporaryFolder()
    private val defaults get() = requireNotNull(javaClass.classLoader?.getResource("xime.yaml")).readText()
    private val merged = "custom_pinyin_" + "d".repeat(32)
    private val other = "custom_pinyin_" + "e".repeat(32)
    private val plain = "custom_pinyin_" + "f".repeat(32)
    private val stored = mutableMapOf<String, String>()
    private lateinit var context: android.content.Context

    @Before fun load() {
        val assets = mock<android.content.res.AssetManager>()
        whenever(assets.open("xime.yaml")).thenAnswer { defaults.byteInputStream() }
        whenever(assets.open("xime.custom.yaml")).thenThrow(java.io.IOException("No custom override"))
        // 其他设置（putBoolean 等）只要能链式调用，不记。
        val editor = mock<android.content.SharedPreferences.Editor>(defaultAnswer = Mockito.RETURNS_SELF)
        val pending = mutableMapOf<String, String?>()
        whenever(editor.putString(any(), anyOrNull())).thenAnswer { pending[it.getArgument<String>(0)] = it.getArgument<String?>(1); editor }
        whenever(editor.remove(any())).thenAnswer { pending[it.getArgument<String>(0)] = null; editor }
        fun flush() { pending.forEach { (k, v) -> if (v == null) stored.remove(k) else stored[k] = v }; pending.clear() }
        whenever(editor.commit()).thenAnswer { flush(); true }
        doAnswer { flush(); null }.whenever(editor).apply()
        val prefs = mock<android.content.SharedPreferences>()
        whenever(prefs.contains(any())).thenAnswer { stored.containsKey(it.getArgument<String>(0)) }
        whenever(prefs.getString(any(), anyOrNull())).thenAnswer { stored[it.getArgument<String>(0)] ?: it.getArgument<String?>(1) }
        whenever(prefs.all).thenAnswer { stored.toMap() }
        whenever(prefs.edit()).thenReturn(editor)
        context = mock<android.content.Context>()
        whenever(context.assets).thenReturn(assets)
        whenever(context.filesDir).thenReturn(temp.root)
        whenever(context.getSharedPreferences("kime_settings", android.content.Context.MODE_PRIVATE)).thenReturn(prefs)
        // 两个自建的合并布局（qw、as 合并 / zx 合并），一个全是单字母的。
        File(temp.root, "custom-keyboard-layouts.json").writeText("""[""" +
            """{"formatVersion":2,"id":"$merged","name":"我的合并","rows":"qw,e,r,t,y,u,i,o,p/as,d,f,g,h,j,k,l/z,x,c,v,b,n,m"},""" +
            """{"formatVersion":2,"id":"$other","name":"另一个","rows":"q,w,e,r,t,y,u,i,o,p/a,s,d,f,g,h,j,k,l/zx,c,v,b,n,m"},""" +
            """{"formatVersion":2,"id":"$plain","name":"单字母","rows":"q,w,e,r,t,y,u,i,o,p/a,s,d,f,g,h,j,k,l/z,x,c,v,b,n,m"}]""")
        CustomKeyboardLayouts.load(context)
        KeysConfigHelper.loadConfig(context)
    }

    @After fun reset() {
        File(temp.root, "custom-keyboard-layouts.json").writeText("[]")
        CustomKeyboardLayouts.load(context)
        KeysConfigHelper.setActiveKeyboardSchema("")
        KeysConfigHelper.applyLetterLayouts()
    }

    private fun layout(id: String) = CustomKeyboardLayouts.find(id)!!
    private fun use(schema: String) {
        KeysConfigHelper.setActiveKeyboardSchema(schema)
        KeysConfigHelper.applyLetterLayouts()
    }
    private fun up(key: String) = KeysConfigHelper.getKeyGesture(key, false)!!.swipeUp!!
    private fun down(key: String) = KeysConfigHelper.getKeyGesture(key, false)!!.swipeDown!!

    @Test fun onlyLayoutsWithMergedKeysGetTheirOwnPage() {
        assertTrue(CustomLayoutSymbols.editable(layout(merged)))
        assertTrue(CustomLayoutSymbols.editable(layout(other)))
        // 全是单字母的按 26 键位置出角标，在「中文26键」里改。
        assertFalse(CustomLayoutSymbols.editable(layout(plain)))
        // 设置页画的键和键盘排预设用的是同一串读顺序。
        val keys = CustomLayoutSymbols.keys(layout(merged))
        assertEquals(listOf("qw", "e", "r", "t", "y", "u", "i", "o", "p", "as", "d"), keys.take(11))
        assertEquals(24, keys.size)
        assertEquals(keys, CustomLayoutSymbols.rows(layout(merged)).flatten())
    }

    @Test fun restoringAndDeletingDropTheLayoutsChanges() {
        val layout = layout(merged)
        CustomLayoutSymbols.save(context, layout, mapOf("up.qw" to "★"))
        assertEquals(mapOf("up.qw" to "★"), CustomLayoutSymbols.load(context, layout))
        // 恢复预设：清掉修改。
        CustomLayoutSymbols.save(context, layout, emptyMap())
        use(merged)
        assertEquals("1", up("qw").value)
        // 拆开 qw 之后，旧的 qw 修改不再算这个布局的，保存时一并清掉；删布局时它的修改全删掉。
        CustomLayoutSymbols.save(context, layout, mapOf("up.qw" to "★", "up.e" to "☆"))
        val split = layout.split("qw")
        CustomLayoutSymbols.save(context, split, CustomLayoutSymbols.load(context, split))
        assertEquals(mapOf("up.e" to "☆"), CustomLayoutSymbols.load(context, layout))
        CustomLayoutSymbols.clear(context, merged)
        assertTrue(stored.keys.none { merged in it })
    }
}
