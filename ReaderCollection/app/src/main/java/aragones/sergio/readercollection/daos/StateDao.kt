/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.daos

import androidx.room.*
import aragones.sergio.readercollection.models.StateResponse
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface StateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatesObserver(states: List<StateResponse>): Completable

    @Delete
    fun deleteStatesObserver(states: List<StateResponse>): Completable

    @Query("SELECT * FROM State")
    fun getStatesObserver(): Single<List<StateResponse>>
}