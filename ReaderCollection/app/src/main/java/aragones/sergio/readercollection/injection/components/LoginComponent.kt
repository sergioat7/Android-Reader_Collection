/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.injection.components

import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule
import aragones.sergio.readercollection.viewmodelfactories.*
import dagger.Component

@Component(modules = [
    SharedPreferencesModule::class,
    AppDatabaseModule::class
])
interface LoginComponent {

    fun inject(loginViewModelFactory: LoginViewModelFactory)
    fun inject(registerViewModelFactory: RegisterViewModelFactory)
    fun inject(booksViewModelFactory: BooksViewModelFactory)
    fun inject(profileViewModelFactory: ProfileViewModelFactory)
    fun inject(popupSyncAppViewModelFactory: PopupSyncAppViewModelFactory)
    fun inject(landingViewModelFactory: LandingViewModelFactory)
}