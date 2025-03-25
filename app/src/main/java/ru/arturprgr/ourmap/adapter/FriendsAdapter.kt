package ru.arturprgr.ourmap.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.arturprgr.ourmap.MainActivity
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.LayoutFriendBinding
import ru.arturprgr.ourmap.model.User
import ru.arturprgr.ourmap.ui.MapFragment

class FriendsAdapter : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    val friendsList: ArrayList<User> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(friend: User) = with(LayoutFriendBinding.bind(itemView)) {
            val userReference = FirebaseDatabase.getInstance()
                .getReference("ourmap/${FirebaseAuth.getInstance().uid}")
            val friendReference =
                FirebaseDatabase.getInstance().getReference("ourmap/${friend.uid}")
            userId.text = "ID: ${friend.uid}"
            if (friend.isFriend) {
                delete.isVisible = true
                confirm.isVisible = false
                cancel.isVisible = false
                if (friend.geoPoint != null) root.setOnLongClickListener {
                    MainActivity.viewMap()
                    MapFragment.setCenter(friend.geoPoint!!)
                    true
                }
                name.text = friend.name
                delete.setOnClickListener {
//                    Удаление
                    AlertDialog.Builder(friend.context).apply {
                        setIcon(R.drawable.ic_delete)
                        setTitle(R.string.removing_from_friends)
                        setMessage(R.string.сonfirm_the_action)
                        setPositiveButton(R.string.yes) { _, _ ->
                            MapFragment.removeMarker(friend.realIndex + 1)
                            userReference.child("friends").get().addOnSuccessListener {
                                it.ref.setValue(
                                    "${it.value}".replace("${friend.uid};", "").replace("null", "")
                                )
                                MainActivity.friendsAdapter.removeFriend(friend)
                            }
                            friendReference.child("friends").get().addOnSuccessListener {
                                it.ref.setValue(
                                    "${it.value}".replace(
                                        "${FirebaseAuth.getInstance().uid};", ""
                                    ).replace("null", "")
                                )
                            }
                        }
                        setNegativeButton(R.string.no) { _, _ -> }
                        create().show()
                    }
                }
            } else {
                delete.isVisible = false
                confirm.isVisible = true
                cancel.isVisible = true
                name.text =
                    "${friend.name} ${root.resources.getString(R.string.wants_to_add_you_to_friends)}"
                confirm.setOnClickListener {
                    //                     Добавление
                    userReference.apply {
                        child("invites").get().addOnSuccessListener {
                            it.ref.setValue(
                                "${it.value}".replace("${friend.uid};", "").replace("null", "")
                            )
                        }
                        child("friends").get().addOnSuccessListener {
                            it.ref.setValue("${it.value}${friend.uid};".replace("null", ""))
                            MainActivity.friendsAdapter.removeFriend(friend)
                        }
                    }
                    friendReference.child("friends").get().addOnSuccessListener {
                        it.ref.setValue(
                            "${it.value}${FirebaseAuth.getInstance().uid};".replace("null", "")
                        )
                    }
                    MainActivity.friendsAdapter.removeFriend(friend)
                    friend.isFriend = true
                    MainActivity.friendsAdapter.addFriend(friend)
                }
                cancel.setOnClickListener {
                    userReference.child("invites").get().addOnSuccessListener {
                        it.ref.setValue(
                            "${it.value}".replace("${friend.uid};", "").replace("null", "")
                        )
                        MainActivity.friendsAdapter.removeFriend(friend)
                    }
                }
            }
            root.setOnClickListener {
                val view = View.inflate(friend.context, R.layout.layout_account, null)
                androidx.appcompat.app.AlertDialog.Builder(friend.context).apply {
                    view.findViewById<TextView>(R.id.name).text = friend.name
                    view.findViewById<TextView>(R.id.status).text = friend.status
                    setView(view)
                    create()
                    show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_friend, parent, false
        )
    )

    override fun getItemCount(): Int = friendsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(friendsList[position])

    private fun removeFriend(friend: User) {
        friendsList.remove(friend)
        notifyItemRemoved(friend.index)
    }

    fun addFriend(friend: User) {
        friendsList.add(friend)
        notifyItemInserted(friendsList.size - 1)
    }
}