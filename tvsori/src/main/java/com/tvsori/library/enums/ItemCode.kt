package com.tvsori.library.enums

enum class ItemCode constructor(val code: String) {
    NONE(""), HEARTCON("601"), POINT_ROSE("401");

    companion object {
        private val VALUES = values()

        fun fromValue(value: String): ItemCode {
            for (code in VALUES) {
                if (code.code == value) {
                    return code
                }
            }
            return NONE
        }
    }
}