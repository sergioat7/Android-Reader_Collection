/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.repositories.RegisterRepository
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
        private val registerRepository: RegisterRepository
): ViewModel() {
    // TODO: Implement the ViewModel
}