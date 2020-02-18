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
import com.example.photorama.MainAppActivity
import com.example.photorama.R
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.networking.Mutations
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author Sultan
 * login fragment.
 */

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setChangeListeners(username_input)
        setChangeListeners(password_txt)
        setPasswordActionListener()
        setLoginBtnListener()
        setSignupBtnListener()
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
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { fbRes ->
            sendToServer(username, password, fbRes.token)
        }
    }

    /**
     * attempts to login with the server with the user's input; ie, email, and password.
     * @param username the username from the text field
     * @param password the password from the text field
     */
    private fun sendToServer(username: String, password: String, firebaseToken: String) {
        // run the login mutation
        Mutations(this@LoginFragment.activity!!).login(
            username,
            password,
            firebaseToken,
            onCompleted = { err, result ->
                if (err != null) {
                    val errMessage = "Invalid credentials"

                    val toastMessage: String

                    // check if the error message has to do with the user's credentials
                    if (err.contains(errMessage)) {
                        toastMessage = errMessage
                    } else {
                        // otherwise it must be a connection error
                        toastMessage = "Network error"
                    }

                    // display the error message
                    this@LoginFragment.activity?.runOnUiThread {
                        Toast.makeText(
                            this@LoginFragment.activity!!, toastMessage, Toast.LENGTH_LONG
                        ).show()
                    }

                    return@login
                }
                if (result != null) {
                    if (result.Login() == null) {
                        // display an error message
                        this@LoginFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@LoginFragment.activity!!,
                                "Network error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    // if the request is completed, then store the returned data in the app's cache
                    // for the next time the user launches the app
                    val id = result.Login()!!.id()
                    val uName = result.Login()!!.username()
                    val screenName = result.Login()!!.screenName()
                    val jwt = result.Login()!!.jwt()
                    val followers = result.Login()!!.followers() as List<String>
                    val following = result.Login()!!.following() as List<String>

                    val jsonObject = JSONObject()
                    jsonObject.put("id", id)
                    jsonObject.put("username", uName)
                    jsonObject.put("screenName", screenName)
                    jsonObject.put("jwt", jwt)
                    jsonObject.put("followers", JSONArray(followers))
                    jsonObject.put("following", JSONArray(following))

                    CacheHandler(this@LoginFragment.activity!!.applicationContext)
                        .overWriteCache(jsonObject)

                    // move on to the app's main screen
                    val intent = Intent(
                        this@LoginFragment.activity!!.applicationContext,
                        MainAppActivity::class.java
                    )
                    this@LoginFragment.activity!!.startActivity(intent)
                }
            })
    }
}
