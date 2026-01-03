/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2026
 */

package aragones.sergio.readercollection.data.remote.model

sealed class CustomExceptions : Exception() {
    class ExistentUser : CustomExceptions()
}
