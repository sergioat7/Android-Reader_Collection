/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.injection

import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import dagger.Component

@Component(modules = [
    SharedPreferencesModule::class,
    GoogleAPIClientModule::class
])
interface BooksComponent {

    fun inject(bookDetailViewModelFactory: BookDetailViewModelFactory)
}