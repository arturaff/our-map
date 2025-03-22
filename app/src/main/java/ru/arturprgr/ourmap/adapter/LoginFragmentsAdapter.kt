package ru.arturprgr.ourmap.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.arturprgr.ourmap.ui.EntranceFragment
import ru.arturprgr.ourmap.ui.RegisterFragment

class LoginFragmentsAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf(EntranceFragment(), RegisterFragment())

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}