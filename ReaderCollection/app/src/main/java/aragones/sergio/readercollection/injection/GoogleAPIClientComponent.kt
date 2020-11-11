/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2020
 */

package aragones.sergio.readercollection.injection

import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import aragones.sergio.readercollection.viewmodelfactories.SearchViewModelFactory
import dagger.Component

@Component(modules = [GoogleAPIClientModule::class])
interface GoogleAPIClientComponent {

    fun inject(searchViewModelFactory: SearchViewModelFactory)
    fun inject(bookDetailViewModelFactory: BookDetailViewModelFactory)
}