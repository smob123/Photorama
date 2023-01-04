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
import kotlinx.android.synthetic.main.signup_fragment.*

/**
 * @author Sultan
 * signup fragment
 */

class SignupFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.signup_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize view model
        val factory = AuthViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(requireActivity(), factory).get(AuthViewModel::class.java)
        initSignupObserver()
        initErrorObserver()

        setUsernameChangeListeners()
        setScreenNameChangeListeners()
        setEmailChangeListeners()
        setPasswordChangeListeners()
        setPasswordActionListener()
        setSignupBtnListener()
        setLoginBtnListener()
    }

    fun initSignupObserver() {
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
     * sets the username edit text on change listeners.
     */
    private fun setUsernameChangeListeners() {
        username_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (newText != null) {
                    isUsernameValid()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {}
        })

        setOnFocusChangeListener(username_input)
    }

    /**
     * checks if the username follows the correct pattern.
     */
    private fun isUsernameValid(): Boolean {
        val text = username_input.text.toString()

        if (text.startsWith("_")) {
            username_input.error = "username cannot start with a _"
            return false
        } else if (text.endsWith("_")) {
            username_input.error = "username cannot end with a _"
            return false
        } else if (text.length < 3 || text.length > 10) {
            username_input.error = "username should be between 3 and 11 characters"
            return false
        } else {
            username_input.error = null
            return true
        }
    }

    /**
     * sets the screen name edit text on change listeners.
     */
    private fun setScreenNameChangeListeners() {
        user_screen_name_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (newText != null) {
                    isScreenNameValid()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {}
        })

        setOnFocusChangeListener(user_screen_name_input)
    }

    /**
     * checks if the screen name follows the correct pattern.
     */
    private fun isScreenNameValid(): Boolean {
        val text = user_screen_name_input.text.toString()

        if (text.isEmpty()) {
            user_screen_name_input.error = "This field cannot be empty"
            return false
        } else {
            user_screen_name_input.error = null
            return true
        }
    }

    /**
     * sets the email edit text on change listeners.
     */
    private fun setEmailChangeListeners() {
        email_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (newText != null) {
                    isEmailValid()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {}
        })

        setOnFocusChangeListener(email_input)
    }

    /**
     * checks if the email follows the correct pattern.
     */
    private fun isEmailValid(): Boolean {
        val pattern =
            Regex(
                "[a-zA-Z]" +
                        "[a-zA-Z0-9\\!\\#\\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]{0,63}" +
                        "\\@[a-zA-Z]" +
                        "[a-zA-Z0-9-]{0,253}" +
                        "[a-zA-Z0-9]" +
                        "\\.[a-z-A-Z]{2,3}"
            )
        val text = email_input.text.toString()

        if (!pattern.matches(text)) {
            email_input.error = "Invalid email address"
            return false
        } else {
            email_input.error = null
            return true
        }
    }

    /**
     * sets the password edit text on change listeners.
     */
    private fun setPasswordChangeListeners() {
        password_txt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (newText != null) {
                    isPasswordValid()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {}
        })

        setOnFocusChangeListener(password_txt)
    }

    /**
     * checks if the password follows the correct pattern.
     */
    private fun isPasswordValid(): Boolean {
        val text = password_txt.text.toString()

        if (text.length < 6) {
            password_txt.error = "Password must be 6 characters at least"
            return false
        } else {
            password_txt.error = null
            return true
        }
    }

    /**
     * checks if an edit text elements is empty when it goes out of focus.
     */
    private fun setOnFocusChangeListener(editText: EditText) {
        editText.setOnFocusChangeListener { _, inFocus ->
            if (!inFocus) {
                val text = editText.text
                if (text.isEmpty()) {
                    editText.error = "This field cannot be empty"
                }
            }
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
     * set the on click listener for the sign up button.
     */
    private fun setSignupBtnListener() {
        // get the button from the layout
        signup_btn.setOnClickListener {
            submitInput()
        }
    }

    /**
     * sends the input to the server if it's valid.
     */
    private fun submitInput() {
        // check if all inputs are valid
        val validInputs = arrayOf(
            isUsernameValid(), isScreenNameValid(),
            isEmailValid(), isPasswordValid()
        )

        for (isValid in validInputs) {
            if (!isValid) {
                return
            }
        }

        // get the device's firebase token, and send it to the server along with the user's input
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { fbRes ->
            // get the username, email, and password from the text feilds
            val username = username_input.text.toString()
            val screenName = user_screen_name_input.text.toString()
            val email = email_input.text.toString()
            val password = password_txt.text.toString()

            // send them to the server
            viewModel.signup(username, email, screenName, password, fbRes.token)
        }
    }

    /**
     * sets the on click listener for the login button.
     */
    private fun setLoginBtnListener() {
        // get the login button
        login_btn.setOnClickListener {
            //create a new fragment transition to the login screen, and replace the current fragment
            activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.form, LoginFragment())
                .commit()
        }
    }
}
