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
            title = "Five Forest Apples",
            domain = "Numbers & Counting",
            bundleId = "BUNDLE-NUM-CORE",
            ageTarget = "Ages 3–5",
            storyContext = "Kiko found a basket of crisp red apples beneath the old oak tree.",
            visualSceneEmojis = "🍎  🍎  🍎  🍎  🍎",
            openingQuestion = "Look at Kiko's basket under the oak tree! Can you count aloud how many red apples are sitting on the moss?",
            expectedKeywords = listOf("5", "five", "lima"),
            stepDown1Question = "Let's count them together! What number comes after one, two, three?",
            stepDown2Question = "You found four! Now look right next to the fourth apple—if we add one more apple to four, how many apples do we have altogether?",
            stepDown3Scaffold = "Let's touch each apple with our voice: One, Two, Three, Four... and the last shiny one makes Five! Can you say 'Five apples' with me?",
            celebrationAffirmation = "Hoot-hurray! You counted all five apples all by yourself! Five is one more than four!",
            unlockedStoryClue = "Golden Acorn: The oak tree hides 5 singing crickets.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-5-APPLES",
                    emojiVisual = "🍎🍎🍎🍎🍎",
                    spokenLabel = "Five Apples",
                    countBadge = 5,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-3-APPLES",
                    emojiVisual = "🍎🍎🍎",
                    spokenLabel = "Three Apples",
                    countBadge = 3,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Five apples!", "Three", "Four", "I need a hint")
        ),
        SocraticTask(
            id = "TASK-LIT-01",
            title = "Syllable Clapping: BO-LA",
            domain = "Sounds & Syllables",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Ages 4–6",
            storyContext = "Kiko is hopping across smooth river stones that make musical word sounds.",
            visualSceneEmojis = "⚽   🪨   🦋",
            openingQuestion = "Listen closely to the word 'BO-LA' (ball). When we clap 'BO' and 'LA', what sound does the very first clap make?",
            expectedKeywords = listOf("bo", "bola", "ball", "b"),
            stepDown1Question = "Let's slow our beaks down! Watch my wings clap twice: 'BO' ... 'LA'. Which sound came out first before 'LA'?",
            stepDown2Question = "Does 'BO-LA' start with a round 'BO' sound like a bouncing ball, or a 'MA' sound?",
            stepDown3Scaffold = "It starts with 'BO'! When we put 'BO' and 'LA' together, we get BO-LA! Can you say 'BO' aloud?",
            celebrationAffirmation = "Wonderful ears! You heard the first syllable 'BO' in BO-LA!",
            unlockedStoryClue = "Golden Feather: The river echoes every two-syllable word.",
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
            quickVoiceChoices = listOf("BO!", "MA", "LA", "Clap with me")
        ),
        SocraticTask(
            id = "TASK-NUM-02",
            title = "Sharing Glowing Fireflies",
            domain = "Story Addition",
            bundleId = "BUNDLE-NUM-CORE",
            ageTarget = "Ages 4–6",
            storyContext = "Two glowing fireflies are lighting up Kiko's lantern, and two more friends fly in.",
            visualSceneEmojis = "✨🐝 ✨🐝   ➕   ✨🐝 ✨🐝",
            openingQuestion = "Two little fireflies are resting on Kiko's lantern. Two more glowing fireflies zoom over to join them! How many fireflies are glowing now?",
            expectedKeywords = listOf("4", "four", "empat"),
            stepDown1Question = "Let's picture the first two fireflies: one, two. Now if one friend lands first, what comes after two?",
            stepDown2Question = "That's three! And when the second friend lands right beside them, what number comes right after three?",
            stepDown3Scaffold = "One, two... three, four! Two fireflies plus two fireflies make four bright fireflies! Try saying 'Four'!",
            celebrationAffirmation = "Brilliant thinking! Two and two come together to make four!",
            unlockedStoryClue = "Lantern Spark: Four fireflies light the path to the Treehouse.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-4-FIREFLIES",
                    emojiVisual = "✨🐝✨🐝✨🐝✨🐝",
                    spokenLabel = "Four Fireflies",
                    countBadge = 4,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-2-FIREFLIES",
                    emojiVisual = "✨🐝✨🐝",
                    spokenLabel = "Two Fireflies",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Four fireflies!", "Two", "Three", "Help me count")
        ),
        SocraticTask(
            id = "TASK-LIT-02",
            title = "Rhythm Claps: KU-DA",
            domain = "Sounds & Syllables",
            bundleId = "BUNDLE-LIT-SYLLABLES",
            ageTarget = "Ages 3–6",
            storyContext = "In the morning meadow, Kiko hears a friendly horse (Kuda) trotting by.",
            visualSceneEmojis = "🐎 (Ku - da)   👏 👏",
            openingQuestion = "Listen to our meadow friend: 'KU-DA'. How many claps do you hear when we say 'KU - DA'?",
            expectedKeywords = listOf("2", "two", "dua"),
            stepDown1Question = "Let's clap our hands once for each piece of the word: Clap one is 'KU'... Clap two is 'DA'. How many claps was that?",
            stepDown2Question = "Was 'KU-DA' just one short clap, or one-two claps?",
            stepDown3Scaffold = "It's two claps! One for 'KU', and two for 'DA'! Let's say 'Two claps' together!",
            celebrationAffirmation = "Spot on! KU-DA has two clear syllable claps!",
            unlockedStoryClue = "Meadow Bell: Words are built from musical sound blocks.",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-2-CLAPS",
                    emojiVisual = "👏 👏",
                    spokenLabel = "Two Claps",
                    countBadge = 2,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-1-CLAP",
                    emojiVisual = "👏",
                    spokenLabel = "One Clap",
                    countBadge = 1,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Two claps!", "One clap", "Three claps")
        ),
        SocraticTask(
            id = "TASK-RBS-01",
            title = "Squirrel Sammy's Pinecones",
            domain = "Comparing Quantities",
            bundleId = "BUNDLE-RBS-SPIRAL",
            ageTarget = "Ages 4–6",
            storyContext = "Squirrel Sammy gathered two piles of pinecones for his winter cozy nest.",
            visualSceneEmojis = "🌰🌰🌰🌰🌰🌰 (6)   vs   🌰🌰 (2)",
            openingQuestion = "Squirrel Sammy has two piles of pinecones! One pile has six pinecones, and the other pile has two. Which pile has MORE pinecones?",
            expectedKeywords = listOf("6", "six", "enam", "left", "first", "more", "bigger"),
            stepDown1Question = "Imagine holding two pinecones in one hand, and six pinecones in the other. Which hand feels fuller and heavier?",
            stepDown2Question = "When we count up—one, two, three, four, five, six—does six come after two because it is a bigger group?",
            stepDown3Scaffold = "Six is a much bigger group than two! So the pile with six pinecones has MORE! Say 'Six has more'!",
            celebrationAffirmation = "You remembered! Six is a bigger group than two!",
            unlockedStoryClue = "Forest Crown: You helped Sammy pick the biggest pinecone pile!",
            fallbackOptions = listOf(
                VisualCardOption(
                    id = "OPT-6-PINECONES",
                    emojiVisual = "🌰🌰🌰🌰🌰🌰",
                    spokenLabel = "Six Pinecones",
                    countBadge = 6,
                    isCorrect = true
                ),
                VisualCardOption(
                    id = "OPT-2-PINECONES",
                    emojiVisual = "🌰🌰",
                    spokenLabel = "Two Pinecones",
                    countBadge = 2,
                    isCorrect = false
                )
            ),
            quickVoiceChoices = listOf("Six has more!", "Two pinecones", "Not sure")
        )
    )
}
