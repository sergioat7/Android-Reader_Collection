/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection.components

import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import aragones.sergio.readercollection.viewmodelfactories.ProfileViewModelFactory
import aragones.sergio.readercollection.viewmodelfactories.RegisterViewModelFactory
import dagger.Component

@Component(modules = [SharedPreferencesModule::class])
interface SharedPreferencesComponent {

    fun inject(landingActivity: LandingActivity)
    fun inject(registerViewModelFactory: RegisterViewModelFactory)
    fun inject(profileViewModelFactory: ProfileViewModelFactory)
}