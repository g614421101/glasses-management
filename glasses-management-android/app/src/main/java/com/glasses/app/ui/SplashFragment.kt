package com.glasses.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.glasses.app.R
import com.glasses.app.discovery.ConnectionManager
import com.glasses.app.discovery.ConnectionState
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    companion object {
        fun newInstance() = SplashFragment()
    }

    private lateinit var connectionManager: ConnectionManager
    private lateinit var layoutSearching: LinearLayout
    private lateinit var layoutManual: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var etIp: TextInputEditText
    private lateinit var etPort: TextInputEditText
    private lateinit var btnConnect: MaterialButton
    private lateinit var tvError: TextView
    private lateinit var btnRetry: MaterialButton

    var onConnected: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutSearching = view.findViewById(R.id.layoutSearching)
        layoutManual = view.findViewById(R.id.layoutManual)
        layoutError = view.findViewById(R.id.layoutError)
        etIp = view.findViewById(R.id.etIp)
        etPort = view.findViewById(R.id.etPort)
        btnConnect = view.findViewById(R.id.btnConnect)
        tvError = view.findViewById(R.id.tvError)
        btnRetry = view.findViewById(R.id.btnRetry)

        connectionManager = ConnectionManager(requireContext())

        btnConnect.setOnClickListener {
            val ip = etIp.text.toString().trim()
            val port = etPort.text.toString().trim().toIntOrNull() ?: 8080
            if (ip.isNotEmpty()) {
                lifecycleScope.launch {
                    connectionManager.connectManual(ip, port)
                }
            }
        }

        btnRetry.setOnClickListener {
            lifecycleScope.launch {
                connectionManager.connect()
            }
        }

        lifecycleScope.launch {
            connectionManager.state.collectLatest { state ->
                updateUi(state)
            }
        }

        lifecycleScope.launch {
            connectionManager.connect()
        }
    }

    private fun updateUi(state: ConnectionState) {
        layoutSearching.visibility = if (state is ConnectionState.Searching || state is ConnectionState.Connecting) View.VISIBLE else View.GONE
        layoutManual.visibility = if (state is ConnectionState.ManualInput) View.VISIBLE else View.GONE
        layoutError.visibility = if (state is ConnectionState.Error) View.VISIBLE else View.GONE

        when (state) {
            is ConnectionState.Connected -> {
                onConnected?.invoke(state.url)
            }
            is ConnectionState.Error -> {
                tvError.text = state.message
            }
            else -> {}
        }
    }
}
