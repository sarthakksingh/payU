package com.sarthak.payu.utils

import com.sarthak.payu.data.model.Category


/**
 * Rule-based keyword classifier for auto-categorizing transactions.
 * Extensible: add keywords to any category bucket below.
 */
object CategoryClassifier {

    private val rules: Map<Category, List<String>> = mapOf(
        Category.FOOD to listOf(
            "zomato", "swiggy", "food", "restaurant", "cafe", "coffee",
            "pizza", "burger", "lunch", "dinner", "breakfast", "snack",
            "grocery", "blinkit", "zepto", "dunzo", "starbucks", "dominos",
            "kfc", "mcdonald", "hotel", "dhaba", "biryani", "meal"
        ),
        Category.TRAVEL to listOf(
            "uber", "ola", "rapido", "flight", "train", "bus", "metro",
            "irctc", "makemytrip", "goibibo", "airline", "taxi", "cab",
            "petrol", "fuel", "toll", "auto", "travel", "trip", "journey",
            "indigo", "spicejet", "airasia", "booking"
        ),
        Category.SHOPPING to listOf(
            "amazon", "flipkart", "myntra", "meesho", "ajio", "nykaa",
            "shopping", "clothes", "shoes", "shirt", "dress", "purchase",
            "buy", "order", "delivery", "mall", "market", "store", "shop"
        ),
        Category.ENTERTAINMENT to listOf(
            "netflix", "prime", "hotstar", "spotify", "youtube", "movie",
            "cinema", "theatre", "concert", "game", "gaming", "steam",
            "entertainment", "subscription", "zee5", "sonyliv", "apple music"
        ),
        Category.HEALTH to listOf(
            "hospital", "doctor", "medicine", "pharmacy", "medical", "clinic",
            "health", "apollo", "1mg", "netmeds", "pharmeasy", "gym",
            "fitness", "yoga", "lab", "test", "blood", "dental", "insurance"
        ),
        Category.EDUCATION to listOf(
            "course", "udemy", "coursera", "college", "school", "tuition",
            "book", "study", "fees", "education", "class", "coaching",
            "exam", "certificate", "training", "workshop", "unacademy"
        ),
        Category.UTILITIES to listOf(
            "electricity", "water", "gas", "wifi", "internet", "broadband",
            "phone", "recharge", "bill", "utility", "airtel", "jio", "bsnl",
            "vi", "postpaid", "prepaid", "rent", "maintenance", "society"
        ),
        Category.SALARY to listOf(
            "salary", "wage", "payroll", "stipend", "ctc", "hike", "bonus",
            "increment", "pay", "credited", "employer"
        ),
        Category.FREELANCE to listOf(
            "freelance", "client", "project", "upwork", "fiverr", "contract",
            "consulting", "invoice", "payment received", "gig", "commission"
        ),
        Category.INVESTMENT to listOf(
            "invest", "stock", "mutual fund", "sip", "zerodha", "groww",
            "share", "nse", "bse", "crypto", "bitcoin", "fd", "ppf",
            "nps", "dividend", "return", "profit", "trading"
        )
    )

    /**
     * Returns the best matching category for the given note text.
     * Falls back to Category.OTHER if no match found.
     */
    fun classify(note: String): Category {
        val lower = note.lowercase().trim()
        if (lower.isBlank()) return Category.OTHER

        var bestCategory = Category.OTHER
        var bestScore = 0

        for ((category, keywords) in rules) {
            val score = keywords.count { keyword -> lower.contains(keyword) }
            if (score > bestScore) {
                bestScore = score
                bestCategory = category
            }
        }
        return bestCategory
    }

    /**
     * Returns confidence: HIGH, MEDIUM, LOW based on keyword match count
     */
    fun confidence(note: String): String {
        val lower = note.lowercase().trim()
        val maxScore = rules.values.maxOf { keywords ->
            keywords.count { lower.contains(it) }
        }
        return when {
            maxScore >= 2 -> "HIGH"
            maxScore == 1 -> "MEDIUM"
            else -> "LOW"
        }
    }
}