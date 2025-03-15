/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 14/3/2025
 */

package aragones.sergio.readercollection.presentation

import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Public properties
    val language: String
        get() = userRepository.language
    //endregion
}