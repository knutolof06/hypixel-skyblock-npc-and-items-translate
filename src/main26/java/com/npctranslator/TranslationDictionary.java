package com.npctranslator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationDictionary {
    private static final File DICT_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "npc_translator_dict.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // Original Text -> Translated Text
    private static ConcurrentHashMap<String, String> dictionary = new ConcurrentHashMap<>();

    public static void load() {
        if (DICT_FILE.exists()) {
            try (FileReader reader = new FileReader(DICT_FILE)) {
                Type type = new TypeToken<ConcurrentHashMap<String, String>>() {}.getType();
                ConcurrentHashMap<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    dictionary = loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void save() {
        try (FileWriter writer = new FileWriter(DICT_FILE)) {
            GSON.toJson(dictionary, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String original, String provider) {
        return dictionary.get(provider + ":" + original);
    }

    public static void put(String original, String translated, String provider) {
        if (original == null || translated == null || original.trim().isEmpty() || translated.trim().isEmpty()) return;
        dictionary.put(provider + ":" + original, translated);
    }

    public static boolean has(String original, String provider) {
        return dictionary.containsKey(provider + ":" + original);
    }

    public static void clear() {
        dictionary.clear();
        if (DICT_FILE.exists()) {
            DICT_FILE.delete();
        }
    }

    public static int size() {
        return dictionary.size();
    }
}
