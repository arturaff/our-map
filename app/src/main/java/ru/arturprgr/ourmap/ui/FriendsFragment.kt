package ru.arturprgr.ourmap.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.arturprgr.ourmap.MainActivity
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {
    private lateinit var binding: FragmentFriendsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)

        binding.apply {
            friends.layoutManager = LinearLayoutManager(requireContext())
            friends.adapter = MainActivity.friendsAdapter
            userId.text = "${getString(R.string.your_id)} ${FirebaseAuth.getInstance().uid}"
            userId.setOnClickListener {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "uid",
                        FirebaseAuth.getInstance().uid
                    )
                )
                Toast.makeText(
                    requireContext(), getString(R.string.id_has_been_copied), Toast.LENGTH_LONG
                ).show()
            }

            add.setOnClickListener {
                val view = View.inflate(requireContext(), R.layout.layout_add_friend, null)
                val userId: TextInputEditText = view.findViewById(R.id.user_id)
                AlertDialog.Builder(requireContext()).apply {
                    setView(view)
                    setTitle(getString(R.string.add_friend))
                    setMessage(getString(R.string.add_a_friend_to_see_it_on_the_map))
                    setPositiveButton(getString(R.string.add)) { _, _ ->
                        if ("${userId.text}" == "${FirebaseAuth.getInstance().uid}") {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.this_is_your_ID),
                                Toast.LENGTH_LONG
                            ).show()
                        } else if ("${userId.text}".length != 28) {
                            Log.d("Attempt", "${userId.text}" + "${userId.text}".length)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.the_length_of_the_user_ID_should_be_28_characters),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val reference =
                                FirebaseDatabase.getInstance().getReference("ourmap/${userId.text}")
                            reference.get().apply {
                                addOnSuccessListener { users ->
                                    if (users.value == null) {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.there_is_no_user_with_such_ID),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        reference.child("invites").get().apply {
                                            addOnSuccessListener { friends ->
                                                if (!"${friends.value}".contains("${userId.text}")) this.result.ref.setValue(
                                                    "${result.value}${FirebaseAuth.getInstance().uid};".trim()
                                                        .replace("null", "")
                                                ) else Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.this_friend_has_already_been_added),
                                                    Toast.LENGTH_LONG
                                                ).show()

                                            }
                                            addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.an_error_occurred),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                                addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.an_error_occurred),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                    setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                    show()
                }
            }
        }

        return binding.root
    }
}