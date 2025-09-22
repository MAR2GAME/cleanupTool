package com.mycleaner.phonecleantool.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.mycleaner.phonecleantool.R
import com.mycleaner.phonecleantool.bean.BatteryInfo

import java.lang.reflect.Method

class BatteryInfoHelper(private val context: Context) {

    /**
     * 获取电池信息
     */
    fun getBatteryInfo(): BatteryInfo {
        val batteryStatus = getBatteryStatusIntent()
        val level = getBatteryLevel(batteryStatus)
        val capacity = getBatteryCapacityUsingReflection()
        return BatteryInfo(
            level,
            scale = getBatteryScale(batteryStatus),
            health = getBatteryHealth(batteryStatus),
//            status = getChargingStatus(batteryStatus),
//            plugged = getPlugType(batteryStatus),
            voltage = getBatteryVoltage(batteryStatus),
            temperature = getBatteryTemperature(batteryStatus) / 10f,
            technology = getBatteryTechnology(batteryStatus),
            capacity,
            availableTime = getRemainingTimeEstimate(level)
        )
    }


    /**
     * 获取电池状态 Intent
     */
    private fun getBatteryStatusIntent(): Intent {
        return context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: Intent()
    }

    /**
     * 获取当前电池电量百分比
     */
    private fun getBatteryLevel(batteryStatus: Intent): Int {
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }


    private var lastBatteryLevel: Int? = null
    private var lastTimeRecorded: Long? = null
    private var consumptionRatePerMinute: Float? = null // 每分钟消耗的电量百分比

    /**
     * 更新电量消耗速率。
     * 需要定期调用（例如每隔几分钟），或者在收到电池变化广播时调用。
     */
    fun updateConsumptionRate(currentLevel: Int) {
        val currentTime = System.currentTimeMillis()
        lastBatteryLevel?.let { lastLevel ->
            lastTimeRecorded?.let { lastTime ->
                // 计算电量差值（百分比）和时间差值（分钟）
                val levelDiff =
                    (lastLevel - currentLevel).coerceAtLeast(0) // 确保不为负，通常放电时 currentLevel 会减小
                val timeDiffMinutes = (currentTime - lastTime) / (1000f * 60f) // 毫秒转换为分钟

                if (levelDiff > 0 && timeDiffMinutes > 0) {
                    // 计算平均每分钟消耗的电量百分比
                    consumptionRatePerMinute = levelDiff / timeDiffMinutes
                }
            }
        }
        // 更新上一次的电量和时间记录
        lastBatteryLevel = currentLevel
        lastTimeRecorded = currentTime
    }

    /**
     * 估算剩余时间（单位：分钟）。
     * 返回值为 Float 类型，可能为 null（如果无法估算）。
     */
    fun estimateRemainingTimeMinutes(currentLevel: Int): Float? {
        return consumptionRatePerMinute?.let { rate ->
            if (rate > 0) {
                currentLevel / rate // 当前电量百分比 / 每分钟消耗的百分比
            } else {
                null // 如果速率为零或负，无法估算（可能正在充电或速率无效）
            }
        }
    }

    /**
     * 格式化剩余时间（小时和分钟）。
     */
    fun formatRemainingTime(minutes: Float): String {
        val totalMinutes = minutes.toInt()
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return "${hours}h${mins}m"
    }


    /**
     * 获取电池最大刻度值
     */
    private fun getBatteryScale(batteryStatus: Intent): Int {
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    }

    /**
     * 获取电池健康状态
     */
    private fun getBatteryHealth(batteryStatus: Intent): String {
        return when (batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.overcooling)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.damage)
            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.good)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.overheat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.overvoltage)
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> context.getString(R.string.unknown)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.fault)
            else -> context.getString(R.string.unknown)
        }
    }

    /**
     * 获取充电状态
     */
//    private fun getChargingStatus(batteryStatus: Intent): String {
//        return when (batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
//            BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
//            BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
//            BatteryManager.BATTERY_STATUS_FULL -> "已充满"
//            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
//            BatteryManager.BATTERY_STATUS_UNKNOWN -> "未知状态"
//            else -> "未知状态"
//        }
//    }

    /**
     * 获取充电方式
     */
