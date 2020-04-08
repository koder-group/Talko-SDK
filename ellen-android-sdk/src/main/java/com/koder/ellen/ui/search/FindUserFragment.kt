package com.koder.ellen.ui.search

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koder.ellen.MessengerActivity
import com.koder.ellen.R
import com.koder.ellen.data.MessageDataSource
import com.koder.ellen.data.MessageRepository
import com.koder.ellen.model.User
import com.koder.ellen.ui.BaseViewModelFactory
import com.koder.ellen.ui.main.MainViewModel
import com.koder.ellen.ui.message.MessageViewModel
import java.util.*

// Used when adding a participant to a conversation
internal class FindUserFragment : Fragment() {

    companion object {
        fun newInstance() = FindUserFragment()
        private const val TAG = "AddUserFragment"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var mToolbar: Toolbar
    private lateinit var mProgressBar: ProgressBar

    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val users: MutableList<User> = mutableListOf()
    private lateinit var excludeUserIds: ArrayList<String>

    val messageViewModel: MessageViewModel by lazy {
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

        // Get user IDs to exclude
        excludeUserIds = getArguments()?.getStringArrayList("userIds") as ArrayList<String>
        Log.d(TAG, "excludeUserIds ${excludeUserIds}")
    }

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

//                messageViewModel.messageDataChanged(
//                    messageEditText.text.toString()
//                )
//
//                // Prevent infinite loop by unregistering and registering listener
//                messageEditText.removeTextChangedListener(this)
//                // Autocolor mentions
//                val autocolored = autoColorMentions(s.toString())
////                messageEditText.setText(HtmlCompat.fromHtml(autocolored, HtmlCompat.FROM_HTML_MODE_COMPACT))
//                s?.replace(0, s.length, HtmlCompat.fromHtml(autocolored, HtmlCompat.FROM_HTML_MODE_COMPACT))
//                // Set cursor to the end of input
//                messageEditText.setSelection(messageEditText.length())
//                messageEditText.addTextChangedListener(this)

                Log.d(TAG, "${s}")

                if(s.toString().isBlank()) {
                    users.clear()
                    viewAdapter.notifyDataSetChanged()
                    mProgressBar.visibility = View.GONE
                    return
                }

//                val timer = Timer()
//                timer.schedule(object: TimerTask() {
//                    override fun run() {
//                        // do your actual work here
//                        messageViewModel.userSearchChanged(
//                            s.toString()
//                        )
//                    }
//                }, 2000); // 600ms delay before the timer executes the „run“ method from TimerTask

            }
        })

        searchEditText.afterTextChangedDelayed {
            if(searchEditText.text.toString().isBlank()) {
                mProgressBar.visibility = View.GONE
            } else {
                mProgressBar.visibility = View.VISIBLE
                messageViewModel.userSearchChanged(
                    searchEditText.text.toString()
                )
            }
        }

        searchEditText.requestFocus()
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // App Bar title
        activity?.run {
            // Communicate with host Activity
            viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//            viewModel.updateActionBarTitle(getResources().getString(R.string.conversations))

            // Toolbar, DrawerLayout
            (this as MessengerActivity).setSupportActionBar(findViewById(R.id.toolbar))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.title = resources.getString(R.string.find_a_user)

            // Setup RecyclerView
            viewManager = LinearLayoutManager(this)
            viewAdapter = FindUserAdapter(this, users, this@FindUserFragment)
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
//            val found = users.find { it.userId.equals(prefs.userId(), ignoreCase = true)}
//            found?.let {
//                users.remove(found)
//            }
            // Remove participants from search results
            for(userId in excludeUserIds) {
                val found = users.find { it.userId.equals(userId, ignoreCase = true) }
                found?.let {
                    users.remove(found)
                }
            }
            viewAdapter.notifyDataSetChanged()

            mProgressBar.visibility = View.GONE
        })

        // Observer, Participant Added
        messageViewModel.participantAdded.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "participantAdded ${it}")


            val intent = Intent(context, FindUserFragment::class.java)
            intent.putExtra("userId", it.userId)
            intent.putExtra("displayName", it.displayName)
            intent.putExtra("tenantId", it.tenantId)
            targetFragment!!.onActivityResult(targetRequestCode, RESULT_OK, intent)

            hideKeyboard(activity as Activity)
            activity?.supportFragmentManager?.popBackStack()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.find_user_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle presses on the action bar items
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "popBackStack")
                hideKeyboard(activity as Activity)
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            R.id.action_message -> {
//                val integrator = IntentIntegrator.forSupportFragment(this)
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
//                integrator.setOrientationLocked(false)
//                integrator.setPrompt("")
//                integrator.setBeepEnabled(false)
//                integrator.setCaptureActivity(CustomScannerActivity::class.java)
//                integrator.initiateScan()
//                Log.d(TAG, "action_message")

                // Show user search fragment
//                (context as MainActivity).showSearchFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    fun finishFragment() {
        hideKeyboard(activity as Activity)
        activity?.supportFragmentManager?.popBackStack()
    }

    fun TextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(0, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }
}
