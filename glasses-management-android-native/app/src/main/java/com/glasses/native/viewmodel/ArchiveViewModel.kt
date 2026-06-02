package com.glasses.native.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.native.data.model.Customer
import com.glasses.native.data.model.OptometryRecord
import com.glasses.native.data.model.SalesRecord
import com.glasses.native.data.model.TimelineItem
import com.glasses.native.data.repository.ArchiveRepository
import com.glasses.native.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val customer: Customer? = null,
    val timeline: List<TimelineItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddOptometry: Boolean = false,
    val showEditOptometry: Boolean = false,
    val editingOptometry: OptometryRecord? = null,
    val showAddSales: Boolean = false,
    val showEditSales: Boolean = false,
    val editingSales: SalesRecord? = null,
    val showDeleteConfirm: Boolean = false,
    val deletingType: String? = null,
    val deletingId: Long? = null
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState

    fun loadArchive(customerId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val customerResult = customerRepository.getCustomer(customerId)
            val timelineResult = archiveRepository.getTimeline(customerId)

            customerResult.fold(
                onSuccess = { customer ->
                    _uiState.value = _uiState.value.copy(customer = customer)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            )

            timelineResult.fold(
                onSuccess = { timeline ->
                    _uiState.value = _uiState.value.copy(timeline = timeline, isLoading = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun showAddOptometry() {
        _uiState.value = _uiState.value.copy(showAddOptometry = true)
    }

    fun hideAddOptometry() {
        _uiState.value = _uiState.value.copy(showAddOptometry = false)
    }

    fun addOptometry(record: OptometryRecord) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = archiveRepository.addOptometry(record.copy(customerId = _uiState.value.customer?.id))
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showAddOptometry = false, isLoading = false)
                    _uiState.value.customer?.id?.let { loadArchive(it) }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun showEditOptometry(record: OptometryRecord) {
        _uiState.value = _uiState.value.copy(showEditOptometry = true, editingOptometry = record)
    }

    fun hideEditOptometry() {
        _uiState.value = _uiState.value.copy(showEditOptometry = false, editingOptometry = null)
    }

    fun updateOptometry(record: OptometryRecord) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = archiveRepository.updateOptometry(record)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showEditOptometry = false, editingOptometry = null, isLoading = false)
                    _uiState.value.customer?.id?.let { loadArchive(it) }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun showAddSales() {
        _uiState.value = _uiState.value.copy(showAddSales = true)
    }

    fun hideAddSales() {
        _uiState.value = _uiState.value.copy(showAddSales = false)
    }

    fun addSales(record: SalesRecord) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = archiveRepository.addSales(record.copy(customerId = _uiState.value.customer?.id))
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showAddSales = false, isLoading = false)
                    _uiState.value.customer?.id?.let { loadArchive(it) }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun showEditSales(record: SalesRecord) {
        _uiState.value = _uiState.value.copy(showEditSales = true, editingSales = record)
    }

    fun hideEditSales() {
        _uiState.value = _uiState.value.copy(showEditSales = false, editingSales = null)
    }

    fun updateSales(record: SalesRecord) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = archiveRepository.updateSales(record)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showEditSales = false, editingSales = null, isLoading = false)
                    _uiState.value.customer?.id?.let { loadArchive(it) }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun showDeleteConfirm(type: String, id: Long) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, deletingType = type, deletingId = id)
    }

    fun hideDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false, deletingType = null, deletingId = null)
    }

    fun deleteRecord() {
        val type = _uiState.value.deletingType ?: return
        val id = _uiState.value.deletingId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (type == "OPTOMETRY") {
                archiveRepository.deleteOptometry(id)
            } else {
                archiveRepository.deleteSales(id)
            }
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showDeleteConfirm = false, deletingType = null, deletingId = null, isLoading = false)
                    _uiState.value.customer?.id?.let { loadArchive(it) }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        _uiState.value.customer?.id?.let { loadArchive(it) }
    }
}
