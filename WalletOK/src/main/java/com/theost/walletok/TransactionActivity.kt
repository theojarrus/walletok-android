package com.theost.walletok

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.theost.walletok.data.models.TransactionCreationModel
import com.theost.walletok.data.repositories.TransactionsRepository
import com.theost.walletok.widgets.TransactionCategoryListener
import com.theost.walletok.widgets.TransactionListener
import com.theost.walletok.widgets.TransactionTypeListener
import com.theost.walletok.widgets.TransactionValueListener

class TransactionActivity : FragmentActivity(), TransactionListener, TransactionValueListener,
    TransactionTypeListener,
    TransactionCategoryListener {

    companion object {
        private const val TRANSACTION_MODE_KEY = "transaction_edit_mode"

        fun newIntent(context: Context, mode: Int): Intent {
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra(TRANSACTION_MODE_KEY, mode)
            return intent
        }
    }

    private val transaction = TransactionCreationModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creation)

        val mode = intent.getIntExtra(TRANSACTION_MODE_KEY, R.string.new_transaction)
        if (savedInstanceState == null) {
            when (mode) {
                R.string.new_transaction -> startFragment(TransactionValueFragment.newFragment(0))
                R.string.edit_transaction -> {
                    // todo transaction edit
                }
            }
        }
    }

    override fun onValueEdit() {
        startFragment(TransactionValueFragment.newFragment(transaction.value ?: 0))
    }

    override fun onTypeEdit() {
        startFragment(TransactionTypeFragment.newFragment(transaction.type))
    }

    override fun onCategoryEdit() {
        startFragment(TransactionCategoryFragment.newFragment(transaction.categoryId))
    }

    override fun onValueSubmitted(value: Int) {
        transaction.value = value
        if (transaction.isFilled()) {
            startFragment(TransactionEditFragment.newFragment(transaction))
        } else {
            startFragment(TransactionTypeFragment.newFragment(transaction.type))
        }
    }

    override fun onTypeSubmitted(type: String) {
        transaction.type = type
        if (transaction.isFilled()) {
            startFragment(TransactionEditFragment.newFragment(transaction))
        } else {
            startFragment(TransactionCategoryFragment.newFragment(transaction.categoryId))
        }
    }

    override fun onCategorySubmitted(categoryId: Int, categoryName: String) {
        transaction.categoryId = categoryId
        transaction.categoryName = categoryName
        startFragment(TransactionEditFragment.newFragment(transaction))
    }

    override fun onTransactionSubmitted() {
        if (transaction.isFilled()) {
            TransactionsRepository.addTransaction(transaction.value!!, transaction.categoryId!!)
            setResult(RESULT_OK)
        }
        finish()
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.creation_fragment_container, fragment)
            .commit()
    }

}