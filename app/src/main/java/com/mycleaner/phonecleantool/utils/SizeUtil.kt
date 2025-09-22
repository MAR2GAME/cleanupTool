package com.mycleaner.phonecleantool.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

object SizeUtil {
    const val sz1B: Long = 1L
    const val sz1GB: Long = 0x40000000L
    const val sz1KB: Long = 0x400L
    const val sz1MB: Long = 0x100000L
    const val sz1TB: Long = 0x10000000000L

    fun _formatSizeDecimalPartOnly(arg2: Long): String {
        val v0 = if (arg2 > 0x40000000L) formatSize_3(arg2) else formatSizeDecimalPartOnly(arg2)
        return v0
    }

    fun formatSize(arg10: Long, arg12: Int): String {
        val v0: String
        if (arg10 < 0L) {
            v0 = "" + arg10
        } else if (arg10 >= 0x40000000L) {
            val v0_1 = arg10 % 0x40000000L
            val v2 = 10L * v0_1 % 0x40000000L
            v0 = String.format(
                "%sGB",
                BigDecimal((arg10 / 0x40000000L).toString() + "." + (v0_1 * 10L / 0x40000000L).toString() + (v2 * 10L / 0x40000000L).toString() + (10L * (10L * v2 % 0x40000000L) / 0x40000000L).toString()).setScale(
                    arg12,
                    4
                ).toString()
            )
        } else if (arg10 >= 0x100000L) {
            val v0_2 = arg10 % 0x100000L
            val v2_1 = 10L * v0_2 % 0x100000L
            v0 = String.format(
                "%sMB",
                BigDecimal((arg10 / 0x100000L).toString() + "." + (v0_2 * 10L / 0x100000L).toString() + (v2_1 * 10L / 0x100000L).toString() + (10L * (10L * v2_1 % 0x100000L) / 0x100000L).toString()).setScale(
                    arg12,
                    4
                ).toString()
            )
        } else if (arg10 >= 0x400L) {
            val v0_3 = arg10 % 0x400L
            val v2_2 = 10L * v0_3 % 0x400L
            v0 = String.format(
                "%sKB",
                BigDecimal((arg10 / 0x400L).toString() + "." + (v0_3 * 10L / 0x400L).toString() + (v2_2 * 10L / 0x400L).toString() + (10L * (10L * v2_2 % 0x400L) / 0x400L).toString()).setScale(
                    arg12,
                    4
                ).toString()
            )
        } else if (arg10 == 0L) {
            v0 = "0 KB"
        } else {
            v0 = "0 KB"
        }

        return v0
    }

    fun formatSize(arg8: Long, arg10: String?): String {
        var v0: Float
        var v1: String? = null
        if (arg8 >= 0x400L) {
            v1 = "KB"
            v0 = (((arg8.toDouble())) / 1024).toFloat()
            if (v0 >= 1024f) {
                v1 = "MB"
                v0 /= 1024f
            }

            if (v0 >= 1024f) {
                v1 = "GB"
                v0 /= 1024f
            }
        } else {
            v0 = arg8.toFloat()
        }

        val v3 = StringBuilder(DecimalFormat(arg10).format((v0.toDouble())))
        if (v1 == null) {
            v3.append("B")
        } else {
            v3.append(v1)
        }

        return v3.toString()
    }

    fun formatSize2(arg2: Long): String {
        return formatSize(arg2, "#0.0")
    }

    fun formatSize2MB(arg6: Long): String {
        var v0 = "0MB"
        if (arg6 > 0L) {
            val v1 = DecimalFormat("#0.##")
            v0 = v1.format(((((arg6.toFloat())) / 1048576f).toDouble())) + "MB"
        }

        return v0
    }

    fun formatSize3(arg6: Long): String {
        return formatSize(arg6, 2)
//        val v0: String?
//        val v1: String
//        val v2: Float
//        if (arg6 >= 0x3E800000L) {
//            v2 = (((arg6.toDouble())) / 1073741824).toFloat()
//            v1 = "GB"
//        } else if (arg6 >= 0xFA000L) {
//            v2 = (((arg6.toDouble())) / 1048576).toFloat()
//            v1 = "MB"
//        } else {
//            v2 = (((arg6.toDouble())) / 1024).toFloat()
//            v1 = "KB"
//        }
//
//        if (v2 > 100f) {
//            v0 = "#0"
//        } else if (v2 > 10f) {
//            v0 = "#0.0"
//        } else {
//            v0 = "#0.00"
//        }
//
//        val v3 = DecimalFormat(v0)
//        val v0_1 = v3.getDecimalFormatSymbols()
//        v0_1.setDecimalSeparator('.')
//        v3.setDecimalFormatSymbols(v0_1)
//        val v0_2 = v3.format((v2.toDouble()))
//        return v0_2.replace("-".toRegex(), ".") + v1
    }

