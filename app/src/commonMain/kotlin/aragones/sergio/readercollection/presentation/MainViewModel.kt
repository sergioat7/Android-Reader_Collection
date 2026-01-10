/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 14/3/2025
 */

package aragones.sergio.readercollection.presentation

import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.domain.UserRepository

class MainViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Public properties
    val language: String
        get() = userRepository.language
    //endregion

    //region Public methods
    fun setLanguage(language: String) {
        userRepository.language = language
    }
    //endregion
}