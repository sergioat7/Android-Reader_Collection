/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/1/2021
 */

package aragones.sergio.readercollection.injection

import aragones.sergio.readercollection.network.di.NetworkModule
import aragones.sergio.readercollection.database.di.DatabaseModule
import aragones.sergio.readercollection.data.source.di.DispatcherModule
import aragones.sergio.readercollection.ui.bookdetail.BookDetailViewModelFactory
import aragones.sergio.readercollection.ui.booklist.BookListViewModelFactory
import aragones.sergio.readercollection.ui.books.BooksViewModelFactory
import aragones.sergio.readercollection.ui.landing.LandingViewModelFactory
import aragones.sergio.readercollection.ui.login.LoginViewModelFactory
import aragones.sergio.readercollection.ui.modals.syncapp.PopupSyncAppViewModelFactory
import aragones.sergio.readercollection.ui.register.RegisterViewModelFactory
import aragones.sergio.readercollection.ui.search.SearchViewModelFactory
import aragones.sergio.readercollection.ui.settings.SettingsViewModelFactory
import aragones.sergio.readercollection.ui.statistics.StatisticsViewModelFactory
import dagger.Component

@Component(
    modules = [
        DatabaseModule::class,
        DispatcherModule::class,
        NetworkModule::class
    ]
)
interface AppComponent {

    fun inject(bookDetailViewModelFactory: BookDetailViewModelFactory)
    fun inject(bookListViewModelFactory: BookListViewModelFactory)
    fun inject(booksViewModelFactory: BooksViewModelFactory)
    fun inject(landingViewModelFactory: LandingViewModelFactory)
    fun inject(loginViewModelFactory: LoginViewModelFactory)
    fun inject(popupSyncAppViewModelFactory: PopupSyncAppViewModelFactory)
    fun inject(registerViewModelFactory: RegisterViewModelFactory)
    fun inject(searchViewModelFactory: SearchViewModelFactory)
    fun inject(settingsViewModelFactory: SettingsViewModelFactory)
    fun inject(statisticsViewModelFactory: StatisticsViewModelFactory)
}