/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/1/2021
 */

package aragones.sergio.readercollection.injection

import aragones.sergio.readercollection.viewmodelfactories.*
import dagger.Component

@Component(
    modules = [
        AppDatabaseModule::class,
        NetworkModule::class
    ]
)
interface AppComponent {

    fun inject(bookDetailViewModelFactory: BookDetailViewModelFactory)
    fun inject(booksViewModelFactory: BooksViewModelFactory)
    fun inject(landingViewModelFactory: LandingViewModelFactory)
    fun inject(loginViewModelFactory: LoginViewModelFactory)
    fun inject(popupSyncAppViewModelFactory: PopupSyncAppViewModelFactory)
    fun inject(profileViewModelFactory: ProfileViewModelFactory)
    fun inject(registerViewModelFactory: RegisterViewModelFactory)
    fun inject(searchViewModelFactory: SearchViewModelFactory)
}