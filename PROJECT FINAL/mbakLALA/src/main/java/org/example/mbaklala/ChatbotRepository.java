package org.example.mbaklala;

import java.util.*;

public class ChatbotRepository {

    public static List<PatternData> loadPatterns() {

        List<PatternData> list = new ArrayList<>();

        list.add(new PatternData("harga", "harga"));
        list.add(new PatternData("status", "status"));
        list.add(new PatternData("lokasi", "lokasi"));

        list.add(new PatternData("cuci", "layanan"));
        list.add(new PatternData("laundry", "layanan"));

// 🔥 tambah layanan
        list.add(new PatternData("boneka", "layanan"));
        list.add(new PatternData("pakaian", "layanan"));
        list.add(new PatternData("spray", "layanan"));
        list.add(new PatternData("sepatu", "layanan"));
        list.add(new PatternData("cuci setrika", "layanan"));
        list.add(new PatternData("selimut", "layanan"));
        list.add(new PatternData("setrika", "layanan"));

        list.add(new PatternData("cuci", "layanan"));
        list.add(new PatternData("boneka", "layanan"));
        list.add(new PatternData("pakaian", "layanan"));

        list.add(new PatternData("estimasi", "estimasi"));
        list.add(new PatternData("lama", "estimasi"));

        list.add(new PatternData("pesan", "opening"));

        return list;
    }

    public static Map<String, List<String>> loadResponses() {

        Map<String, List<String>> map = new HashMap<>();

        map.put("opening", List.of(
                "Halo! Mau buat pesanan atau cek layanan? 😊"
        ));

        map.put("harga", List.of(
                "Ini daftar harga layanan kami 👇"
        ));

        map.put("layanan", List.of(
                "Kami melayani berbagai jenis laundry seperti pakaian, boneka, sepatu, selimut, boneka, dan lainnya 😊",
                "Layanan kami meliputi cuci pakaian, boneka, setrika, sepatu, selimut, dan paket cuci setrika 👍",
                "Bisa banget! Kami menerima hampir semua jenis cucian termasuk boneka dan sepatu ✨"
        ));

        map.put("status", List.of(
                "Masukkan ID pesanan ya (contoh: #2601)"
        ));

        map.put("estimasi", List.of(
                "Berikut estimasi layanan kami ⏱"
        ));

        map.put("layanan", List.of(
                "Kami bisa mencuci pakaian, boneka, dan lainnya 😊"
        ));

        map.put("fallback", List.of(
                "Maaf, saya belum mengerti 😢"
        ));

        return map;
    }
}