package ru.tinkoff.acquiring.sdk.redesign.cards.list.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.redesign.cards.list.models.CardItemUiModel

/**
 * Created by Ivan Golovachev
 */
class CardsListAdapter(
    private val onDeleteClick: (CardItemUiModel) -> Unit
) : RecyclerView.Adapter<CardsListAdapter.CardViewHolder>() {

    private val cards = mutableListOf<CardItemUiModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setCards(cards: List<CardItemUiModel>) {
        if (this.cards.isEmpty()) {
            this.cards.addAll(cards)
            notifyDataSetChanged()
        } else {
            this.cards.clear()
            this.cards.addAll(cards)
            notifyItemRangeChanged(0, cards.size, PAYLOAD_CHANGE_MODE)
        }
    }

    fun onRemoveCard(indexAt: Int) {
        this.cards.removeAt(indexAt)
        notifyItemRemoved(indexAt)
    }

    fun onAddCard(card: CardItemUiModel) {
        //TODO после задачи на добавление карты
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.acq_card_list_item, parent, false) as View
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position], onDeleteClick)
    }

    override fun onBindViewHolder(
        holder: CardViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_CHANGE_MODE)) {
            holder.bindDeleteVisibility(cards.get(position).showDelete)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cardNameView =
            itemView.findViewById<TextView>(R.id.acq_card_list_item_masked_name)
        private val cardDeleteIcon =
            itemView.findViewById<ImageView>(R.id.acq_card_list_item_delete)

        fun bind(
            card: CardItemUiModel,
            onDeleteClick: (CardItemUiModel) -> Unit
        ) {
            cardNameView.text = itemView.context.getString(
                R.string.acq_cardlist_bankname,
                card.bankName,
                card.tail
            )
            bindDeleteVisibility(card.showDelete)
            cardDeleteIcon.setOnClickListener { onDeleteClick(card) }
        }

        fun bindDeleteVisibility(showDelete: Boolean) {
            cardDeleteIcon.isVisible = showDelete
        }
    }

    companion object {
        const val PAYLOAD_CHANGE_MODE = "PAYLOAD_CHANGE_MODE"
    }
}

