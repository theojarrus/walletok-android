package com.theost.walletok.data.repositories

import com.theost.walletok.R
import com.theost.walletok.data.api.WalletOkService
import com.theost.walletok.data.dto.mapToCategory
import com.theost.walletok.data.models.TransactionCategory
import com.theost.walletok.data.models.TransactionCategoryType
import io.reactivex.Completable
import io.reactivex.Single

object CategoriesRepository {
    private val service = WalletOkService.getInstance()
    private val categories = mutableListOf(
        TransactionCategory(
            id = 0,
            image = R.drawable.ic_category_card,
            name = "Зарплата",
            type = TransactionCategoryType.INCOME
        ),
        TransactionCategory(
            id = 1,
            image = R.drawable.ic_category_card,
            name = "Подработка",
            type = TransactionCategoryType.INCOME
        ),
        TransactionCategory(
            id = 2,
            image = R.drawable.ic_category_gift,
            name = "Подарок",
            type = TransactionCategoryType.INCOME
        ),
        TransactionCategory(
            id = 3,
            image = R.drawable.ic_category_percent,
            name = "Капитализация",
            type = TransactionCategoryType.INCOME
        ),
        TransactionCategory(
            id = 4,
            image = R.drawable.ic_category_food,
            name = "Кафе и рестораны",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 5,
            image = R.drawable.ic_category_supermarket,
            name = "Супермаркеты",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 6,
            image = R.drawable.ic_category_sport,
            name = "Спорт",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 7,
            image = R.drawable.ic_category_transport,
            name = "Транспорт",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 8,
            image = R.drawable.ic_category_pharmacy,
            name = "Медицина",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 9,
            image = R.drawable.ic_category_gas,
            name = "Бензин",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 10,
            image = R.drawable.ic_category_house,
            name = "Квартплата",
            type = TransactionCategoryType.EXPENSE
        ),
        TransactionCategory(
            id = 11,
            image = R.drawable.ic_category_travel,
            name = "Путешествия",
            type = TransactionCategoryType.EXPENSE
        )
    )

    fun getCategories(): Single<List<TransactionCategory>> {
        return if (categories.isNotEmpty()) Single.just(categories) else
            service.getCategories().map { list -> list.map { it.mapToCategory() } }
                .doOnSuccess {
                    categories.clear()
                    categories.addAll(it)
                }
    }

    fun addCategory(name: String, iconRes: Int, type: TransactionCategoryType): Completable {
        return Completable.fromAction {
            val category = simulateCreation(name, iconRes, type)
            categories.add(category)
        }
    }

    private fun simulateCreation(name: String, iconRes: Int, type: TransactionCategoryType): TransactionCategory {
        return TransactionCategory(
            categories.size + 1,
            iconRes,
            name,
            type
        )
    }

    fun removeCategories(categoriesIds: List<Int>): Completable {
        return Completable.fromAction {
            categoriesIds.forEach { id -> categories.removeAll { it.id == id} }
        }
    }

}