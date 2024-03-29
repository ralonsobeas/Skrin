package com.rod.skrin.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

import com.rod.skrin.R
import com.rod.skrin.adapters.ChatAdapter
import com.rod.skrin.extensions.toast
import com.rod.skrin.models.TotalMessagesEvent
import com.rod.skrin.utils.CircleTransform
import com.rod.skrin.utils.RxBus
import com.squareup.picasso.Picasso
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_info.view.*


class InfoFragment : Fragment() {

    private lateinit var _view : View

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private lateinit var chatDBRef: CollectionReference
    private lateinit var adapter: ChatAdapter

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()


    private var chatSubsctiption : ListenerRegistration? = null

    private lateinit var infoBusListener : Disposable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _view =  inflater.inflate(R.layout.fragment_info, container, false)

        setUpChatDB()
        setUpCurrentUser()
        setUpCurrentUserInfoUI()

        //subscribeToTotalMessagesFirebaseStyle()
        subscribeToTotalMessagesEventBusReactivaStyle()
        return _view
    }

    private fun setUpChatDB(){
        chatDBRef = store.collection("chat")

    }

    private fun setUpCurrentUser(){
        currentUser = mAuth.currentUser!!

    }

    private fun setUpCurrentUserInfoUI(){
        _view.textViewInfoEmail.text = currentUser.email
        _view.textViewInfoName.text = currentUser.displayName?.let{
            currentUser.displayName
        } ?: run{
            getString(R.string.info_no_name)
        }

        currentUser.photoUrl?.let {
            Picasso.get().load(currentUser.photoUrl).resize(300,300)
                .centerCrop().transform(CircleTransform()).into(_view.imageViewInfoAvatar)
        } ?: run {
            Picasso.get().load(R.drawable.ic_person).resize(300,300)
                .centerCrop().transform(CircleTransform()).into(_view.imageViewInfoAvatar)
        }
    }

    private fun subscribeToTotalMessagesFirebaseStyle() {
        chatSubsctiption = chatDBRef.addSnapshotListener(object : java.util.EventListener,
            EventListener<QuerySnapshot> {
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                exception?.let {
                    activity!!.toast("Exception!!")
                    return
                }

                snapshot?.let{
                    _view.textViewInfoTotalMessages.text = "${it.size()}"

                }

            }
        })
    }

    private fun subscribeToTotalMessagesEventBusReactivaStyle(){

        infoBusListener = RxBus.listen(TotalMessagesEvent::class.java).subscribe({
            _view.textViewInfoTotalMessages.text =  "${it.total}"
        })
    }

    override fun onDestroyView() {
        infoBusListener.dispose()
        chatSubsctiption?.remove()
        super.onDestroyView()
    }

}
