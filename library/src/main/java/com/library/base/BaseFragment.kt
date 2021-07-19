package com.library.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.library.base.base.BaseViewModel
import com.library.base.base.ViewModelFactory
import com.library.base.event.Message
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.arch.QMUIFragment
import com.qmuiteam.qmui.arch.SwipeBackLayout
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VM : BaseViewModel> : QMUIFragment() {
    private var isFirst: Boolean = true
    protected lateinit var viewModel: VM
    protected lateinit var views: View

    override fun onCreateView(): View {
        createViewModel()
        lifecycle.addObserver(viewModel)
        views = LayoutInflater.from(activity).inflate(layoutId(), null)
        return views;
    }

    override fun onViewCreated(rootView: View) {
        super.onViewCreated(rootView)
        initView(views)
    }

    abstract fun layoutId(): Int
    open fun lazyLoadData() {}
    open fun initView(views: View) {}
    open fun handleEvent(msg: Message) {}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onVisible()
        registerDefUIChange()
    }


    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            lazyLoadData()
            isFirst = false
        }
    }
//    private var inputMethodManager: InputMethodManager? = null

//    open fun hideSoftInput(windowToken: IBinder?,isShow:Boolean){
//       /* if (inputMethodManager == null) {
//            inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//        }*/
//        if(isShow){
//            inputMethodManager?.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
//        }else{
//            inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)
//        }
//    }

    override fun backViewInitOffset(context: Context?, dragDirection: Int, moveEdge: Int): Int {
        if (moveEdge == SwipeBackLayout.EDGE_TOP || moveEdge == SwipeBackLayout.EDGE_BOTTOM) {
            return 0
        }
//        return super.backViewInitOffset(context, dragDirection, moveEdge)
        return QMUIDisplayHelper.dp2px(context, 100)
    }
    var  tipDialog:QMUITipDialog?=null
    var  runnable:Runnable?=null
    private fun registerDefUIChange() {
        viewModel.defUi.showDialog.observe(viewLifecycleOwner, Observer {
//            MToastUtils.show("show")
             runnable = Runnable {
                 if (tipDialog == null) {
                     tipDialog = QMUITipDialog.Builder(context)
                         .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                         .setTipWord("加载中")
                         .create()
                 }
                tipDialog?.show()
            }
            views.postDelayed(runnable,100);

        })
        viewModel.defUi.disDialog.observe(viewLifecycleOwner, Observer {
//            MToastUtils.show("diss")
            if(runnable!=null){
                views.removeCallbacks(runnable)
            }
            tipDialog?.dismiss()

        })
        viewModel.defUi.toastEvent.observe(viewLifecycleOwner, Observer {
            MToastUtils.show(it)
        })
        viewModel.defUi.msgEvent.observe(viewLifecycleOwner, Observer {
            handleEvent(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (tipDialog?.isShowing == true) {
            tipDialog?.dismiss()
        }
    }


    open fun isShareVM(): Boolean = false

    private fun createViewModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val tp = type.actualTypeArguments[0]
            val tClass = tp as? Class<VM> ?: BaseViewModel::class.java
            val viewModelStore = if (isShareVM()) requireActivity().viewModelStore else this.viewModelStore
            viewModel = ViewModelProvider(viewModelStore, ViewModelFactory()).get(tClass) as VM
        }
    }


}