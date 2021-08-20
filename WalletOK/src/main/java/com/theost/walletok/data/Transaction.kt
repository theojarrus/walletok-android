package com.theost.walletok.data

data class Transaction(
    val id: Int,
    val categoryId: Int,
    val money: String,
    val currency: String,
    val dateTime: String
)

data class TransactionCategory(
    val id: Int,
    val image: Any,
    val name: String,
    val type: TransactionCategoryType
)

enum class TransactionCategoryType(val uiName: String) {
    INCOME("Пополнение"), EXPENSE("Траты")
}