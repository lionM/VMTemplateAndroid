package com.vmloft.develop.app.template.ui.user

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

import com.google.android.material.tabs.TabLayoutMediator

import com.vmloft.develop.app.template.R
import com.vmloft.develop.app.template.databinding.ActivityUserInfoBinding
import com.vmloft.develop.app.template.im.IMManager
import com.vmloft.develop.app.template.request.bean.User
import com.vmloft.develop.app.template.request.viewmodel.UserViewModel
import com.vmloft.develop.app.template.router.AppRouter
import com.vmloft.develop.app.template.ui.post.PostFallsFragment
import com.vmloft.develop.app.template.ui.post.PostLikesFragment
import com.vmloft.develop.library.base.BVMActivity
import com.vmloft.develop.library.base.BViewModel
import com.vmloft.develop.library.base.router.CRouter
import com.vmloft.develop.library.base.widget.CommonDialog
import com.vmloft.develop.library.common.config.ConfigManager
import com.vmloft.develop.library.image.IMGLoader
import com.vmloft.develop.library.tools.utils.VMColor
import com.vmloft.develop.library.tools.utils.VMDimen
import com.vmloft.develop.library.tools.utils.VMStr
import org.koin.androidx.viewmodel.ext.android.getViewModel


/**
 * Create by lzan13 on 2021/01/20 22:56
 * 描述：用户信息界面
 */
@Route(path = AppRouter.appUserInfo)
class UserInfoActivity : BVMActivity<ActivityUserInfoBinding, UserViewModel>() {

    override var isDarkStatusBar: Boolean = false

    @Autowired(name = CRouter.paramsObj0)
    lateinit var user: User

    private val fragmentList = arrayListOf<Fragment>()
    private lateinit var publishFragment: PostFallsFragment
    private lateinit var likesFragment: PostLikesFragment

    private val titles = listOf("发布的", "喜欢的")

    override fun initVM(): UserViewModel = getViewModel()

    override fun initVB() = ActivityUserInfoBinding.inflate(layoutInflater)

    override fun initUI() {
        super.initUI()

        mBinding.tabTopSpaceView.layoutParams.height = VMDimen.dp2px(48) + VMDimen.statusBarHeight
        setTopTitleColor(R.color.app_title_display)

        mBinding.infoCoverIV.setOnClickListener { CRouter.goDisplaySingle(if (user.cover.isNullOrEmpty()) user.avatar else user.cover) }
        mBinding.infoAvatarIV.setOnClickListener { CRouter.goDisplaySingle(user.avatar) }
        mBinding.infoFansLL.setOnClickListener { }
        mBinding.infoFollowLL.setOnClickListener { }
        mBinding.infoLikeLL.setOnClickListener { }

        mBinding.infoFollowMeTV.setOnClickListener { follow() }
        mBinding.infoSendBtn.setOnClickListener { IMManager.goChat(user.id) }

    }

    override fun initData() {
        ARouter.getInstance().inject(this)

        mViewModel.userInfo(user.id)

        initFragmentList()
        initViewPager()

        bindInfo()
    }

    /**
     * 初始化 Fragment 集合
     */
    private fun initFragmentList() {
        publishFragment = PostFallsFragment.newInstance(user.id)
        likesFragment = PostLikesFragment.newInstance(user.id)

        fragmentList.run {
            add(publishFragment)
            add(likesFragment)
        }
    }

    /**
     * 初始化 ViewPager
     */
    private fun initViewPager() {
        mBinding.viewPager.offscreenPageLimit = titles.size - 1
        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragmentList[position]

            override fun getItemCount() = fragmentList.size
        }
        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    override fun onModelRefresh(model: BViewModel.UIModel) {
        if (model.type == "userInfo") {
            user = model.data as User
            bindInfo()
        } else if (model.type == "follow") {
            user.relation++
            setupFollowStatus()
        } else if (model.type == "cancelFollow") {
            user.relation--
            setupFollowStatus()
        }
    }

    /**
     * 绑定用户数据
     */
    private fun bindInfo() {
        if (user.cover.isNullOrEmpty()) {
            IMGLoader.loadCover(mBinding.infoCoverIV, user.avatar, isBlur = true, thumbExt = "!vt192")
        } else {
            IMGLoader.loadCover(mBinding.infoCoverIV, user.cover, isBlur = true, thumbExt = "!vt192")
        }

        IMGLoader.loadAvatar(mBinding.infoAvatarIV, user.avatar)
        // 身份
        if (ConfigManager.clientConfig.vipEntry && user.role.identity in 100..199) {
            mBinding.infoNameTV.setTextColor(VMColor.byRes(R.color.app_identity_vip))
            mBinding.infoIdentityIV.visibility = View.VISIBLE
        } else {
            mBinding.infoIdentityIV.visibility = View.GONE
        }
        // 性别
        when (user.gender) {
            1 -> mBinding.infoGenderIV.setImageResource(R.drawable.ic_gender_man)
            0 -> mBinding.infoGenderIV.setImageResource(R.drawable.ic_gender_woman)
            else -> mBinding.infoGenderIV.setImageResource(R.drawable.ic_gender_unknown)
        }

        val nickname = if (user.nickname.isNullOrEmpty()) VMStr.byRes(R.string.info_nickname_default) else user.nickname
        setTopTitle(nickname)
        mBinding.infoNameTV.text = nickname
        mBinding.infoAddressTV.text = if (user.address.isNullOrEmpty()) VMStr.byRes(R.string.info_address_default) else user.address
        mBinding.infoSignatureTV.text = if (user.signature.isNullOrEmpty()) VMStr.byRes(R.string.info_signature_default) else user.signature


        mBinding.infoLikeTV.text = user.likeCount.toString()
        mBinding.infoFollowTV.text = user.followCount.toString()
        mBinding.infoFansTV.text = user.fansCount.toString()

        setupFollowStatus()
    }

    private fun setupFollowStatus() {
        mBinding.infoFollowMeTV.text = when (user.relation) {
            0 -> VMStr.byRes(R.string.follow_status_0)
            1 -> VMStr.byRes(R.string.follow_status_1)
            2 -> VMStr.byRes(R.string.follow_status_2)
            else -> VMStr.byRes(R.string.follow)
        }

        // VMHeaderLayout 会缓存子 View 布局内容，默认情况下不会重新刷新布局内容，这里主动通知 VMHeaderLayout 子 View 内容有更新
        mBinding.infoHeaderLayout.updateLayout()
    }

    /**
     * 关注与取消
     */
    private fun follow() {
        if (user.relation != 0 && user.relation != 2) {
            mViewModel.follow(user.id)
        } else {
            showCancelFollowDialog()
        }
    }

    /**
     * 取消关注对话框
     */
    private fun showCancelFollowDialog() {
        mDialog = CommonDialog(this)
        (mDialog as CommonDialog).let { dialog ->
            dialog.setContent(R.string.follow_cancel_tips)
            dialog.setPositive{
                mViewModel.cancelFollow(user.id)
            }
            dialog.show()
        }

    }


}