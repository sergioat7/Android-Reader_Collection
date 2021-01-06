/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.injection.components

import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import dagger.Component

@Component(modules = [
    SharedPreferencesModule::class,
    AppDatabaseModule::class
])
interface LoginComponent {

    fun inject(loginViewModelFactory: LoginViewModelFactory)
}