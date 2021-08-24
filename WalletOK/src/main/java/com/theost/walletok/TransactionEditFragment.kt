package com.theost.walletok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import com.theost.walletok.base.BaseAdapter
import com.theost.walletok.base.ErrorMessageHelper
import com.theost.walletok.data.models.TransactionCreationModel
import com.theost.walletok.data.repositories.CategoriesRepository
import com.theost.walletok.databinding.FragmentTransactionEditBinding
import com.theost.walletok.delegates.*
import com.theost.walletok.utils.DateTimeUtils
import com.theost.walletok.utils.StringUtils
import com.theost.walletok.utils.addTo
import com.theost.walletok.widgets.TransactionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class TransactionEditFragment : Fragment() {

    companion object {
        private const val TRANSACTION_MODEL_KEY = "transaction_model"
        private const val TRANSACTION_TITLE_KEY = "transaction_title"

        fun newFragment(transaction: TransactionCreationModel, titleRes: Int): Fragment {
            val fragment = TransactionEditFragment()
            val bundle = Bundle()
            bundle.putParcelable(TRANSACTION_MODEL_KEY, transaction)
            bundle.putInt(TRANSACTION_TITLE_KEY, titleRes)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var transactionListener: TransactionListener
    private lateinit var binding: FragmentTransactionEditBinding
    private lateinit var currentDate: Calendar

    private var categoryName: String? = null
    private var transaction: TransactionCreationModel? = null

    private val adapter = BaseAdapter()
    private val compositeDisposable = CompositeDisposable()

    private val titleRes: Int
        get() = arguments?.getInt(TRANSACTION_TITLE_KEY)!!

    private var isDefaultDate: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionEditBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding.toolbar.title = getString(titleRes)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        transactionListener = activity as TransactionListener

        adapter.apply {
            addDelegate(PreferenceAdapterDelegate { onPreferenceClicked(it) })
            addDelegate(TitleAdapterDelegate())
        }

        binding.listPreferences.setHasFixedSize(true)
        binding.listPreferences.adapter = adapter

        loadTransactionData()

        binding.submitButton.setOnClickListener {
            transactionListener.onTransactionSubmitted()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transaction = if (savedInstanceState == null) {
            arguments?.getParcelable(TRANSACTION_MODEL_KEY)
        } else {
            savedInstanceState.getParcelable(TRANSACTION_MODEL_KEY)
        }

        currentDate = Calendar.getInstance()
        if (transaction?.dateTime != null) {
            currentDate.time = transaction!!.dateTime!!
            isDefaultDate = false
        } else {
            transaction?.dateTime = Date()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(TRANSACTION_MODEL_KEY, transaction)
        super.onSaveInstanceState(outState)
    }

    private fun onPreferenceClicked(preferenceName: String) {
        when (preferenceName) {
            TransactionPreferenceType.VALUE.uiName -> transactionListener.onValueEdit()
            TransactionPreferenceType.TYPE.uiName -> transactionListener.onTypeEdit()
            TransactionPreferenceType.CATEGORY.uiName -> transactionListener.onCategoryEdit()
            TransactionPreferenceType.DATE.uiName -> pickDate()
        }
    }

    private fun pickDate() {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date()
            date.time = selection
            currentDate.time = date
            pickTime()
        }
        datePicker.show(activity?.supportFragmentManager!!, "date_picker")
    }

    private fun pickTime() {
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(CLOCK_24H).build()
        timePicker.addOnPositiveButtonClickListener {
            currentDate.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            currentDate.set(Calendar.MINUTE, timePicker.minute)
            onDateSubmitted()
        }
        timePicker.show(activity?.supportFragmentManager!!, "time_picker")
    }

    private fun onDateSubmitted() {
        isDefaultDate = false
        transaction?.dateTime = currentDate.time
        loadTransactionData()
    }

    private fun loadTransactionData() {
        CategoriesRepository.getCategories().subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                categoryName = list.find { item -> item.id == transaction?.category }?.name
                adapter.setData(getPreferencesList())
            }, {
                ErrorMessageHelper.setUpErrorMessage(binding.errorWidget) {
                    loadTransactionData()
                }
            }).addTo(compositeDisposable)
    }

    private fun getPreferencesList(): List<Any> {
        val value = StringUtils.formatMoney(
            StringUtils.convertMoneyForDisplay(
                transaction?.value ?: 0
            )
        ) + " " + (transaction?.currency ?: getString(R.string.wallet_rub))
        return listOf(
            ListTitle(getString(R.string.basic)),
            TransactionPreference(
                TransactionPreferenceType.VALUE,
                value,
                true
            ),
            TransactionPreference(
                TransactionPreferenceType.TYPE,
                transaction?.type ?: "",
                true
            ),
            TransactionPreference(
                TransactionPreferenceType.CATEGORY,
                categoryName ?: "",
                true
            ),
            ListTitle(getString(R.string.additional)),
            TransactionPreference(
                TransactionPreferenceType.DATE,
                if (!isDefaultDate)
                    DateTimeUtils.getFormattedDateOrCurrent(transaction!!.dateTime!!)
                else getString(R.string.now),
                true
            )
        )
    }

}