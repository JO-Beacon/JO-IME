package com.kingzcheung.xime.ui.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputGestureLocksTest {
    private val base = KeyboardInputPreferences(cursorGesture = CursorGestureMode.SPACE)

    @Test fun `the setting chosen first locks the conflicting one and names itself`() {
        val longPress = base.copy(symbolInputMode = SymbolInputMode.LONG_PRESS)
        val bubbleLock = InputGestureLocks.lockFor(longPress, InputGestureLock.Option.LETTER_LONG_PRESS_MENU, true)
        assertNotNull(bubbleLock)
        assertEquals("已被「角标输入方式：长按」锁定", bubbleLock!!.message)
        assertNotNull(InputGestureLocks.lockFor(base.copy(letterLongPressMenu = true),
            InputGestureLock.Option.SYMBOL_INPUT_MODE, SymbolInputMode.LONG_PRESS))
        assertNotNull(InputGestureLocks.lockFor(base.copy(glideTyping = true),
            InputGestureLock.Option.CURSOR_GESTURE, CursorGestureMode.KEYBOARD))
        assertNotNull(InputGestureLocks.lockFor(base.copy(cursorGesture = CursorGestureMode.KEYBOARD),
            InputGestureLock.Option.GLIDE_TYPING, true))
        // 没有冲突的值不锁。
        assertNull(InputGestureLocks.lockFor(base, InputGestureLock.Option.LETTER_LONG_PRESS_MENU, true))
    }

    /** 用户 10-31：「这个开了那个就开不了」——双拼键位提示和上下角标提示、两个占 Shift 键的开关各自互锁。 */
    @Test fun `key hints and corner hints lock each other, so do the two shift key options`() {
        // 默认角标开着：键位提示被锁，原因写出是哪个角标。
        val hintLock = InputGestureLocks.lockFor(base, InputGestureLock.Option.DOUBLE_PINYIN_KEY_HINTS, true)
        assertEquals("已被「上方、下方角标提示」锁定", hintLock?.message)
        assertEquals("已被「下方角标提示」锁定", InputGestureLocks.lockFor(base.copy(swipeUpHints = false),
            InputGestureLock.Option.DOUBLE_PINYIN_KEY_HINTS, true)?.message)
        val clean = base.copy(swipeUpHints = false, swipeDownHints = false)
        assertNull(InputGestureLocks.lockFor(clean, InputGestureLock.Option.DOUBLE_PINYIN_KEY_HINTS, true))
        // 键位提示开着：两个角标提示都不能开。
        assertEquals("已被「双拼键位提示」锁定", InputGestureLocks.lockFor(clean.copy(doublePinyinKeyHints = true),
            InputGestureLock.Option.CORNER_HINTS, true)?.message)
        assertNull(InputGestureLocks.lockFor(clean, InputGestureLock.Option.CORNER_HINTS, true))
        // 用户 10-31：「和角标一起显示」开着时分层排开，两边都不锁。
        val together = base.copy(doublePinyinKeyHintsWithCorners = true)
        assertNull(InputGestureLocks.lockFor(together, InputGestureLock.Option.DOUBLE_PINYIN_KEY_HINTS, true))
        assertNull(InputGestureLocks.lockFor(together.copy(doublePinyinKeyHints = true, swipeUpHints = false,
            swipeDownHints = false), InputGestureLock.Option.CORNER_HINTS, true))
        // Shift：先开的锁住另一个。
        assertEquals("已被「ing 放在 Shift 键上」锁定", InputGestureLocks.lockFor(base.copy(doublePinyinSemicolonOnShift = true),
            InputGestureLock.Option.SHIFT_SYLLABLE_SPLIT, true)?.message)
        assertEquals("已被「Shift 键分词」锁定", InputGestureLocks.lockFor(base.copy(shiftSyllableSplit = true),
            InputGestureLock.Option.SEMICOLON_ON_SHIFT, true)?.message)
        assertNull(InputGestureLocks.lockFor(base, InputGestureLock.Option.SHIFT_SYLLABLE_SPLIT, true))
        assertNull(InputGestureLocks.lockFor(base, InputGestureLock.Option.SEMICOLON_ON_SHIFT, true))
        // 字母键大写：一律大写和中文大写只能开一个，先开的锁住另一个。
        assertEquals("已被「字母键显示大写」锁定", InputGestureLocks.lockFor(base.copy(uppercaseKeyLabels = true),
            InputGestureLock.Option.CHINESE_UPPERCASE_KEY_LABELS, true)?.message)
        assertEquals("已被「中文键盘字母显示大写」锁定", InputGestureLocks.lockFor(base.copy(chineseUppercaseKeyLabels = true),
            InputGestureLock.Option.UPPERCASE_KEY_LABELS, true)?.message)
        assertNull(InputGestureLocks.lockFor(base, InputGestureLock.Option.UPPERCASE_KEY_LABELS, true))
        assertNull(InputGestureLocks.lockFor(base, InputGestureLock.Option.CHINESE_UPPERCASE_KEY_LABELS, true))
    }

    @Test fun `choosing long press turns nine key flick on`() {
        assertTrue(InputGestureLocks.withSymbolInputMode(base, SymbolInputMode.LONG_PRESS).t9FlickLetters)
        assertEquals(false, InputGestureLocks.withSymbolInputMode(base, SymbolInputMode.SWIPE_UP).t9FlickLetters)
    }
}
