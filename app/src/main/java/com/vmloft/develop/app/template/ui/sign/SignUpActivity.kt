package com.vmloft.develop.app.template.ui.sign


import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod

import com.alibaba.android.arouter.facade.annotation.Route

import com.vmloft.develop.app.template.R
import com.vmloft.develop.app.template.databinding.ActivitySignUpBinding
import com.vmloft.develop.app.template.request.viewmodel.SignViewModel
import com.vmloft.develop.app.template.router.AppRouter
import com.vmloft.develop.library.base.BVMActivity
import com.vmloft.develop.library.base.BViewModel
import com.vmloft.develop.library.base.router.CRouter
import com.vmloft.develop.library.base.utils.errorBar
import com.vmloft.develop.library.tools.utils.VMReg

import org.koin.androidx.viewmodel.ext.android.getViewModel


/**
 * Create by lzan13 on 2020/6/19 17:10
 * 描述：注册界面
 */
@Route(path = AppRouter.appSignUp)
class SignUpActivity : BVMActivity<ActivitySignUpBinding, SignViewModel>() {

    private var mAccount: String = ""
    private var mPassword: String = ""

    override fun initVM(): SignViewModel = getViewModel()

    override fun initVB() = ActivitySignUpBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()
        setTopTitle(R.string.sign_up)

        // 监听输入框变化
        mBinding.signAccountET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mAccount = s.toString().trim()
                verifyInputBox()
            }
        })
        mBinding.signPasswordET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                mPassword = s.toString().trim()
                verifyInputBox()
            }
        })

        // 设置密码隐藏与显示
        mBinding.signPasswordIcon.setOnClickListener {
            if (mBinding.signPasswordIcon.isSelected) {
                // 隐藏密码
                mBinding.signPasswordET.transformationMethod = PasswordTransformationMethod.getInstance()
                mBinding.signPasswordIcon.isSelected = false
            } else {
                // 显示密码
                mBinding.signPasswordET.transformationMethod = HideReturnsTransformationMethod.getInstance()
                mBinding.signPasswordIcon.isSelected = true
            }
        }
        mBinding.signUserAgreementTV.setOnClickListener { CRouter.go(AppRouter.appSettingsAgreementPolicy, str0 = "agreement") }
        mBinding.signPrivatePolicyTV.setOnClickListener { CRouter.go(AppRouter.appSettingsAgreementPolicy, str0 = "policy") }
        mBinding.signSubmitBtn.setOnClickListener {
            if (mBinding.signPrivatePolicyCB.isChecked) {
                if (VMReg.isEmail(mAccount)) {
                    mViewModel.signUpByEmail(mAccount, mPassword)
                } else {
                    // TODO 只能使用邮箱注册
                    errorBar("只能使用邮箱注册账户")
                }
                // 屏蔽手机号注册
//                if (VMReg.isSimpleMobileNumber(mAccount)) {
//                    mViewModel.signUpByPhone(mAccount, mPassword)
//                }
            } else {
                errorBar(R.string.agreement_policy_hint)
            }
        }
    }

    override fun initData() {

    }

    override fun onModelRefresh(model: BViewModel.UIModel) {
        if (model.type == "signUpByPhone") {
            // 注册成功，跳转到主页
            CRouter.goMain()
            finish()
        }
        if (model.type == "signUpByEmail") {
            // 注册成功，跳转到主页
            CRouter.goMain()
            finish()
        }
    }

    /**
     * 校验输入框内容
     */
    private fun verifyInputBox() {
        mBinding.signSubmitBtn.isEnabled = VMReg.isEmail(mAccount) && mPassword.isNotEmpty()
    }

}
