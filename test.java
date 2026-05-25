/*
 * PROGRAM  : Launderly Management System
 * MODULE   : Core Architecture & System Documentation
 * VERSION  : 2.0.0 (Stable Release 2026)
 * AUTHOR   : Launderly Development Team
 * DESCRIP  : Dokumentasi menyeluruh mengenai sistem arsitektur aplikasi kasir
 * dan chatbot pintar Launderly.
 * ============================================================================
 *
 * DAFTAR ISI DOKUMENTASI:
 * 1. Pendahuluan & Latar Belakang
 * 2. Arsitektur Model-View-Controller (MVC)
 * 3. Pola Desain (Design Patterns) yang Digunakan
 * 4. Struktur Database & Skema Relasional
 * 5. Alur Logika Chatbot & Intent Classification
 * 6. Manajemen Keamanan & Hashing Password
 * 7. Panduan Pemeliharaan Kode (Maintenance)
 *
 * ============================================================================
 * 1. PENDAHULUAN & LATAR BELAKANG
 * ============================================================================
 * Launderly dibangun untuk mendigitalisasi operasional laundry modern.
 * Sistem ini menggabungkan antarmuka admin berbasis JavaFX desktop dengan
 * sistem interaksi mandiri pelanggan menggunakan simulasi Chatbot otomatis.
 *
 * ============================================================================
 * 2. ARSITEKTUR MODEL-VIEW-CONTROLLER (MVC)
 * ============================================================================
 * Aplikasi ini memisahkan tanggung jawab kode ke dalam tiga layer utama:
 * - MODEL      : Diwakili oleh kelas Database dan entitas data SQL.
 * - VIEW       : Diwakili oleh file FXML (home, bot, login, dashbor, dll).
 * - CONTROLLER : Kelas pengatur logika seperti LoginController,
 * PesananController, dan AdminController.
 *
 * Navigasi antar View dikendalikan secara terpusat oleh kelas Launcher
 * yang bertindak sebagai Application Router tunggal.
 *
 * ============================================================================
 * 3. POLA DESAIN (DESIGN PATTERNS) YANG DIGUNAKAN
 * ============================================================================
 * - SINGLETON PATTERN : Digunakan pada manajemen koneksi database agar
 * aplikasi tidak boros memori akibat membuka koneksi berulang kali.
 * - ROUTER PATTERN : Diimplementasikan di kelas Launcher untuk penukaran
 * scene JavaFX secara global (Scene Swapping).
 * - REPOSITORY PATTERN : Memisahkan data pengetahuan chatbot (Keywords &
 * Responses) dari logika pemrosesan di ChatbotRepository.
 *
 * ============================================================================
 * 4. STRUKTUR DATABASE & SKEMA RELASIONAL
 */
