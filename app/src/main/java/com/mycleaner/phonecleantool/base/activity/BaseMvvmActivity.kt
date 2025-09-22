package com.mycleaner.phonecleantool.base.activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.mycleaner.phonecleantool.adv.AdvActivity
import com.mycleaner.phonecleantool.base.viewmodel.BaseViewModel
import com.mycleaner.phonecleantool.utils.ActivityManagerUtils



import java.lang.reflect.ParameterizedType


abstract class BaseMvvmActivity<V: ViewBinding,VM: BaseViewModel>: AdvActivity(){
    lateinit var viewModel: VM
    lateinit var binding: V

    private fun initViewModel(){
        viewModel = createViewModel()
    }

    //初始化bing
    private fun initBinding(){
        var dbClass = genericTypeBinding()
        var method = dbClass.getMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null,layoutInflater) as V
        setContentView(binding.root)
    }

    fun genericTypeBinding(): Class<V>{
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<V>
    }

    protected fun createViewModel():VM{
       return ViewModelProvider.AndroidViewModelFactory(application).create(genericTypeViewModel())
    }


    fun genericTypeViewModel(): Class<VM> {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VM>
    }

    protected abstract fun init()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initBinding()
        initViewModel()
        init()
        ActivityManagerUtils.addActivity(this)//创建Activity入栈管理
        // 设置固定竖屏（兼容所有版本）
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }
    override fun onDestroy() {
        super.onDestroy()

        ActivityManagerUtils.removeActivity(this) //销毁Activity移出栈
    }



}