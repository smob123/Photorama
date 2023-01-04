package com.example.photorama.screens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.photorama.MainAppActivity
import com.example.photorama.R
import com.example.photorama.viewModels.AuthViewModel
import com.example.photorama.viewModels.AuthViewModelFactory
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * @author Sultan
 * login fragment.
 */

class LoginFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize view model
        val factory = AuthViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(requireActivity(), factory).get(AuthViewModel::class.java)
        initLoginObserver()
        initErrorObserver()

        setChangeListeners(username_input)
        setChangeListeners(password_txt)
        setPasswordActionListener()
        setLoginBtnListener()
        setSignupBtnListener()
    }

    /**
     * observes login requests.
     */
    private fun initLoginObserver() {
        viewModel.getUserInfo().observe(requireActivity(), Observer { user ->
            if (user == null) {
                return@Observer
            }

            // move on to the app's main screen
            val intent = Intent(
                requireActivity(),
                MainAppActivity::class.java
            )
            requireActivity().startActivity(intent)
        })
    }

    /**
     * observes error messages returned from the server.
     */
    private fun initErrorObserver() {
        viewModel.getAuthErrorMessage().observe(requireActivity(), Observer { message ->
            if (message == null) {
                return@Observer
            }

            Toast.makeText(requireActivity(), message.getMessage(), Toast.LENGTH_LONG).show()
        })
    }

    /**
     * sets on change listeners for the edit text elements.
     * @param editText the element to add the listener to
     */
    private fun setChangeListeners(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (newText != null) {
                    // check if the element is empty
                    isFieldEmpty(editText)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {}
        })

        // check if the element is empty on focus change
        editText.setOnFocusChangeListener { _, inFocus ->
            if (!inFocus) {
                isFieldEmpty(editText)
            }
        }
    }

    /**
     * sets the on click listener for the sign up button.
     */
    private fun setSignupBtnListener() {
        signup_btn.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.form, SignupFragment())
                .commit()
        }
    }

    /**
     * check if an edit text element is empty.
     * @param editText the element to check
     */
    private fun isFieldEmpty(editText: EditText): Boolean {
        val text = editText.text.toString()

        if (text.isEmpty()) {
            // set an error message to the element
            editText.error = "this field cannot be empty"
            return true
        } else {
            editText.error = null
            return false
        }
    }

    /**
     * sets the on click listener for the login button.
     */
    private fun setLoginBtnListener() {
        // initialize the submit button listener
        login_btn.setOnClickListener {
            submitInput()
        }
    }

    /**
     * sets the on editor action listener for the password text view, in order to verify, and submit
     * the login credentials to the server.
     */
    private fun setPasswordActionListener() {
        password_txt.setOnEditorActionListener { _, _, _ ->
            submitInput()
            return@setOnEditorActionListener true
        }
    }

    /**
     * sends the input to the server.
     */
    private fun submitInput() {
        // check if any of the fields is empty
        if (isFieldEmpty(username_input) || isFieldEmpty(password_txt)) {
            return
        }

        // send the input to the server
        val username = username_input.text.toString()
        val password = password_txt.text.toString()

        // get the device's firebase token, and send it with the username, and password
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { fbRes ->
            if (fbRes.isSuccessful) {
                viewModel.login(username, password, fbRes.result!!.token)
            } else if (fbRes.isCanceled) {
                Toast.makeText(activity, "cancelled fetching firebase token", Toast.LENGTH_LONG)
                    .show()
            } else if (fbRes.exception != null) {
                Toast.makeText(activity, "couldn't get firebase token", Toast.LENGTH_LONG).show()
            }
        }
    }
}
