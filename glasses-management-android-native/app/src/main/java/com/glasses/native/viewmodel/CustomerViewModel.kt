package com.glasses.native.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.native.data.model.Customer
import com.glasses.native.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchKeyword: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalRecords: Long = 0,
    val pageSize: Int = 10,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingCustomer: Customer? = null,
    val showDeleteConfirm: Boolean = false,
    val deletingCustomer: Customer? = null
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState

    init {
        loadCustomers()
    }

    fun loadCustomers(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = customerRepository.getCustomers(
                keyword = _uiState.value.searchKeyword.ifBlank { null },
                page = page,
                size = _uiState.value.pageSize
            )
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        customers = data.records,
                        isLoading = false,
                        currentPage = data.current,
                        totalPages = data.pages,
                        totalRecords = data.total
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun onSearchChange(keyword: String) {
        _uiState.value = _uiState.value.copy(searchKeyword = keyword)
    }

    fun onSearch() {
        loadCustomers(1)
    }

    fun onNextPage() {
        val next = _uiState.value.currentPage + 1
        if (next <= _uiState.value.totalPages) {
            loadCustomers(next)
        }
    }

    fun onPrevPage() {
        val prev = _uiState.value.currentPage - 1
        if (prev >= 1) {
            loadCustomers(prev)
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = customerRepository.addCustomer(customer)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showAddDialog = false, isLoading = false)
                    loadCustomers()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun showEditDialog(customer: Customer) {
        _uiState.value = _uiState.value.copy(showEditDialog = true, editingCustomer = customer)
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editingCustomer = null)
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = customerRepository.updateCustomer(customer)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showEditDialog = false, editingCustomer = null, isLoading = false)
                    loadCustomers(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun showDeleteConfirm(customer: Customer) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, deletingCustomer = customer)
    }

    fun hideDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false, deletingCustomer = null)
    }

    fun deleteCustomer() {
        val customer = _uiState.value.deletingCustomer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = customerRepository.deleteCustomer(customer.id!!)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showDeleteConfirm = false, deletingCustomer = null, isLoading = false)
                    loadCustomers(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadCustomers(_uiState.value.currentPage)
    }
}