    fun formatSizeDecimalPartOnly(arg2: Long): String {
        return formatSize(arg2, "#0")
    }

    fun formatSizeFix2(arg6: Long): String {
        var v0: Float
        var v1: String? = null
        if (arg6 >= 0x400L) {
            v1 = "KB"
            v0 = (arg6 / 0x400L).toFloat()
            if (v0 >= 1024f) {
                v1 = "MB"
                v0 /= 1024f
            }

            if (v0 >= 1024f) {
                v1 = "GB"
                v0 /= 1024f
            }
        } else {
            v0 = arg6.toFloat()
        }

        val v3 = StringBuilder(DecimalFormat("#0.00").format((v0.toDouble())))
        if (v1 != null) {
            v3.append(v1)
        }

        return v3.toString()
    }

    fun formatSizeFloatSingle(arg2: Long): String {
        return formatSize(arg2, 1)
    }

    fun formatSizeForJunkHeader(arg8: Long): String {
        val v0_1: String?
        val v1_1: Float
        val v2: String?
        if (arg8 >= 1000L) {
            var v1 = "KB"
            var v0 = (((arg8.toDouble())) / 1024).toFloat()
            if (v0 >= 1000f) {
                v1 = "MB"
                v0 /= 1024f
            }

            if (v0 >= 1000f) {
                v2 = "GB"
                v1_1 = v0 / 1024f
            } else {
                v2 = v1
                v1_1 = v0
            }
        } else {
            v2 = "KB"
            v1_1 = (((arg8.toDouble())) / 1024).toFloat()
        }

        if (v1_1 > 100f) {
            v0_1 = "#0"
        } else if (v1_1 > 10f) {
            v0_1 = "#0.0"
        } else {
            v0_1 = "#0.00"
        }

        val v3 = DecimalFormat(v0_1)
        val v0_2 = v3.getDecimalFormatSymbols()
        v0_2.setDecimalSeparator('.')
        v3.setDecimalFormatSymbols(v0_2)
        val v0_3 = StringBuilder(v3.format((v1_1.toDouble())))
        v0_3.append(v2)
        return v0_3.toString().replace("-".toRegex(), ".")
    }

    fun formatSizeForMiui(arg2: Long): String {
        var v0_1: String
        try {
            Class.forName("miui.text.util.MiuiFormatter")
            v0_1 = miuiFormatSize(arg2, "#0.0")
        } catch (v0: ClassNotFoundException) {
            v0_1 = formatSize(arg2, "#0.0")
        }

        return v0_1
    }

    fun formatSizeGB(arg6: Long): String {
        val v0: String
        if (arg6 > 0x400L) {
            val v1 = DecimalFormat("#0.0")
            v0 = v1.format(((((arg6.toFloat())) / 1024f).toDouble())) + "GB"
        } else {
            v0 = arg6.toString() + "MB"
        }

        return v0
    }

    fun formatSizeGetUnit(arg8: Long): String {
        val v1: Float
        var v0_1: String
        if (arg8 >= 0x400L) {
            val v0 = (((arg8.toDouble())) / 1024).toFloat()
            if (v0 >= 1024f) {
                v0_1 = "MB"
                v1 = v0 / 1024f
            } else {
                v0_1 = "KB"
                v1 = v0
            }

            if (v1 >= 1024f) {
                v0_1 = "GB"
                val v1_1 = v1 / 1024f
            }
        } else {
            v0_1 = "B"
        }

        return v0_1
    }

    fun formatSizeInt(arg14: Long): String {
        val v0: String
        if (arg14 < 0L) {
            v0 = "" + arg14
        } else if (arg14 >= 0x10000000000L) {
            v0 = String.format(
                Locale.US,
                "%d.%dTB",
                arg14 / 0x10000000000L,
                arg14 % 0x10000000000L * 10L / 0x10000000000L
            )
        } else if (arg14 >= 0x40000000L) {
            v0 = String.format(
                Locale.US,
                "%d.%dGB",
                arg14 / 0x40000000L,
                arg14 % 0x40000000L * 10L / 0x40000000L
            )
        } else if (arg14 >= 0x100000L) {
            v0 = String.format(
                Locale.US,
                "%d.%dMB",
                arg14 / 0x100000L,
                arg14 % 0x100000L * 10L / 0x100000L
            )
        } else if (arg14 >= 0x400L) {
            v0 = String.format(Locale.US, "%d.%dKB", arg14 / 0x400L, arg14 % 0x400L * 10L / 0x400L)
        } else if (arg14 == 0L) {
            v0 = "0KB"
        } else {
            v0 = "< 1KB"
        }

        return v0
    }


