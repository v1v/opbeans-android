package co.elastic.apm.opbeans.modules.orders

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.elastic.apm.opbeans.R
import co.elastic.apm.opbeans.app.ui.ListDivider
import co.elastic.apm.opbeans.app.ui.LoadableList
import co.elastic.apm.opbeans.modules.orders.data.models.OrderStateItem
import co.elastic.apm.opbeans.modules.orders.ui.OrdersNetworkState
import co.elastic.apm.opbeans.modules.orders.ui.OrdersViewModel
import co.elastic.apm.opbeans.modules.orders.ui.list.OrderListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var list: LoadableList
    private lateinit var adapter: OrderListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initList()

        lifecycleScope.launch {
            viewModel.state.collectLatest {
                when (it) {
                    is OrdersNetworkState.Loading -> list.showLoading()
                    is OrdersNetworkState.ErrorLoading -> list.showError(it.exception)
                    is OrdersNetworkState.FinishedLoading -> list.hideLoading()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.orders.collectLatest {
                populateList(it)
            }
        }

        viewModel.fetchOrders()
    }

    private fun populateList(orders: List<OrderStateItem>) {
        list.showList()
        adapter.submitList(orders)
    }

    private fun initList() {
        adapter = OrderListAdapter()
        val recyclerView = list.getList()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(ListDivider(requireContext()))
        recyclerView.adapter = adapter

        list.onRefreshRequested { viewModel.fetchOrders() }
    }

    private fun initViews(view: View) {
        list = view.findViewById(R.id.order_list)
    }
}