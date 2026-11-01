package com.kingzcheung.xime.settings

import org.junit.Assert.*
import org.junit.Test

class InputModesTest {
    private val schemas = listOf(SchemaInfo("rime_ice", "中文 26 键", "", "", ""))

    @Test fun englishIsAvailableWithNoEnabledSchemas() {
        assertEquals(listOf(InputModes.english), InputModes.available(emptyList()))
    }

    @Test fun chineseModeComesBeforeTheBuiltinEnglishEntry() {
        assertEquals(listOf("rime_ice", InputModes.ENGLISH),
            InputModes.available(schemas, listOf("rime_ice")).map { it.schemaId })
    }

    @Test fun currentLanguagePanelOnlyListsThatLanguage() {
        assertEquals(listOf("rime_ice"),
            InputModes.inCurrentLanguage(schemas, "rime_ice").map { it.schemaId })
        assertEquals(listOf(InputModes.ENGLISH),
            InputModes.inCurrentLanguage(schemas, InputModes.ENGLISH).map { it.schemaId })
    }

    @Test fun languageChoicesCoverOnlyChineseAndEnglish() {
        val choices = InputModes.languageChoices(schemas, "rime_ice", emptyMap())
        assertEquals(listOf("中文", "英文"), choices.map { it.name })
        assertEquals(listOf("rime_ice", InputModes.ENGLISH), choices.map { it.schemaId })
    }

    @Test fun languageOrderOnlyKnowsChineseAndEnglish() {
        assertEquals(listOf(InputLanguage.ENGLISH, InputLanguage.CHINESE),
            InputModes.languageOrder(listOf("en", "ja", "unknown")))
        assertEquals(listOf("en", "zh"),
            InputModes.orderWithLanguageLast(listOf("en"), InputLanguage.CHINESE))
    }

    @Test fun mergeOrderKeepsTheEnglishSlot() {
        val current = listOf("rime_ice", InputModes.ENGLISH, "custom_pinyin_0001")
        assertEquals(listOf("custom_pinyin_0001", InputModes.ENGLISH, "rime_ice"),
            InputModes.mergeOrder(current, listOf("custom_pinyin_0001", "rime_ice")))
    }

    @Test fun repeatedNormalizationKeepsOnePermanentEnglishEntry() {
        val available = InputModes.available(listOf(InputModes.english, InputModes.english))
        assertEquals(listOf(InputModes.english), InputModes.available(available))
    }

    @Test fun tapAndMenuUseTheSameEnglishSelectionIdentity() {
        assertEquals(InputModes.ENGLISH, InputModes.selectedId("rime_ice", isAsciiMode = true))
        assertEquals("rime_ice", InputModes.selectedId("rime_ice", isAsciiMode = false))
    }
}