    fun formatSizeMB(arg4: Long): Float {
        var v0 = 0f
        if (arg4 > 0L) {
            v0 = BigDecimal(((((arg4.toFloat())) / 1048576f).toDouble())).setScale(2, 4).toFloat()
        }

        return v0
    }

    fun formatSizeSmallestMBUnit(arg6: Long): String {
        val v0: String?
        val v1: String
        val v2: Float
        if (arg6 >= 0x3E800000L) {
            v2 = (((arg6.toDouble())) / 1073741824).toFloat()
            v1 = "GB"
        } else {
            v2 = (((arg6.toDouble())) / 1048576).toFloat()
            v1 = "MB"
        }

        if (v2 >= 100f) {
            v0 = "#0"
        } else if (v2 >= 10f) {
            v0 = "#0.0"
        } else {
            v0 = "#0.00"
        }

        val v3 = DecimalFormat(v0)
        val v0_1 = v3.getDecimalFormatSymbols()
        v0_1.setDecimalSeparator('.')
        v3.setDecimalFormatSymbols(v0_1)
        val v0_2 = v3.format((v2.toDouble())).replace("-".toRegex(), ".")
        return v0_2 + v1
    }

    fun formatSizeSmallestMBUnit2(arg8: Long): String {
        val v1: Float
        val v0: String
        if (arg8 >= 0x3E800000L) {
            v0 = "GB"
            v1 = (((arg8.toDouble())) / 1073741824).toFloat()
        } else {
            v0 = "MB"
            v1 = (((arg8.toDouble())) / 1048576).toFloat()
        }

        val v3 = DecimalFormat("#0.00")
        val v2 = v3.getDecimalFormatSymbols()
        v2.setDecimalSeparator('.')
        v3.setDecimalFormatSymbols(v2)
        val v1_1 = v3.format((v1.toDouble())).replace("-".toRegex(), "")
        return v1_1 + v0
    }


    fun formatSizeWithoutSuffix(arg6: Long): String {
        var v0: Float
        if (arg6 >= 0x400L) {
            v0 = (((arg6.toDouble())) / 1024).toFloat()
            if (v0 >= 1024f) {
                v0 /= 1024f
            }
        } else {
            v0 = arg6.toFloat()
        }

        return StringBuilder(DecimalFormat("#0.0").format((v0.toDouble()))).toString()
    }

    fun formatSizeWithoutSuffix2(arg6: Long): String {
        var v0: Float
        if (arg6 >= 0x400L) {
            v0 = (((arg6.toDouble())) / 1024).toFloat()
            if (v0 >= 1024f) {
                v0 /= 1024f
            }

            if (v0 >= 1024f) {
                v0 /= 1024f
            }
        } else {
            v0 = arg6.toFloat()
        }

        return StringBuilder(DecimalFormat("#0.0").format((v0.toDouble()))).toString()
    }

    fun formatSize_1(arg2: Long): String {
        return formatSize(arg2, 2)
    }

    fun formatSize_2(arg6: Long): String {
        val v0: String?
        val v1: String
        val v2: Float
        if (arg6 >= 0x3E800000L) {
            v2 = (((arg6.toDouble())) / 1073741824).toFloat()
            v1 = "GB"
        } else if (arg6 >= 0xFA000L) {
            v2 = (((arg6.toDouble())) / 1048576).toFloat()
            v1 = "MB"
        } else {
            v2 = (((arg6.toDouble())) / 1024).toFloat()
            v1 = "KB"
        }

        if (v2 >= 100f) {
            v0 = "#0"
        } else if (v2 >= 10f) {
            v0 = "#0.0"
        } else {
            v0 = "#0.00"
        }

        val v3 = DecimalFormat(v0)
        val v0_1 = v3.getDecimalFormatSymbols()
        v0_1.setDecimalSeparator('.')
        v3.setDecimalFormatSymbols(v0_1)
        val v0_2 = v3.format((v2.toDouble())).replace("-".toRegex(), ".")
        return v0_2 + v1
    }

    fun formatSize_3(arg2: Long): String {
        return formatSize(arg2, "#0.00")
    }

    private fun miuiFormatSize(arg8: Long, arg10: String?): String {
        var v0: Float
        var v1: String? = null
        if (arg8 >= 1000L) {
            v1 = "KB"
            v0 = (((arg8.toDouble())) / 1000).toFloat()
            if (v0 >= 1000f) {
                v1 = "MB"
                v0 /= 1000f
            }

            if (v0 >= 1000f) {
                v1 = "GB"
                v0 /= 1000f
            }
        } else {
            v0 = arg8.toFloat()
        }

        val v3 = StringBuilder(DecimalFormat(arg10).format((v0.toDouble())))
        if (v1 == null) {
            v3.append("B")
        } else {
            v3.append(v1)
        }

        return v3.toString()
    }
}

