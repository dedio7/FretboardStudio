package com.dedio.fretboardstudio.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PresetRepository {
    private const val PREFS_NAME = "fretboard_presets_prefs"
    private const val KEY_PRESETS = "key_saved_presets"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        loadFromPrefsIfNeeded()
    }

    private val defaultPresets = listOf(
        ChordPreset(
            id = "preset-1",
            name = "I - IV - V (C - F - G)",
            slots = listOf(
                ChordSlot(root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 0),
                ChordSlot(root = Pitch(5), quality = TriadType.MAJOR, slotIndex = 1),
                ChordSlot(root = Pitch(7), quality = TriadType.MAJOR, slotIndex = 2),
                ChordSlot(root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 3)
            )
        ),
        ChordPreset(
            id = "preset-2",
            name = "Pop (C - G - Am - F)",
            slots = listOf(
                ChordSlot(root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 0),
                ChordSlot(root = Pitch(7), quality = TriadType.MAJOR, slotIndex = 1),
                ChordSlot(root = Pitch(9), quality = TriadType.MINOR, slotIndex = 2),
                ChordSlot(root = Pitch(5), quality = TriadType.MAJOR, slotIndex = 3)
            )
        ),
        ChordPreset(
            id = "preset-3",
            name = "Jazz Turnaround (Am - Dm - G - C)",
            slots = listOf(
                ChordSlot(root = Pitch(9), quality = TriadType.MINOR, slotIndex = 0),
                ChordSlot(root = Pitch(2), quality = TriadType.MINOR, slotIndex = 1),
                ChordSlot(root = Pitch(7), quality = TriadType.MAJOR, slotIndex = 2),
                ChordSlot(root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 3)
            )
        )
    )

    private val memoryCache = mutableListOf<ChordPreset>().apply { addAll(defaultPresets) }
    private var isLoaded = false

    private fun loadFromPrefsIfNeeded() {
        if (isLoaded) return
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_PRESETS, null)
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                val loaded = mutableListOf<ChordPreset>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getString("id")
                    val name = obj.getString("name")
                    val slotsArray = obj.getJSONArray("slots")
                    val slots = mutableListOf<ChordSlot>()
                    for (j in 0 until slotsArray.length()) {
                        val sObj = slotsArray.getJSONObject(j)
                        slots.add(
                            ChordSlot(
                                id = sObj.optString("id", java.util.UUID.randomUUID().toString()),
                                root = Pitch(sObj.getInt("root")),
                                quality = TriadType.valueOf(sObj.getString("quality")),
                                inversion = TriadInversion.valueOf(sObj.optString("inversion", TriadInversion.ALL.name)),
                                baseString = sObj.optInt("baseString", 5),
                                isVisible = sObj.optBoolean("isVisible", true),
                                slotIndex = sObj.optInt("slotIndex", j)
                            )
                        )
                    }
                    loaded.add(ChordPreset(id = id, name = name, slots = slots))
                }
                if (loaded.isNotEmpty()) {
                    memoryCache.clear()
                    memoryCache.addAll(loaded)
                }
            }
        } catch (_: Exception) {
            // fallback to defaults
        }
        isLoaded = true
    }

    private fun saveToPrefs() {
        val ctx = appContext ?: return
        try {
            val jsonArray = JSONArray()
            for (preset in memoryCache) {
                val obj = JSONObject()
                obj.put("id", preset.id)
                obj.put("name", preset.name)
                val slotsArray = JSONArray()
                for (slot in preset.slots) {
                    val sObj = JSONObject()
                    sObj.put("id", slot.id)
                    sObj.put("root", slot.root.semitone)
                    sObj.put("quality", slot.quality.name)
                    sObj.put("inversion", slot.inversion.name)
                    sObj.put("baseString", slot.baseString)
                    sObj.put("isVisible", slot.isVisible)
                    sObj.put("slotIndex", slot.slotIndex)
                    slotsArray.put(sObj)
                }
                obj.put("slots", slotsArray)
                jsonArray.put(obj)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PRESETS, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun getPresets(): List<ChordPreset> {
        loadFromPrefsIfNeeded()
        return memoryCache.toList()
    }

    fun savePreset(preset: ChordPreset) {
        memoryCache.add(preset)
        saveToPrefs()
    }

    fun updatePreset(presetId: String, slots: List<ChordSlot>) {
        val index = memoryCache.indexOfFirst { it.id == presetId }
        if (index >= 0) {
            memoryCache[index] = memoryCache[index].copy(slots = slots)
            saveToPrefs()
        }
    }

    fun renamePreset(presetId: String, newName: String) {
        val index = memoryCache.indexOfFirst { it.id == presetId }
        if (index >= 0) {
            memoryCache[index] = memoryCache[index].copy(name = newName)
            saveToPrefs()
        }
    }

    fun deletePreset(presetId: String) {
        memoryCache.removeIf { it.id == presetId }
        saveToPrefs()
    }
}
