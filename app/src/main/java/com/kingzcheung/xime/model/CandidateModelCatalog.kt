package com.kingzcheung.xime.model

/** Optional scoring resources; ordinary Chinese dictionaries do not depend on these downloads. */
object CandidateModelCatalog {
    const val GRAMMAR = "t9-grammar"
    const val SENTENCE = "t9-sentence"
    const val NGRAM = "t9-ngram"
    const val NGRAM_SMALL = "t9-ngram-small"
    private const val SOURCE = "https://raw.githubusercontent.com/Cyletix/CyIME/6cc85b1c08a90a68eca0bd8d018fdb33e7e3d791/app/src/main/assets/rime_chinese/"
    /** JO-IME 不再提供九键模型（九键已砍），清单为空。 */
    val models = emptyList<ModelInfo>()
}
