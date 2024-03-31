/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.domain.base

import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseRepository {

    val disposables = CompositeDisposable()

    open fun onDestroy() {
        disposables.clear()
    }
}