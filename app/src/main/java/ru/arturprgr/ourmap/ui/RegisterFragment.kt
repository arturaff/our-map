package ru.arturprgr.ourmap.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.arturprgr.ourmap.LoginActivity
import ru.arturprgr.ourmap.MainActivity
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.apply {
            signIn.setOnClickListener {
                LoginActivity.binding.root.currentItem = 0
            }

            register.setOnClickListener {
                if ("${email.text}" == "" &&
                    "${name.text}" == "" &&
                    "${password.text}" == "" &&
                    "${password2.text}" == ""
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.fill_all_fields),
                        Toast.LENGTH_LONG
                    ).show()
                } else if ("${password.text}".length <= 8) {
                    password.error == getString(R.string.too_short_password_invent_at_least_8_characters)
                } else if ("${password.text}" != "${password2.text}") {
                    password2.error == getString(R.string.passwords_do_not_match)
                } else FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    "${email.text}",
                   "${password.text}"
                ).apply {
                    addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("ourmap/${FirebaseAuth.getInstance().uid}").apply {
                            child("name").setValue("${name.text}".trim())
                            child("status").setValue("Олд")
                            child("friends").setValue("")
                            child("invites").setValue("")
                        }
                        activity?.finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                    addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.an_error_has_occurred_repeat_the_registration_again),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            return binding.root
        }
    }
}