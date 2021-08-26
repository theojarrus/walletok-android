package com.theost.walletok.presentation.wallet_details.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.theost.walletok.data.repositories.CategoriesRepository
import com.theost.walletok.delegates.CategoryItem
import com.theost.walletok.utils.Resource
import com.theost.walletok.utils.Status
import com.theost.walletok.utils.addTo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class UserCategoriesViewModel : ViewModel() {

    private val _allData = MutableLiveData<List<CategoryItem>>()
    val allData: LiveData<List<CategoryItem>> = _allData
    private val compositeDisposable = CompositeDisposable()

    private val _loadingStatus = MutableLiveData<Resource<*>>()
    val loadingStatus: LiveData<Resource<*>> = _loadingStatus

    fun loadData() {
        _loadingStatus.postValue(Resource.Loading(Unit))
        CategoriesRepository.getCategories().subscribeOn(Schedulers.io())
            .subscribe({ resource ->
                if (resource.status == Status.SUCCESS) {
                    val categoryItems = resource.data!!.map { category ->
                        CategoryItem(
                            id = category.id,
                            name = category.name,
                            iconColor = category.iconColor,
                            iconUrl = category.iconLink,
                            isSelected = false
                        )
                    }
                    _allData.postValue(categoryItems)
                    _loadingStatus.postValue(Resource.Success(Unit))
                }
            }, {
                _loadingStatus.postValue(Resource.Error(Unit, it))
            }).addTo(compositeDisposable)
    }

    fun selectData(position: Int) {
        val categoryItems = mutableListOf<CategoryItem>()
        categoryItems.addAll(_allData.value!!)
        categoryItems[position].isSelected = !categoryItems[position].isSelected
        _allData.value = categoryItems
    }

    fun deleteSelectedData() {
        _loadingStatus.postValue(Resource.Loading(Unit))
        val selectedCategories = mutableListOf<Int>()
        _allData.value?.forEach { if (it.isSelected) selectedCategories.add(it.id) }
//        CategoriesRepository.removeCategories(selectedCategories)
//            .subscribeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                _loadingStatus.postValue(Resource.Success(Unit))
//            }, {
//                _loadingStatus.postValue(Resource.Error(Unit, it))
//            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}