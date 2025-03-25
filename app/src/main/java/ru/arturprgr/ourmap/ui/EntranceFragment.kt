package ru.arturprgr.ourmap.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import ru.arturprgr.ourmap.LoginActivity
import ru.arturprgr.ourmap.MainActivity
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.FragmentEntranceBinding

class EntranceFragment : Fragment() {
    private lateinit var binding: FragmentEntranceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEntranceBinding.inflate(inflater, container, false)

        binding.apply {
            createAccount.setOnClickListener {
                LoginActivity.binding.root.currentItem = 1
            }

            signIn.setOnClickListener {
                if ("${email.text}" != "" && "${password.text}" != "") FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    "${email.text}",
                    "${password.text}"
                ).apply {
                    addOnSuccessListener {
                        activity?.finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                    addOnFailureListener {
                        password.error =
                            getString(R.string.incredible_password_or_email_entered)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.fill_all_fields),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        return binding.root
    }
}