package com.koder.ellen.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.koder.ellen.CompletionCallback
import com.koder.ellen.Messenger
import com.koder.ellen.Messenger.Companion.prefs
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.core.Utils
import com.koder.ellen.core.Utils.Companion.getShape
import com.koder.ellen.data.MessageDataSource
import com.koder.ellen.data.MessageRepository
import com.koder.ellen.data.Result
import com.koder.ellen.model.User
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.main.MainViewModel
import com.koder.ellen.ui.message.MessageViewModel
import com.koder.ellen.ui.search.SearchAdapter


open class UserSearchScreen : Fragment() {

    companion object {
        fun newInstance() = UserSearchScreen()
        private const val TAG = "UserSearchScreen"

        var mClickListener: OnItemClickListener? = null
        @JvmStatic fun setItemClickListener(onItemClickListener: OnItemClickListener) {
            mClickListener = onItemClickListener
        }
    }

    abstract class OnItemClickListener: ClickInterface {}
    interface ClickInterface {
        fun OnItemClickListener(user: User, position: Int)
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var mToolbar: Toolbar

    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val users: MutableList<User> = mutableListOf()
    private lateinit var mProgressBar: ProgressBar

    private lateinit var selectedUser: User

    private val messageViewModel: MessageViewModel by lazy {
        ViewModelProvider(this, BaseViewModelFactory {
            MessageViewModel(
                MessageRepository(MessageDataSource()),
                activity!!.application
            )
        }).get(MessageViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable App Bar menu
        setHasOptionsMenu(true)

        // Always use the day (light) theme
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate Fragment's view
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        mToolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
        mProgressBar = rootView.findViewById<ProgressBar>(R.id.progress_bar)

        val searchEditText: EditText = rootView.findViewById(R.id.search_input)
        searchEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
//                Log.d(TAG, "${s}")

                if(s.toString().isBlank()) {
                    users.clear()
                    viewAdapter.notifyDataSetChanged()

                    mProgressBar.visibility = View.GONE

                    return
                }
            }
        })

        searchEditText.afterTextChangedDelayed {
            if(searchEditText.text.toString().isBlank()) {
                mProgressBar.visibility = View.GONE
            } else {
                mProgressBar.visibility = View.VISIBLE
//                messageViewModel.userSearchChanged(   // TODO Use searchUser instead
//                    searchEditText.text.toString()
//                ) // TODO
                searchUser(searchEditText.text.toString(), object: CompletionCallback() {
                    override fun onCompletion(result: Result<Any>) {
                        if(result is Result.Success) {
                            var resultData = result.data
                            if(resultData is List<*>) resultData = resultData.toMutableList()
                            messageViewModel.userSearchResult.postValue(resultData as MutableList<User>)
                        }
                    }
                })
            }
        }

        searchEditText.requestFocus()
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        val appBar = rootView.findViewById<AppBarLayout>(R.id.appbar_layout)
        appBar.visibility = View.GONE

        // Customizable UI options
        rootView.setBackgroundColor(Color.parseColor(Messenger.screenBackgroundColor))
        val contentFrame = rootView.findViewById<LinearLayout>(R.id.content_frame)

        // Dark mode
        var color = "#FFFFFF"
        val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                color = activity?.resources!!.getString(R.color.dmBackground)
            }
        }

        val shape = getShape(Messenger.screenCornerRadius[0].px.toFloat(), Messenger.screenCornerRadius[1].px.toFloat(), Messenger.screenCornerRadius[2].px.toFloat(), Messenger.screenCornerRadius[3].px.toFloat(), color)
        contentFrame.background = shape

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // App Bar title
        activity?.run {
            // Setup RecyclerView
            viewManager = LinearLayoutManager(this)
            viewAdapter = SearchAdapter(this, users, this@UserSearchScreen)
            recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(false)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
            recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        } ?: throw Throwable("invalid activity")

        // Observer, User search result
        messageViewModel.userSearchResult.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "User search result changed")
            Log.d(TAG, "${it}")
            users.clear()
            users.addAll(it)
            // Remove the current user from results
            val found = users.find { it.userId.equals(prefs?.externalUserId, ignoreCase = true)}
            found?.let {
                users.remove(found)
            }
            viewAdapter.notifyDataSetChanged()

            mProgressBar.visibility = View.GONE
        })
    }

    private fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }

    fun TextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(0, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }

    fun sendClick(user: User, position: Int) {
        mClickListener?.OnItemClickListener(user, position)
    }

    fun setSelectedUser(user: User) {
        selectedUser = user
    }

    fun getSelectedUser(): User? {
        if(!::selectedUser.isInitialized) return null
        return selectedUser
    }

    // Extensions for dp-px conversion
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    open fun searchUser(searchString: String, callback: CompletionCallback) {
        messageViewModel.userSearchChanged(searchString)
    }
}
