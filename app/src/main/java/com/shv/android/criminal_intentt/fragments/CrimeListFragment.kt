package com.shv.android.criminal_intentt.fragments

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shv.android.criminal_intentt.Crime
import com.shv.android.criminal_intentt.CrimeListViewModel
import com.shv.android.criminal_intentt.R
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment(), MenuProvider {
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeListAdapter? = CrimeListAdapter(emptyList())
    private lateinit var emptyListTextView: TextView

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.addMenuProvider(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        emptyListTextView = view.findViewById(R.id.list_is_empty) as TextView
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner
        ) { crimes ->
            crimes?.let {
                Log.i(TAG, "Got a crimes ${crimes.size}")
                updateUI(crimes)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }


    private fun updateUI(crimes: List<Crime>) {
        emptyListTextView.visibility =
            if (crimes.isEmpty()) View.VISIBLE
            else View.GONE
        adapter = CrimeListAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView =
            itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title
            dateTextView.text = crimeDateFormat()
            //dateTextView.text = DateFormat.getDateFormat(context).format(crime.date).toString()
            solvedImageView.visibility =
                if (crime.isSolved) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        private fun crimeDateFormat(): String {
            val date = DateFormat.getDateFormat(context).format(crime.date)
            val time = DateFormat.getTimeFormat(context).format(crime.date)
            return "$date $time"
        }
            //DateFormat.format("EEEE, LLL dd, yyyy, HH:mm", crime.date).toString()



        override fun onClick(view: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeListAdapter(var crimes: List<Crime>) :
        ListAdapter<Crime, CrimeHolder>(CrimeComparator()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int = crimes.size
    }

    private inner class CrimeComparator : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }

    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            R.id.clear_all_crimes -> {
                crimeListViewModel.clearAllCrimes()
                true
            }
            else -> return super.onContextItemSelected(item)
        }
    }
}