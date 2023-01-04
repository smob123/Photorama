package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.photorama.R
import com.example.photorama.adapters.SectionsPagerAdapter
import kotlinx.android.synthetic.main.search_fragment.*

/**
 * @author Sultan
 * user, and hashtag search fragment.
 */

class SearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // hide the action bar
        (activity as AppCompatActivity).supportActionBar?.hide()

        // initialize the view pager's adapter
        val sectionsPagerAdapter = SectionsPagerAdapter(
            requireActivity(),
            childFragmentManager
        )

        view_pager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(view_pager)

        setBackBtnListener()
        showKeyBoard()
    }

    /**
     * sets the click listener for the back button.
     */
    private fun setBackBtnListener() {
        back_btn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    /**
     * displays the keyboard once this activity is launched
     */
    private fun showKeyBoard() {
        // request focus from the EditText element
        search_bar.requestFocus()
    }
}
