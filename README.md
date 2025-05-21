# RushHour Solver

## Deskripsi Singkat

**RushHour Solver** adalah aplikasi (program) Java yang menyelesaikan puzzle *Rush Hour* menggunakan tiga algoritma pencarian:

* **Uniform-Cost Search (UCS)**
* **Greedy Best-First Search (GBFS)**
* **A\***

User dapat memilih salah satu algoritma dan satu dari tiga heuristik (H1–H3) yang berbeda. Program solver ini mendukung konfigurasi papan dengan pintu keluar di keempat sisi, rintangan statis `X`, serta format input `.txt` standar. Antarmuka yang dipakai adalah **javax.swing**.

---

## Requirement & Instalasi

* Java 17 atau lebih baru (rekomendasi Java 23)
* Package `java` dan `javac` yang dapat diakses pada terminal (bash/powershell)

---

## Cara Compile (dan Run)

1. Pastikan semua file `.java` tersedia di dalam folder `src`.
2. Buka terminal (bash/powershell), lalu dari root folder repository ini, masuk ke folder `test`:
```bash
cd test
```
3. Jalankan prompt berikut untuk mengkompilasi kode Java dari ```src/``` ke _bytecode_ untuk disimpan di ```bin/```:
```bash
javac ../src/*.java -d ../bin
```

4. Akan dihasilkan file `.class` untuk setiap kelas pada folder bin.
5. Lanjutkan dengan menjalankan (run) program.

---

## Cara Menjalankan Aplikasi

1. Dengan tetap berada di folder `test`, Run kelas Main untuk membuka GUI aplikasi:
```bash
java -cp ../bin Main
```
2. Di jendela yang muncul:

   * Klik **Browse…** untuk memilih file `.txt` atau masukkan nama file diikuti ekstensi `.txt` yang berada di folder `test`.
   * Pilih algoritma (`ucs`, `gbfs`, `astar`) dan _heuristics ID_ `H-ID` (1–3).
   * Tekan **Pecahkan** untuk mulai penyelesaian _puzzle_.
   * Jika solusi ditemukan, akan muncul kotak-kotak berwarna pada panel utama beserta informasi statistik (node dikunjungi, waktu penyelesaian, dan jumlah _step_ penyelesaian).
   * Gunakan tombol **Prev**/**Next** untuk menavigasi setiap langkah.
   * Klik **Save Results** untuk menyimpan solusi ke berkas teks.
   * Klik **Play/Stop** untuk memulai/menghentikan animasi setiap _step_ solusi.

---

## Contoh Format Input (`.txt`)

```
6 6
12
AAB..F
..BCDF
GPPCDFK
GH.III
GHJ...
LLJMM.
```

* Baris 1: `rows cols`
* Baris 2: (opsional) jumlah piece non-primary
* Baris selanjutnya: papan dengan `.` = kosong, `P` = primary, huruf lain = kendaraan, `X` = obstacle (halangan), `K` = pintu keluar

---

## Author

Jovandra Otniel P. S. (13523141) <br />
IF2211 Strategi Algoritma – Tugas Kecil 3 <br />
Semester 2, Tahun Akademik 2024/2025 <br />

---