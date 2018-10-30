package net.komunan.komunantw.ui.main

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.ui.common.TWBaseViewModel
import net.komunan.komunantw.draweeView
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.string
import net.komunan.komunantw.repository.entity.*
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class HomeTabFragment: Fragment() {
    companion object {
        private const val PARAMETER_COLUMN_ID = "HomeTabFragment.PARAMETER_COLUMN_ID"

        @JvmStatic
        fun create(timeline: Timeline): Fragment {
            return HomeTabFragment().apply {
                arguments = Bundle().apply {
                    putLong(PARAMETER_COLUMN_ID, timeline.id)
                }
            }
        }
    }

    private val viewModel by lazy { makeViewModel() }
    private val ui = HomeTabUI()
    private val adapter = HomeTabAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ui.run {
            container.adapter = adapter
        }
        viewModel.run {
            tweets.observeOnNotNull(this@HomeTabFragment) { tweets ->
                adapter.submitList(tweets)
            }
        }
    }

    private fun makeViewModel(): HomeTabViewModel {
        val timelineId: Long = arguments!!.getLong(PARAMETER_COLUMN_ID)
        val factory = HomeTabViewModel.Factory(timelineId)
        return ViewModelProviders.of(this, factory).get(HomeTabViewModel::class.java)
    }

    private class HomeTabViewModel(private val timelineId: Long): TWBaseViewModel() {
        private val sources: LiveData<List<Source>>
            get() = Source.findByTimelineIdAsync(timelineId)
        private val sourceIds: LiveData<List<Long>>
            get() = Transformations.map(sources) { sources -> sources.map { it.id } }

        val tweets: LiveData<PagedList<TweetDetail>>
            get() = Transformations.switchMap(sourceIds) { Tweet.findBySourceIdsAsync(it) }

        class Factory(private val timelineId: Long): ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass == HomeTabViewModel::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    return HomeTabViewModel(timelineId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}.")
            }
        }
    }

    private class HomeTabAdapter: PagedListAdapter<TweetDetail, TweetViewHolder>(DIFF_CALLBACK) {
        companion object {
            val DIFF_CALLBACK = object: DiffUtil.ItemCallback<TweetDetail>() {
                override fun areItemsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean {
                    return oldItem.id == newItem.id
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
            val ui = TweetUI()
            val view = ui.createView(AnkoContext.create(parent.context, parent))
            return TweetViewHolder(ui, view)
        }

        override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private class HomeTabUI: AnkoComponent<HomeTabFragment> {
        lateinit var container: RecyclerView

        override fun createView(ui: AnkoContext<HomeTabFragment>): View = with(ui) {
            container = recyclerView {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            return@with container
        }
    }

    private class TweetUI: AnkoComponent<ViewGroup> {
        lateinit var userIcon: ImageView
        lateinit var userName: TextView
        lateinit var screenName: TextView
        lateinit var tweetDateTime: TextView
        lateinit var tweetText: TextView

        @SuppressLint("ResourceType")
        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            linearLayout {
                userIcon = draweeView {
                    id = R.id.tweet_user_icon
                }.lparams(dip(48), dip(48))
                verticalLayout {
                    linearLayout {
                        userName = textView {
                            id = R.id.tweet_user_name
                        }
                        screenName = textView {
                            id = R.id.tweet_user_screen_name
                        }
                        tweetDateTime = textView {
                            id = R.id.tweet_date_time
                        }
                    }
                    tweetText = textView {
                        id = R.id.tweet_text
                    }
                }
            }
        }

        fun bind(tweet: TweetDetail) {
            launch(UI) {
                val user = async(CommonPool) { User.find(tweet.userId) }
                userIcon.setImageURI(Uri.parse(user.await().imageUrl))
                userName.text = user.await().name
                screenName.text = R.string.format_screen_name.string(user.await().screenName)
                tweetDateTime.text = "test"
                tweetText.text = tweet.text
            }
        }
    }

    private class TweetViewHolder(val ui: TweetUI, view: View): RecyclerView.ViewHolder(view) {
        fun bind(tweet: TweetDetail?) {
            if (tweet == null) {
                return
            }
            ui.bind(tweet)
        }
    }
}
