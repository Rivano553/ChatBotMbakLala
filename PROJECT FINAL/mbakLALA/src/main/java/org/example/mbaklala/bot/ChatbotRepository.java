package org.example.mbaklala.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotRepository {
    public static Map<String, List<String>> getKeywords() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("opening", List.of("halo", "hai", "pagi", "siang", "sore", "malam", "ping", "p"));
        map.put("harga", List.of("harga", "biaya", "tarif", "layanan", "daftar", "list", "menu", "apa saja"));
        map.put("status", List.of("status", "cek", "lacak"));
        map.put("lupa", List.of("lupa", "id", "nomor id", "id saya"));
        map.put("jam", List.of("jam", "buka", "tutup", "operasional"));
        map.put("paketan", List.of("paketan", "sekaligus", "paket", "campur", "gabung"));
        return map;
    }

    public static Map<String, List<String>> getResponses() {
        Map<String, List<String>> map = new HashMap<>();

        // VARIASI OPENING (SAPAAN)
        map.put("opening", List.of(
                "Halo! Ada yang bisa Launderly bantu?\nKetik: harga, status, atau klik tombol tanya cepat di bawah.",
                "Hai kak! Selamat datang di Launderly.\nMau cek status cucian atau lihat daftar harga? Bisa ketik atau klik tombol di bawah.",
                "Halo! Spesialis cucian kotor siap membantu!\nKetik 'harga', 'status', atau pilih menu tanya cepat ya.",
                "Hai! Launderly di sini. Ada yang bisa kami bantu hari ini?\nBisa cek harga, jam operasional, atau status cucianmu."
        ));

        // VARIASI STATUS
        map.put("status", List.of(
                "Untuk cek status, masukkan ID Cucian kamu (Contoh: LND-260510-1234) atau ketik Nama Lengkap kamu.",
                "Mau tahu cucianmu sudah sampai mana? Ketik aja Nama Lengkap atau ID Pesanan kamu di sini.",
                "Siap melacak cucian! Silakan ketik Nama Lengkap atau ID Pesanan kamu ya.",
                "Cek status itu mudah! Cukup balas pesan ini dengan mengetik Nama Lengkap atau ID pesananmu (Contoh: LND-XXXX)."
        ));

        // VARIASI LUPA ID
        map.put("lupa", List.of(
                "Jangan khawatir! Jika lupa ID, silakan ketik Nama Lengkap yang kamu isi saat Order Laundry tadi untuk mengecek status.",
                "Lupa ID pesanan? Tenang saja kak, cukup ketik Nama Lengkap kamu dan sistem kami akan melacaknya.",
                "Nggak perlu panik kalau lupa ID! Masukkan saja Nama Lengkapmu di sini untuk melihat status cucian.",
                "ID-nya hilang? Ketikkan saja Nama Lengkap yang digunakan saat memesan ya, nanti Launderly bantu carikan."
        ));

        // VARIASI HARGA/LAYANAN
        map.put("harga", List.of(
                "Tentu, berikut adalah daftar layanan Launderly beserta harganya:",
                "Ini dia daftar layanan beserta harganya yang ada di Launderly:",
                "Boleh! Silakan cek daftar harga lengkap kami di bawah ini ya:",
                "Siap! Berikut rincian harga untuk setiap layanan kami:"
        ));

        // VARIASI JAM OPERASIONAL
        map.put("jam", List.of(
                "Launderly buka setiap hari mulai pukul 08:00 WIB hingga 20:00 WIB. Pintu kami selalu terbuka untuk cucian kotor Anda!",
                "Kami beroperasi dari jam 08:00 pagi sampai jam 20:00 malam ya kak. Buka setiap hari!",
                "Jam operasional Launderly: Senin - Minggu (08:00 WIB - 20:00 WIB). Jangan sampai kelewatan ya!",
                "Halo! Kami siap mencuci pakaianmu setiap hari dari pukul 08:00 hingga 20:00 WIB."
        ));

        // VARIASI PAKETAN (BARU DITAMBAHKAN)
        map.put("paketan", List.of(
                "Bisa banget kak! Launderly melayani pencucian berbagai macam barang sekaligus. Kamu bisa cek daftar harga lengkap dengan mengetik kata 'harga'.",
                "Di Launderly, kamu bebas mencuci berbagai jenis pakaian sekaligus lho! Kami membaginya menjadi Kiloan dan Satuan. Mau tahu harga untuk barang tertentu?",
                "Kamu bebas mencampur cucian Kiloan dan Satuan dalam satu pesanan. Sebutkan saja barang apa yang mau dicuci untuk cek harganya!"
        ));

        // VARIASI FALLBACK (GAGAL PAHAM)
        map.put("fallback", List.of(
                "Maaf, Launderly kurang paham. Coba ketik pertanyaan lain atau masukkan Nama/ID untuk cek status.",
                "Hmm, Launderly belum mengerti maksudmu.\nMau cek status? Cukup ketik Nama atau ID pesananmu ya.",
                "Duh, Launderly agak bingung nih. Bisa pakai kata kunci lain?\nAtau langsung ketik Nama/ID untuk melacak cucianmu.",
                "Maaf kak, Launderly masih belajar jadi belum paham kalimat barusan\nKetik 'harga' untuk layanan, atau ketik Namamu untuk cek status."
        ));

        return map;
    }
}