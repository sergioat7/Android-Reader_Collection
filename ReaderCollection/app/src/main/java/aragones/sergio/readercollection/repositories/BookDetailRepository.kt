/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BookDetailRepository @Inject constructor(
    private val googleAPIClient: GoogleAPIClient
) {

    //MARK: - Public methods

    fun getBook(volumeId: String): Single<GoogleBookResponse> {
        return googleAPIClient.getGoogleBookObserver(volumeId)
    }
}