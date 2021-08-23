package com.theost.walletok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.theost.walletok.data.models.TransactionCategoryType
import com.theost.walletok.databinding.FragmentTransactionTypeBinding
import com.theost.walletok.delegates.TypeAdapterDelegate
import com.theost.walletok.delegates.TypeItem
import com.theost.walletok.widgets.TransactionTypeListener

class TransactionTypeFragment : Fragment() {

    companion object {
        private const val TRANSACTION_TYPE_KEY = "transaction_type"
        private const val TRANSACTION_TYPE_UNSET = -1

        fun newFragment(savedType: String? = ""): Fragment {
            val fragment = TransactionTypeFragment()
            val bundle = Bundle()
            bundle.putString(TRANSACTION_TYPE_KEY, savedType)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var binding: FragmentTransactionTypeBinding
    private lateinit var typeItems: List<TypeItem>
    private lateinit var savedType: String
    private var lastSelected: Int = TRANSACTION_TYPE_UNSET
    private val adapter = BaseAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTransactionTypeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        if (savedType != "") binding.submitButton.isEnabled = true

        adapter.addDelegate(TypeAdapterDelegate { position ->
            onItemClicked(position)
        })

        binding.listTypes.setHasFixedSize(true)
        binding.listTypes.adapter = adapter

        typeItems = TransactionCategoryType.values().map { type ->
            TypeItem(
                name = type.uiName,
                isSelected = savedType == type.uiName
            )
        }
        adapter.setData(typeItems)
        lastSelected = typeItems.indexOfFirst { it.name == savedType }

        binding.submitButton.setOnClickListener {
            setCurrentType()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedType = if (savedInstanceState == null) {
            arguments?.getString(TRANSACTION_TYPE_KEY) ?: ""
        } else {
            savedInstanceState.getString(TRANSACTION_TYPE_KEY) ?: ""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(TRANSACTION_TYPE_KEY, savedType)
        super.onSaveInstanceState(outState)
    }

    private fun onItemClicked(position: Int) {
        if (lastSelected != TRANSACTION_TYPE_UNSET) typeItems[lastSelected].isSelected = false
        typeItems[position].isSelected = true
        adapter.setData(typeItems)

        savedType = typeItems[position].name
        lastSelected =  position

        if (savedType != "") {
            binding.submitButton.isEnabled = true
        }
    }

    private fun setCurrentType() {
        (activity as TransactionTypeListener).onTypeSubmitted(savedType)
    }

}