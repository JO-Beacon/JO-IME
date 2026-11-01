package com.kingzcheung.xime.speech

import android.content.Context
import android.content.SharedPreferences
import com.kingzcheung.xime.settings.InputLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.*
import java.io.File

class SpeechLanguagesTest {
    @get:Rule val files = TemporaryFolder()

    @Test fun japaneseLightModelUsesQuantizedFilesAndNeverRequiresSenseVoice() {
        val context = mock<Context>()
        whenever(context.filesDir).thenReturn(files.root)
        val manager = AsrModelManager(context)
        val small = manager.selection(SpeechModelCatalog.ZIPFORMER_JA_INT8, InputLanguage.JAPANESE)
        val original = manager.selection(SpeechModelCatalog.ZIPFORMER_JA, InputLanguage.JAPANESE)
        assertEquals("offline-transducer", small.first!!.modelType)
        assertTrue(small.first.encoderFile.endsWith(".int8.onnx"))
        assertTrue(small.first.decoderFile.endsWith(".int8.onnx"))
        assertTrue(small.first.joinerFile.endsWith(".int8.onnx"))
        assertNull(small.secondDir)
        assertNotEquals(small.firstDir, original.firstDir)
        assertFalse(small.ready)
        assertFalse(SpeechLanguages.supports(SpeechModelCatalog.ZIPFORMER_JA_INT8, InputLanguage.CHINESE))
        assertFalse(SpeechModelSelection.supportsCorrection(SpeechModelCatalog.ZIPFORMER_JA_INT8))
        val manifest = SpeechModelCatalog.models.first { it.id == SpeechModelCatalog.ZIPFORMER_JA_INT8 }
        assertTrue(manifest.files.sumOf { it.sizeBytes } < 170_000_000L)
    }

    @Test fun primaryAndCorrectionMustBothSupportTheLanguage() {
        assertTrue(SpeechLanguages.supports(SpeechModelCatalog.ZIPFORMER_TWO_PASS, InputLanguage.CHINESE))
        assertFalse(SpeechLanguages.supports(SpeechModelCatalog.ZIPFORMER_TWO_PASS, InputLanguage.ENGLISH))
        assertFalse(SpeechLanguages.supports(SpeechModelCatalog.TWO_PASS, InputLanguage.JAPANESE))
        assertTrue(SpeechLanguages.supports(SpeechModelCatalog.TWO_PASS, InputLanguage.ENGLISH))
        InputLanguage.speech.forEach { assertTrue(SpeechLanguages.supports(SpeechModelCatalog.SENSEVOICE, it)) }
        // 没有西班牙语识别器：不能把西班牙语交给 SenseVoice 当中文识别。
        SpeechModelSelection.primaryIds.forEach { assertFalse(SpeechLanguages.supports(it, InputLanguage.SPANISH)) }
        InputLanguage.entries.forEach { assertFalse(SpeechLanguages.supports("unknown-model", it)) }
        SpeechModelSelection.primaryIds.forEach { assertFalse(SpeechLanguages.supports(it, InputLanguage.UNSPECIFIED)) }
    }

    @Test fun compatibleChoiceIsKeptAndItsMissingFilesAreNotSilentlyReplaced() {
        assertEquals(SpeechModelCatalog.TWO_PASS, SpeechLanguages.select(InputLanguage.ENGLISH, SpeechModelCatalog.TWO_PASS) { true })
        val checked = mutableListOf<String>()
        val error = assertThrows(IllegalStateException::class.java) {
            SpeechLanguages.select(InputLanguage.CHINESE, SpeechModelCatalog.ZIPFORMER) { checked += it; false }
        }
        assertEquals(listOf(SpeechModelCatalog.ZIPFORMER), checked)
        assertTrue(error.message!!.contains("尚未完整下载"))
    }

    @Test fun incompatibleExplicitSelectionIsRejectedBeforeFileAccess() {
        val context = mock<Context>()
        assertThrows(IllegalArgumentException::class.java) {
            AsrModelManager(context).selection(SpeechModelCatalog.ZIPFORMER_TWO_PASS, InputLanguage.JAPANESE)
        }
        verifyNoInteractions(context)
    }
}
