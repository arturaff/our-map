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
import org.osmdroid.util.GeoPoint
import ru.arturprgr.ourmap.MainActivity
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.LayoutFriendBinding
import ru.arturprgr.ourmap.model.User
import ru.arturprgr.ourmap.ui.MapFragment

class FriendsAdapter : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    val list: ArrayList<User> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(user: User) = with(LayoutFriendBinding.bind(itemView)) {
            val myRef = FirebaseDatabase.getInstance()
                .getReference("ourmap/${FirebaseAuth.getInstance().uid}")
            val userRef = FirebaseDatabase.getInstance().getReference("ourmap/${user.uid}")
            userId.text = "ID: ${user.uid}"
            userRef.apply {
                child("name").get().addOnSuccessListener { name ->
                    child("status").get().addOnSuccessListener { status ->
                        main.setOnClickListener {
                            val view = View.inflate(main.context, R.layout.layout_user, null)
                            AlertDialog.Builder(main.context).apply {
                                view.findViewById<TextView>(R.id.name).text = "${name.value}"
                                view.findViewById<TextView>(R.id.status).text = "${status.value}"
                                setView(view)
                                show()
                            }
                        }
                    }
                }
            }
            if (user.isFriend) {
                delete.isVisible = true
                confirm.isVisible = false
                cancel.isVisible = false
                name.text = user.name
                root.setOnLongClickListener {
                    if (user.geoPoint != null) {
                        MainActivity.viewMap()
                        MapFragment.setCenter(user.geoPoint!!)
                    } else Toast.makeText(
                        root.context,
                        root.context.getString(R.string.the_coordinates_are_still_unknown),
                        Toast.LENGTH_LONG
                    ).show()
                    true
                }
                delete.setOnClickListener {
//                    Удаление
                    AlertDialog.Builder(root.context).apply {
                        setIcon(R.drawable.ic_delete)
                        setTitle(R.string.removing_from_friends)
                        setMessage(R.string.сonfirm_the_action)
                        setPositiveButton(R.string.yes) { _, _ ->
                            userRef.child("friends").get().addOnSuccessListener {
                                it.ref.setValue(
                                    "${it.value}".replace(
                                        "${FirebaseAuth.getInstance().uid};", ""
                                    ).replace("null", "")
                                )
                                myRef.child("friends").get().addOnSuccessListener {
                                    it.ref.setValue(
                                        "${it.value}".replace("${user.uid};", "")
                                            .replace("null", "")
                                    )
                                    MainActivity.friendsAdapter.removeUser(user)
                                    try {
                                        MapFragment.removeMarker(user.realIndex + 1)
                                    } catch (_: ArrayIndexOutOfBoundsException) {
                                    }
                                }
                            }
                        }
                        setNegativeButton(R.string.no) { _, _ -> }
                        show()
                    }
                }
            } else {
                delete.isVisible = false
                confirm.isVisible = true
                cancel.isVisible = true
                name.text =
                    "${user.name} ${root.resources.getString(R.string.wants_to_add_you_to_friends)}"
                confirm.setOnClickListener {
                    //                     Добавление
                    myRef.apply {
                        child("invites").get().addOnSuccessListener {
                            it.ref.setValue(
                                "${it.value}".replace("${user.uid};", "").replace("null", "")
                            )
                        }
                        child("friends").get().addOnSuccessListener {
                            it.ref.setValue(
                                "${it.value}${user.uid};".replace("null", "")
                            )
                        }
                        userRef.apply {
                            child("friends").get().addOnSuccessListener {
                                it.ref.setValue(
                                    "${it.value}${FirebaseAuth.getInstance().uid};".replace(
                                        "null", ""
                                    )
                                )
                            }
                            child("latitude").get().addOnSuccessListener { latitude ->
                                child("longitude").get().addOnSuccessListener { longitude ->
                                    if (latitude.value != null && longitude.value != null) {
                                        user.geoPoint = GeoPoint(
                                            "${latitude.value}".toDouble(),
                                            "${longitude.value}".toDouble()
                                        )
                                        MapFragment.addMarker(
                                            user.name, user.geoPoint!!
                                        )
                                    }
                                    user.isFriend = true
                                    MainActivity.friendsAdapter.changeUser(user.index, user)
                                }
                            }
                        }
                    }
                }
                cancel.setOnClickListener {
                    myRef.child("invites").get().addOnSuccessListener {
                        it.ref.setValue(
                            "${it.value}".replace("${user.uid};", "").replace("null", "")
                        )
                        MainActivity.friendsAdapter.removeUser(user)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_friend, parent, false
        )
    )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    private fun removeUser(user: User) {
        list.remove(user)
        notifyItemRemoved(user.index)
    }

    private fun changeUser(index: Int, user: User) {
        list[index] = user
        notifyItemChanged(index)
    }

    fun addUser(user: User) {
        list.add(user)
        notifyItemInserted(user.index)
    }
}