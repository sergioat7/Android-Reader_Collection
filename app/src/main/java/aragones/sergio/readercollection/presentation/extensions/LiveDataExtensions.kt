/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/2/2022
 */

package aragones.sergio.readercollection.presentation.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, S1, S2, S3, S4, S5, R> LiveData<T>.combineWith(
    source1: LiveData<S1>,
    source2: LiveData<S2>,
    source3: LiveData<S3>,
    source4: LiveData<S4>,
    source5: LiveData<S5>,
    block: (T?, S1?, S2?, S3?, S4?, S5?) -> R
): LiveData<R> {

    val result = MediatorLiveData<R>()
    arrayOf(this, source1, source2, source3, source4, source5).forEach {
        result.addSource(it) {
            result.value = block(
                this.value,
                source1.value,
                source2.value,
                source3.value,
                source4.value,
                source5.value
            )
        }
    }
    return result
}