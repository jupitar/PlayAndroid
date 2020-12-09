package com.zj.play.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zj.core.Play
import com.zj.core.util.LiveDataBus
import com.zj.core.util.showToast
import com.zj.core.view.base.ActivityCollector
import com.zj.model.model.BaseModel
import com.zj.model.model.Login
import com.zj.network.repository.AccountRepository
import com.zj.play.R
import com.zj.play.home.LOGIN_REFRESH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 版权：Zhujiang 个人版权
 * @author zhujiang
 * 版本：1.5
 * 创建日期：2020/5/17
 * 描述：PlayAndroid
 *
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableLiveData<LoginState>()
    val state: LiveData<LoginState>
        get() = _state

    fun toLoginOrRegister(account: Account) {
        _state.postValue(Logging)
        viewModelScope.launch(Dispatchers.IO) {
            val loginModel: BaseModel<Login> = if (account.isLogin) {
                AccountRepository.getLogin(account.username, account.password)
            } else {
                AccountRepository.getRegister(
                    account.username,
                    account.password,
                    account.password
                )
            }

            if (loginModel.errorCode == 0) {
                val login = loginModel.data
                _state.postValue(LoginSuccess(login))
                Play.isLogin = true
                Play.setUserInfo(login.nickname, login.username)
                showToast(
                    if (account.isLogin) getApplication<Application>().getString(R.string.login_success) else getApplication<Application>().getString(
                        R.string.register_success
                    )
                )
                withContext(Dispatchers.Main) {
                    LiveDataBus.get().getChannel(LOGIN_REFRESH).setValue(true)
                }
            } else {
                showToast(loginModel.errorMsg)
                _state.postValue(LoginError)
            }
        }
    }

}

data class Account(val username: String, val password: String, val isLogin: Boolean)
sealed class LoginState
object Logging : LoginState()
data class LoginSuccess(val login: Login) : LoginState()
object LoginError : LoginState()