//    private fun getPlugType(batteryStatus: Intent): String {
//        return when (batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
//            BatteryManager.BATTERY_PLUGGED_AC -> "交流充电"
//            BatteryManager.BATTERY_PLUGGED_USB -> "USB充电"
//            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
//            else -> "未充电"
//        }
//    }

    /**
     * 获取电池电压
     */
    private fun getBatteryVoltage(batteryStatus: Intent): Int {
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
    }

    /**
     * 获取电池温度
     */
    private fun getBatteryTemperature(batteryStatus: Intent): Int {
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
    }

    /**
     * 获取电池技术类型
     */
    private fun getBatteryTechnology(batteryStatus: Intent): String {
        return batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            ?: context.getString(R.string.unknown)
    }

    /**
     * 计算电池容量 (mAh)
     * 注意：这个方法可能需要特定权限，且不一定在所有设备上都准确
     */
    private fun calculateBatteryCapacity(): Double? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val chargeCounter =
                    bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                val voltage = getBatteryVoltage(getBatteryStatusIntent()) / 1000.0 // 转换为伏特

                if (chargeCounter > 0 && voltage > 0) {
                    // 容量 (mAh) = 电荷量 (μAh) / 1000 / 电压 (V)
                    chargeCounter / (voltage * 1000.0)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getBatteryCapacityFromPowerProfile(): Int {
        var batteryCapacity = 0.0
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = constructor.newInstance(context)

            val method: Method = powerProfileClass.getMethod("getBatteryCapacity")
            batteryCapacity = method.invoke(powerProfileInstance) as Double
        } catch (e: Exception) {
            e.printStackTrace()
            // 备用方法：尝试使用旧的方法名和字符串参数
            try {
                val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
                val constructor = powerProfileClass.getConstructor(Context::class.java)
                val powerProfileInstance = constructor.newInstance(context)

                val method: Method =
                    powerProfileClass.getMethod("getAveragePower", String::class.java)
                batteryCapacity = method.invoke(powerProfileInstance, "battery.capacity") as Double
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return batteryCapacity.toInt()
    }

    fun getBatteryCapacityUsingReflection(): Int {
        var batteryCapacity = 0.0
        try {
            // 1. 获取 PowerProfile 类
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")

            // 2. 创建 PowerProfile 实例
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = constructor.newInstance(context)

            // 3. 获取 getBatteryCapacity 方法（某些版本可能叫 getAveragePower）
            val method: Method = try {
                // 先尝试 getBatteryCapacity 方法
                powerProfileClass.getMethod("getBatteryCapacity")
            } catch (e: NoSuchMethodException) {
                // 如果不存在，则尝试 getAveragePower 方法
                powerProfileClass.getMethod("getAveragePower", String::class.java)
            }

            // 4. 调用方法获取容量
            batteryCapacity = if (method.name == "getBatteryCapacity") {
                method.invoke(powerProfileInstance) as Double
            } else {
                // 使用 "battery.capacity" 作为参数调用 getAveragePower
                method.invoke(powerProfileInstance, "battery.capacity") as Double
            }


        } catch (e: Exception) {
            e.printStackTrace()
            // 处理异常，例如类找不到、方法不存在等
        }


        if (batteryCapacity == 0.0) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            // 获取当前的电荷计数（单位：微安时，uAh）
            val chargeCounter =
                batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            // 获取当前的电量百分比
            val capacityPercent =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            // 检查设备是否提供了所需的数据
            if (chargeCounter == Long.MIN_VALUE || capacityPercent == Int.MIN_VALUE || capacityPercent == 0) {
                batteryCapacity = 0.0 // 设备不支持，无法计算
            }

            // 核心计算逻辑：总容量 (uAh) = (当前电荷计数 / 当前百分比) * 100
            val totalCapacityMicroAh = (chargeCounter / capacityPercent) * 100

            // 将微安时(uAh)转换为毫安时(mAh)并返回
            batteryCapacity = totalCapacityMicroAh / 1000.toDouble()

        }
        return batteryCapacity.toInt() // 单位通常是 mAh
    }


    /**
     * 检查设备是否正在充电
     */
    fun isCharging(): Boolean {
        val batteryStatus = getBatteryStatusIntent()
        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * 获取电池剩余使用时间估算（分钟）
     * 注意：这是一个粗略估算，实际使用时间会因使用情况而异
     */
    fun getRemainingTimeEstimate(batteryPercent: Int): String {
        val assumedHourlyDrain = 18.5f // 假设每小时平均消耗18.5%的电量
        val estimatedHours = estimateRemainingUsageTimeSimple(batteryPercent, assumedHourlyDrain)
        return formatTime(estimatedHours)
    }

    fun estimateRemainingUsageTimeSimple(
        batteryPercentage: Int,
        assumedDrainPerHour: Float = 10f // 默认假设每小时消耗10%的电量，这个值需要你根据用户使用情况调整
    ): Float {
        if (batteryPercentage <= 0 || assumedDrainPerHour <= 0) {
            return 0f
        }
        return batteryPercentage / assumedDrainPerHour
    }

    private fun formatTime(hours: Float): String {
        val totalMinutes = (hours * 60).toInt()
        val hoursPart = totalMinutes / 60
        val minutesPart = totalMinutes % 60
        return String.format("%dh%dm", hoursPart, minutesPart)
    }
}