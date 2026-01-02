/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2026
 */

package aragones.sergio.readercollection.domain.di

import aragones.sergio.readercollection.data.local.di.storageModule
import aragones.sergio.readercollection.data.remote.di.networkModule
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import com.aragones.sergio.di.databaseModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {
    includes(
        coroutineDispatcherModule,
        databaseModule,
        networkModule,
        storageModule,
    )
    factory<BooksRepository> {
        BooksRepository(
            booksLocalDataSource = get(),
            booksRemoteDataSource = get(),
            ioDispatcher = get(named(DispatchersName.IO)),
        )
    }
    factory<UserRepository> {
        UserRepository(
            userLocalDataSource = get(),
            userRemoteDataSource = get(),
            ioDispatcher = get(named(DispatchersName.IO)),
        )
    }
}