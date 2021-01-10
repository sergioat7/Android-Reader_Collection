/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories.base

import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseRepository {

    val disposables = CompositeDisposable()

    open fun onDestroy() {
        disposables.clear()
    }
}