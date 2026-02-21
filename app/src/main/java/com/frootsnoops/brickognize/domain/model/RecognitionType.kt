package com.frootsnoops.brickognize.domain.model

enum class RecognitionType(val apiPath: String) {
    PARTS("parts"),
    SETS("sets"),
    FIGS("figs");
    
    companion object {
        fun fromString(value: String): RecognitionType {
            return entries.find { it.apiPath.equals(value, ignoreCase = true) } ?: PARTS
        }
    }
}
