package com.example.transactionrecords

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.buggybank.R
import vcmsa.projects.buggybank.Transaction

class TransactionRecordsAdapter(private var transactions: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionRecordsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.singletransaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTitle.text = transaction.title
        holder.tvCategory.text = transaction.category
        holder.tvPaymentMethod.text = transaction.paymentMethod
        holder.tvAmount.text = transaction.amount.toString()
        holder.tvDate.text = transaction.date

        val context = holder.itemView.context
        holder.tvAmount.setTextColor(
            if (transaction.type.equals("Income", true))
                context.getColor(R.color.green)
            else
                context.getColor(R.color.red)
        )
    }

    override fun getItemCount(): Int = transactions.size

    /**
     * Call this method to update the adapter with a new filtered list.
     */
    fun setFilteredRecords(filteredRecords: List<Transaction>) {
        transactions.clear()
        transactions.addAll(filteredRecords)
        notifyDataSetChanged()
    }
}
