/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.injection.components

import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.GoogleAPIClientModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule
import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import dagger.Component

@Component(modules = [
    SharedPreferencesModule::class,
    GoogleAPIClientModule::class,
    AppDatabaseModule::class
])
interface BooksComponent {

    fun inject(bookDetailViewModelFactory: BookDetailViewModelFactory)
}