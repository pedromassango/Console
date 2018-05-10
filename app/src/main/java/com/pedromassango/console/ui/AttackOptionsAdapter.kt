package com.pedromassango.console.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pedromassango.console.R
import kotlinx.android.synthetic.main.row_option.view.*

/**
 * Created by Pedro Massango on 2/23/18.
 */
class AttackViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bindViews(attack: AttackOption) {
        with(view) {
            with(attack) {
                tv_option_title.text = attackName.toUpperCase()
            }
        }
    }
}

class AttackOptionsAdapter(private val attacks: ArrayList<AttackOption>,
                           private val itemClickListener: (AttackOption)-> Unit): RecyclerView.Adapter<AttackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AttackViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.row_option, parent, false)
        return AttackViewHolder(view)
    }

    override fun getItemCount(): Int = attacks.size

    override fun onBindViewHolder(holder: AttackViewHolder?, position: Int) {
       holder?.bindViews( attack = attacks[position])
        holder?.itemView!!.setOnClickListener { itemClickListener.invoke( attacks[position])
        }
    }
}