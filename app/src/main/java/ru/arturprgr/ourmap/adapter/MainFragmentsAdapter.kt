package ru.arturprgr.ourmap.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.arturprgr.ourmap.ui.FriendsFragment
import ru.arturprgr.ourmap.ui.MapFragment

class MainFragmentsAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf(MapFragment(), FriendsFragment())

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}