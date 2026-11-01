package com.kingzcheung.xime.settings

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 「候选里显示表情」（默认关）：26 键雾凇方案自带表情过滤，九键、14 键方案补上同一个过滤和开关（GitHub #2）。 */
class CandidateEmojiSchemaTest {
    private val chinese = File("src/main/assets/rime_chinese")

    @Test fun rimeIceKeepsItsEmojiFilterAndTable() {
        val root = File("build/generated/chinese-assets/rime_ice")
        assertTrue(File(root, "rime_ice.schema.yaml").readText().contains("    - simplifier@emoji"))
        assertTrue(File(root, "opencc/emoji.json").isFile)
        assertTrue(File(root, "opencc/emoji.txt").isFile)
        assertTrue(File(root, "opencc/others.txt").isFile)
    }
}
