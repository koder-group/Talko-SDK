package com.koder.ellen.ui.main

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.squareup.picasso.Picasso
import java.util.*


internal class AvatarFragment : Fragment() {

    companion object {
        fun newInstance() = AvatarFragment()
        private const val TAG = "AvatarFragment"
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable App Bar menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_avatar, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.run {
            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

            (this as MessengerActivity).setSupportActionBar(findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.title = resources.getString(R.string.select_avatar)

            // RecyclerView
            val spanCount = 7; // # of columns

//        viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            viewManager = GridLayoutManager(this, spanCount);

            val avatarUrls = getAvatarUrls()
            viewAdapter = AvatarAdapter(this, avatarUrls)

            recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(false)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
//            int spacing = 50; // 50px
//            boolean includeEdge = false;
            recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, 5.px, false))

        } ?: throw Throwable("invalid activity")
//        viewModel.updateActionBarTitle(getResources().getString(R.string.select_avatar))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
//        inflater.inflate(R.menu.message_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle presses on the action bar items
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.supportFragmentManager?.popBackStack()
                (activity as MessengerActivity).openDrawer()
                Log.d(TAG, "Go back")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun getAvatarUrls(): MutableList<String> {
        val avatars = mutableListOf<String>(
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-0.png?alt=media&token=85438da0-ee7a-4fe9-806e-f50f16216e9b", // user-0
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-1.png?alt=media&token=88a0e092-9aa9-4ecd-9048-77b87efbe4fe", // user-1
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-2.png?alt=media&token=8b2fc234-4159-41f0-b160-433b5b05ed1b", // user-2
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-3.png?alt=media&token=fd8d7730-f0b2-479b-a787-91f10ac24e43", // user-3
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-4.png?alt=media&token=61a4f12c-aa63-4062-8d40-114876b11854", // user-4
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-5.png?alt=media&token=c8c35693-ef5f-455e-a7b5-7c1da01d15fb", // user-5
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-6.png?alt=media&token=e870241d-fa6c-44fa-8e72-9e1826423ffd", // user-6
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-7.png?alt=media&token=777dce9f-60f7-43a7-9b06-39ce5fff815a", // user-7
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-8.png?alt=media&token=6b7c8901-2f85-4d77-a795-fef9a370f8a6", // user-8
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-9.png?alt=media&token=d26b3a1f-d781-41ed-9239-d0b73543bda6", // user-9
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-10.png?alt=media&token=931cb667-3674-4bae-a66f-27cdfd0f7702", // user-10
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-11.png?alt=media&token=96debd95-16c6-488b-b673-a906f7b099e0", // user-11
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-12.png?alt=media&token=1627b4cc-8f72-455b-a9cf-a91adf5c744f", // user-12
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-13.png?alt=media&token=8dc8e8c2-5470-4ba9-8a0d-f5114543af9c", // user-13
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-14.png?alt=media&token=31985deb-7bc1-45b6-9ce1-078c8e7d3a0a", // user-14
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-15.png?alt=media&token=9c6a3d33-0a1a-49ec-a902-f65466f8c107", // user-15
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-16.png?alt=media&token=76d0c411-4df8-47d6-b60c-a6d2fb7fdca7", // user-16
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-17.png?alt=media&token=357ab283-8066-4348-b77d-b62f94fb43e3", // user-17
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-18.png?alt=media&token=2e91ece5-0d41-499e-b179-1468275f8be5", // user-18
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-19.png?alt=media&token=8ff932d2-a99d-414a-81c6-6e9c31d178b3", // user-19
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-20.png?alt=media&token=d936b3f0-0bdf-46f6-9035-f74e83527083", // user-20
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-21.png?alt=media&token=ef8abb50-062d-45d2-970a-f42164fc049f", // user-21
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-22.png?alt=media&token=897b261e-2070-49a5-b0f4-b7dd117f43b3", // user-22
                "https://firebasestorage.googleapis.com/v0/b/ellen-firebase-example.appspot.com/o/Avatars%2Fuser-23.png?alt=media&token=d224efff-5da0-4a93-9527-a00936759d1d"  // user-23
            )

        for (i in 0..38) {
            avatars.add("https://api.adorable.io/avatars/300/${UUID.randomUUID().toString()}")
        }

        return avatars
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        menu.clear()
//        inflater.inflate(R.menu.toolbar_conversations, menu)
//    }

}