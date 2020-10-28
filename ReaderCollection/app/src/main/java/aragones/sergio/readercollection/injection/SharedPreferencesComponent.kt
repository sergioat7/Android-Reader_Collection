/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection

import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import dagger.Component

@Component(modules = [SharedPreferencesModule::class])
interface SharedPreferencesComponent {

    fun inject(landingActivity: LandingActivity)
    fun inject(loginViewModelFactory: LoginViewModelFactory)
}