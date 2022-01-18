package com.FLAVOR.mvp.classes

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.FLAVOR.mvp.feeds.HomeFragment
import com.FLAVOR.mvp.retrofit_service

class MyFragmentFactory(private var server:retrofit_service):FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){

            HomeFragment::class.java.name ->HomeFragment(server)
            else -> super.instantiate(classLoader, className)
        }
    }
}