package com.whitedns.vpn

import android.content.Context

/**
 * Географическая карта мира для экрана «Карта каналов».
 * Данные: assets/world_map.bin — компактный бинарный формат ZWM1 (169 стран, полигоны + якоря).
 * Формат (little-endian): magic "ZWM1" | u16 count | per country:
 *   char[2] code | u16 anchorX | u16 anchorY(*0.5) | u8 ringCount | per ring: u16 pointCount | points (u16 x, u16 y*0.5)
 * Координаты нормализованы: X 0..65535, Y 0..32767 — делим на максимумы для 0..1.
 */
object WorldMapAsset {

    class Country(val code: String, val anchorX: Float, val anchorY: Float, val rings: Array<FloatArray>)

    @Volatile
    private var cached: List<Country>? = null

    fun countries(context: Context): List<Country> {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val list = runCatching { parse(context.assets.open("world_map.bin").use { it.readBytes() }) }
                .getOrDefault(emptyList())
            cached = list
            return list
        }
    }

    fun parse(data: ByteArray): List<Country> {
        if (data.size <= 6 || data[0] != 'Z'.code.toByte() || data[1] != 'W'.code.toByte() ||
            data[2] != 'M'.code.toByte() || data[3] != '1'.code.toByte()) return emptyList()
        var pos = 4
        fun u16(): Int { val v = (data[pos].toInt() and 0xFF) or ((data[pos + 1].toInt() and 0xFF) shl 8); pos += 2; return v }
        fun u8(): Int { val v = data[pos].toInt() and 0xFF; pos += 1; return v }
        val count = u16()
        val out = ArrayList<Country>(count)
        repeat(count) {
            val code = String(byteArrayOf(data[pos], data[pos + 1]), Charsets.US_ASCII); pos += 2
            val ax = u16().toFloat()
            val ay = u16().toFloat() * 0.5f
            val ringsN = u8()
            val rings = ArrayList<FloatArray>(ringsN)
            repeat(ringsN) {
                val pts = u16()
                val arr = FloatArray(pts * 2)
                for (i in 0 until pts) {
                    arr[i * 2] = u16().toFloat()
                    arr[i * 2 + 1] = u16().toFloat() * 0.5f
                }
                rings.add(arr)
            }
            out.add(Country(code, ax, ay, rings.toTypedArray()))
        }
        return out
    }

    /** ISO-код страны -> якорь (0..1), или null. */
    fun anchorOf(code: String?, list: List<Country>): FloatArray? {
        if (code == null || code.length != 2) return null
        val target = code.uppercase()
        for (c in list) if (c.code == target) return floatArrayOf(c.anchorX / 65535f, c.anchorY / 32767.5f)
        return null
    }

    /** Эмодзи-флаг -> ISO-код (регион-индикаторы). */
    fun flagToCode(flag: String): String? {
        if (flag.length != 2) return null
        val c0 = flag[0].code - 0x1F1E6
        val c1 = flag[1].code - 0x1F1E6
        if (c0 !in 0..25 || c1 !in 0..25) return null
        return "${'A' + c0}${'A' + c1}"
    }

    /** Вытащить первый флаг-эмодзи из имени канала. */
    fun flagFromName(name: String): String? {
        for (i in name.indices) {
            val c = name[i]
            if (c.code in 0x1F1E6..0x1F1FF && i + 1 < name.length && name[i + 1].code in 0x1F1E6..0x1F1FF) {
                return name.substring(i, i + 2)
            }
        }
        return null
    }
}
