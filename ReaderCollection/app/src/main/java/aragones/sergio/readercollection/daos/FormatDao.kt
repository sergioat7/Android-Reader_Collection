/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.daos

import androidx.room.*
import aragones.sergio.readercollection.models.FormatResponse
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface FormatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFormatsObserver(formats: List<FormatResponse>): Completable

    @Delete
    fun deleteFormatsObserver(formats: List<FormatResponse>): Completable

    @Query("SELECT * FROM Format")
    fun getFormatsObserver(): Single<List<FormatResponse>>
}