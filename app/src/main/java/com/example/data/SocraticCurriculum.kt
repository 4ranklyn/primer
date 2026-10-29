package com.example.data

data class VisualCardOption(
    val id: String,
    val emojiVisual: String,
    val spokenLabel: String,
    val countBadge: Int? = null,
    val isCorrect: Boolean
)

data class SocraticTask(
    val id: String,
    val title: String,
    val domain: String,
    val bundleId: String,
    val ageTarget: String,
    val storyContext: String,
    val visualSceneEmojis: String,
    val openingQuestion: String,
    val expectedKeywords: List<String>,
    val stepDown1Question: String,
    val stepDown2Question: String,
    val stepDown3Scaffold: String,
    val celebrationAffirmation: String,
    val unlockedStoryClue: String,
    val fallbackOptions: List<VisualCardOption>,
    val quickVoiceChoices: List<String>
)

object SocraticCurriculumBank {

    val tasks: List<SocraticTask> = listOf(
        SocraticTask(
            id = "TASK-NUM-01",
            title = "Lima Apel di Hutan",
            domain = "Berhitung & Angka",
            bundleId = "BUNDLE-NUM-CORE",
            ageTarget = "Usia 3–5",
            storyContext = "Kiko menemukan sekeranjang apel merah segar di bawah pohon rindang.",
            visualSceneEmojis = "🍎  🍎  🍎  🍎  🍎",
            openingQuestion = "Lihat keranjang Kiko di bawah pohon rindang! Ayo hitung dengan suara lantang, ada berapa buah apel merah di atas lumut?",
            expectedKeywords = listOf("5", "lima", "five"),
            stepDown1Question = "Ayo kita hitung bersama! Angka berapa yang datang setelah satu, dua, tiga? Let's count them together! What number comes after one, two, three?",
            stepDown2Question = "Hebat, kamu menemukan empat! Sekarang lihat tepat di sebelah apel keempat—kalau kita tambah satu apel lagi ke empat, ada berapa apel semuanya?",
            stepDown3Scaffold = "Ayo kita sentuh setiap apel dengan suara kita: Satu, Dua, Tiga, Empat... dan yang terakhir membuat semuanya jadi Lima! Bisa sebutkan 'Lima apel' bersama Kiko?",
            celebrationAffirmation = "Hore-hore! Kamu berhasil menghitung semua lima apel sendiri! Lima itu satu lebih banyak dari empat!",
            unlockedStoryClue = "Biji Kenari Emas: Pohon rindang menyembunyikan 5 jangkrik bernyanyi.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-5-APPLES",
                    emojiVisual = "🍎🍎🍎🍎🍎",
                    spokenLabel = "5 Apel",
                    countBadge = 5,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-3-APPLES",
                    emojiVisual = "🍎🍎🍎",
                    spokenLabel = "3 Apel",
                    countBadge = 3,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Lima apel!", "Tiga", "Empat", "Beri petunjuk")
        ),
        SocraticTask(
            id = "TASK-LIT-01",
            title = "Tepuk Suku Kata: BO-LA",
            domain = "Bunyi & Suku Kata",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Usia 4–6",
            storyContext = "Kiko melompat di atas batu sungai halus yang menghasilkan bunyi kata bermusik.",
            visualSceneEmojis = "⚽   🪨   🦋",
            openingQuestion = "Dengarkan baik-baik kata 'BO-LA'. Saat kita tepuk tangan untuk 'BO' dan 'LA', bunyi apa yang pertama kali terdengar?",
            expectedKeywords = listOf("bo", "bola", "ball", "b"),
            stepDown1Question = "Ayo kita perlambat paruh kita! Lihat sayap Kiko menepuk dua kali: 'BO' ... 'LA'. Bunyi apa yang keluar lebih dulu sebelum 'LA'?",
            stepDown2Question = "Apakah 'BO-LA' dimulai dengan bunyi bulat 'BO' seperti bola memantul, atau bunyi 'MA'?",
            stepDown3Scaffold = "Dimulai dengan 'BO'! Kalau kita gabungkan 'BO' dan 'LA', jadilah BO-LA! Coba katakan 'BO' dengan suara lantang!",
            celebrationAffirmation = "Telingamu hebat sekali! Kamu mendengar suku kata pertama 'BO' di dalam BO-LA!",
            unlockedStoryClue = "Bulu Emas: Sungai menggemakan setiap kata dua suku kata.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-BOLA",
                    emojiVisual = "⚽ 🗣️",
                    spokenLabel = "BO - LA",
                    countBadge = 2,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-MATA",
                    emojiVisual = "👀 🗣️",
                    spokenLabel = "MA - TA",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("BO!", "MA", "LA", "Tepuk bersamaku")
        ),
        SocraticTask(
            id = "TASK-NUM-02",
            title = "Kunang-Kunang Berkelip",
            domain = "Cerita Penjumlahan",
            bundleId = "BUNDLE-NUM-CORE",
            ageTarget = "Usia 4–6",
            storyContext = "Dua kunang-kunang bercahaya menerangi lentera Kiko, dan dua teman lainnya datang terbang.",
            visualSceneEmojis = "✨🐝 ✨🐝   ➕   ✨🐝 ✨🐝",
            openingQuestion = "Dua kunang-kunang kecil sedang hinggap di lentera Kiko. Dua kunang-kunang lagi terbang mendekat untuk bergabung! Berapa kunang-kunang yang berkelip sekarang?",
            expectedKeywords = listOf("4", "empat", "four"),
            stepDown1Question = "Ayo bayangkan dua kunang-kunang pertama: satu, dua. Jika satu teman hinggap lebih dulu, angka berapa setelah dua?",
            stepDown2Question = "Tepat, tiga! Dan saat teman kedua hinggap di sebelahnya, angka berapa tepat setelah tiga?",
            stepDown3Scaffold = "Satu, dua... tiga, empat! Dua kunang-kunang ditambah dua kunang-kunang jadi empat kunang-kunang terang! Coba sebutkan 'Empat'!",
            celebrationAffirmation = "Pikiran yang cemerlang! Dua dan dua bergabung menjadi empat!",
            unlockedStoryClue = "Percikan Lentera: Empat kunang-kunang menerangi jalan ke Rumah Pohon.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-4-FIREFLIES",
                    emojiVisual = "✨🐝✨🐝✨🐝✨🐝",
                    spokenLabel = "4 Kunang-Kunang",
                    countBadge = 4,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-2-FIREFLIES",
                    emojiVisual = "✨🐝✨🐝",
                    spokenLabel = "2 Kunang-Kunang",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Empat kunang-kunang!", "Dua", "Tiga", "Bantu hitung")
        ),
        SocraticTask(
            id = "TASK-LIT-02",
            title = "Irama Tepukan: KU-DA",
            domain = "Bunyi & Suku Kata",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Usia 3–6",
            storyContext = "Di padang rumput pagi, Kiko mendengar seekor kuda ramah berlari santai.",
            visualSceneEmojis = "🐎 (Ku - da)   👏 👏",
            openingQuestion = "Dengarkan teman padang rumput kita: 'KU-DA'. Berapa tepukan yang kamu dengar saat kita ucapkan 'KU - DA'?",
            expectedKeywords = listOf("2", "dua", "two"),
            stepDown1Question = "Ayo tepuk tangan sekali untuk setiap bagian kata: Tepukan satu adalah 'KU'... Tepukan dua adalah 'DA'. Berapa tepukan itu?",
            stepDown2Question = "Apakah 'KU-DA' hanya satu tepukan pendek, atau satu-dua tepukan?",
            stepDown3Scaffold = "Ada dua tepukan! Satu untuk 'KU', dan dua untuk 'DA'! Ayo katakan 'Dua tepukan' bersama-sama!",
            celebrationAffirmation = "Tepat sekali! KU-DA punya dua ketukan suku kata yang jelas!",
            unlockedStoryClue = "Lonceng Padang Rumput: Kata-kata tersusun dari balok bunyi musik.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-2-CLAPS",
                    emojiVisual = "👏 👏",
                    spokenLabel = "2 Tepukan",
                    countBadge = 2,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-1-CLAP",
                    emojiVisual = "👏",
                    spokenLabel = "1 Tepukan",
                    countBadge = 1,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Dua tepukan!", "Satu tepukan", "Tiga tepukan")
        ),
        SocraticTask(
            id = "TASK-RBS-01",
            title = "Buah Pinus Tupai Sammy",
            domain = "Membandingkan Jumlah",
            bundleId = "BUNDLE-RBS-SPIRAL",
            ageTarget = "Usia 4–6",
            storyContext = "Tupai Sammy mengumpulkan dua tumpukan buah pinus untuk sarang hangatnya.",
            visualSceneEmojis = "🌰🌰🌰🌰🌰🌰 (6)   vs   🌰🌰 (2)",
            openingQuestion = "Tupai Sammy punya dua tumpukan buah pinus! Satu tumpukan ada enam pinus, dan tumpukan lain ada dua. Tumpukan mana yang LEBIH BANYAK pinusnya?",
            expectedKeywords = listOf("6", "enam", "six", "kiri", "pertama", "banyak", "lebih banyak"),
            stepDown1Question = "Bayangkan memegang dua pinus di satu tangan, dan enam pinus di tangan satunya. Tangan mana yang terasa lebih penuh dan berat?",
            stepDown2Question = "Saat kita berhitung maju—satu, dua, tiga, empat, lima, enam—apakah enam datang setelah dua karena jumlahnya lebih besar?",
            stepDown3Scaffold = "Enam adalah kelompok yang jauh lebih besar dari dua! Jadi tumpukan dengan enam buah pinus punya LEBIH BANYAK! Sebutkan 'Enam lebih banyak'!",
            celebrationAffirmation = "Kamu mengingatnya! Enam adalah kelompok yang lebih banyak dari dua!",
            unlockedStoryClue = "Mahkota Hutan: Kamu membantu Sammy memilih tumpukan buah pinus terbesar!",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-6-PINECONES",
                    emojiVisual = "🌰🌰🌰🌰🌰🌰",
                    spokenLabel = "6 Buah Pinus",
                    countBadge = 6,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-2-PINECONES",
                    emojiVisual = "🌰🌰",
                    spokenLabel = "2 Buah Pinus",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Enam lebih banyak!", "Dua pinus", "Belum yakin")
        ),
        SocraticTask(
            id = "TASK-LIT-03",
            title = "Bunyi Awal: BA-JU",
            domain = "Bunyi & Suku Kata",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Usia 4–6",
            storyContext = "Kiko sedang mencoba rompi musim dingin burung hantu kecil di rumah pohon.",
            visualSceneEmojis = "👕 (Ba - ju)   🦉 ✨",
            openingQuestion = "Dengarkan kata 'BA-JU'. Bunyi suku kata apa yang paling pertama kamu dengar di awal kata 'BA-JU'?",
            expectedKeywords = listOf("ba", "baju", "b"),
            stepDown1Question = "Ayo kita ucapkan perlahan: 'BA' ... 'JU'. Bunyi apa yang pertama kali dibuat mulutmu?",
            stepDown2Question = "Apakah 'BA-JU' dimulai dengan 'BA' seperti bayi burung, atau 'MA'?",
            stepDown3Scaffold = "Dimulai dengan 'BA'! 'BA' dan 'JU' digabung menjadi BA-JU! Ucapkan 'BA' bersama Kiko!",
            celebrationAffirmation = "Luar biasa! Kamu mendengar bunyi awal 'BA' di dalam BA-JU!",
            unlockedStoryClue = "Rompi Mungil: Bulu hangat membuat Kiko tetap nyaman.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-BAJU",
                    emojiVisual = "👕 🗣️",
                    spokenLabel = "BA - JU",
                    countBadge = 2,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-TOPI",
                    emojiVisual = "🧢 🗣️",
                    spokenLabel = "TO - PI",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("BA!", "MA", "JU")
        ),
        SocraticTask(
            id = "TASK-LIT-04",
            title = "Melengkapi Kata: BU-KU",
            domain = "Bunyi & Suku Kata",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Usia 4–6",
            storyContext = "Kiko sedang membuka buku cerita hutan kuno.",
            visualSceneEmojis = "📖 (Bu - ku)   🌿 🦉",
            openingQuestion = "Kiko suka sekali membaca buku! Kalau kita mulai dengan bunyi 'BU'... suku kata apa yang kita tambahkan untuk melengkapi kata 'BU-KU'?",
            expectedKeywords = listOf("ku", "buku", "k"),
            stepDown1Question = "Dengarkan kata lengkapnya: 'BU' ... 'KU'. Bunyi apa yang menyelesaikan kata setelah 'BU'?",
            stepDown2Question = "Apakah bunyi penutupnya 'KU' atau 'DA'?",
            stepDown3Scaffold = "Bunyinya adalah 'KU'! Satukan 'BU' dengan 'KU' dan jadilah BU-KU! Ucapkan 'KU' bersamaku!",
            celebrationAffirmation = "Membaca yang hebat! 'KU' melengkapi buku cerita favorit kita!",
            unlockedStoryClue = "Kunci Cerita: Buku membuka gerbang hutan ajaib.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-BUKU",
                    emojiVisual = "📖 🗣️",
                    spokenLabel = "BU - KU",
                    countBadge = 2,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-KAYU",
                    emojiVisual = "🪵 🗣️",
                    spokenLabel = "KA - YU",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("KU!", "DA", "BU")
        ),
        SocraticTask(
            id = "TASK-LIT-05",
            title = "Penjelajah Rima: MA-TA & BA-TA",
            domain = "Rima & Fonem",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Usia 4–6",
            storyContext = "Kiko menemukan batu loncatan berima di sepanjang aliran sungai.",
            visualSceneEmojis = "👀 (Ma-ta)   🧱 (Ba-ta)   🎶",
            openingQuestion = "Dengarkan dua kata bersahabat ini: 'MA-TA' dan 'BA-TA'. Apakah keduanya berakhir dengan bunyi musik yang sama?",
            expectedKeywords = listOf("ya", "yes", "sama", "same", "rima", "ta"),
            stepDown1Question = "Dengarkan bunyi akhirnya: 'Ma-TA'... 'Ba-TA'. Apakah keduanya sama-sama berakhir dengan 'TA'?",
            stepDown2Question = "Apakah 'MA-TA' dan 'BA-TA' terdengar seperti pasangan rima yang serasi, ya atau tidak?",
            stepDown3Scaffold = "Ya, keduanya berima! Kedua kata berakhiran bunyi 'TA'! Katakan 'Ya, berima!'",
            celebrationAffirmation = "Telinga luar biasa! MA-TA dan BA-TA berima karena keduanya berakhir dengan TA!",
            unlockedStoryClue = "Batu Rima: Kata-kata yang berima menciptakan lagu hutan.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-RHYME-YES",
                    emojiVisual = "🎶 ✅",
                    spokenLabel = "Ya, Berima!",
                    countBadge = null,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-RHYME-NO",
                    emojiVisual = "❌",
                    spokenLabel = "Tidak",
                    countBadge = null,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Ya, berima!", "Tidak", "TA!")
        ),
        SocraticTask(
            id = "TASK-NUM-04",
            title = "Batu Loncatan Sungai: Menghitung ke 7",
            domain = "Berhitung & Angka",
            bundleId = "BUNDLE-NUM-CORE",
            ageTarget = "Usia 4–6",
            storyContext = "Kiko sedang menghitung tujuh batu datar melintasi aliran sungai kecil.",
            visualSceneEmojis = "🪨🪨🪨🪨🪨🪨🪨 (7 Batu)",
            openingQuestion = "Kiko melompat di atas batu: 1, 2, 3, 4, 5, 6... angka berapa yang tepat setelah enam?",
            expectedKeywords = listOf("7", "tujuh", "seven"),
            stepDown1Question = "Hitung bersama lompatan Kiko: empat, lima, enam... langkah berikutnya berapa?",
            stepDown2Question = "Apakah tujuh datang tepat setelah enam, atau tiga?",
            stepDown3Scaffold = "Enam... dan lalu Tujuh! Tujuh itu satu lebih banyak dari enam! Bisa sebutkan 'Tujuh'?",
            celebrationAffirmation = "Hore-hore! Tujuh batu berhasil dilewati dengan aman!",
            unlockedStoryClue = "Teratai Sungai: Tujuh langkah membawamu menyeberangi air.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-7-STONES",
                    emojiVisual = "🪨🪨🪨🪨🪨🪨🪨",
                    spokenLabel = "7 Batu",
                    countBadge = 7,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-5-STONES",
                    emojiVisual = "🪨🪨🪨🪨🪨",
                    spokenLabel = "5 Batu",
                    countBadge = 5,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Tujuh!", "Enam", "Delapan")
        ),
        SocraticTask(
            id = "TASK-NUM-05",
            title = "Piknik Buah Beri Hutan: 3 + 1",
            domain = "Cerita Penjumlahan",
            bundleId = "BUNDLE-NUM-CORE",
            ageTarget = "Usia 4–6",
            storyContext = "Kiko punya 3 buah beri ungu manis dan menemukan 1 lagi di semak cerah.",
            visualSceneEmojis = "🫐🫐🫐   ➕   🫐",
            openingQuestion = "Kiko punya tiga buah beri manis di mangkuk. Kiko memetik satu beri lagi dari pohon! Ada berapa buah beri semuanya sekarang?",
            expectedKeywords = listOf("4", "empat", "four"),
            stepDown1Question = "Kita punya tiga buah beri: satu, dua, tiga. Kalau ditambah satu lagi, angka berapa setelah tiga?",
            stepDown2Question = "Apakah tiga ditambah satu sama dengan empat buah beri, atau dua?",
            stepDown3Scaffold = "Tiga... dan satu lagi menjadikannya Empat! Tiga tambah satu itu empat! Ucapkan 'Empat buah beri'!",
            celebrationAffirmation = "Matematika yang lezat! Tiga dan satu bersama-sama menjadi empat!",
            unlockedStoryClue = "Keranjang Beri: Empat buah beri membuat piknik yang menyenangkan!",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-4-BERRIES",
                    emojiVisual = "🫐🫐🫐🫐",
                    spokenLabel = "4 Buah Beri",
                    countBadge = 4,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-3-BERRIES",
                    emojiVisual = "🫐🫐🫐",
                    spokenLabel = "3 Buah Beri",
                    countBadge = 3,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Empat!", "Tiga", "Lima")
        )
    )
}
