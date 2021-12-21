/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/1/2021
 */

package aragones.sergio.readercollection.injection.components

import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule
import aragones.sergio.readercollection.viewmodelfactories.*
import dagger.Component

@Component(modules = [
    AppDatabaseModule::class,
    SharedPreferencesModule::class
])
interface AppComponent {

    fun inject(booksViewModelFactory: BooksViewModelFactory)
    fun inject(landingViewModelFactory: LandingViewModelFactory)
    fun inject(loginViewModelFactory: LoginViewModelFactory)
    fun inject(popupSyncAppViewModelFactory: PopupSyncAppViewModelFactory)
    fun inject(profileViewModelFactory: ProfileViewModelFactory)
    fun inject(registerViewModelFactory: RegisterViewModelFactory)
}