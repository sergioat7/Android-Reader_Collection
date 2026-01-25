/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2026
 */

package aragones.sergio.readercollection

expect fun getCurrentTarget(): Target

enum class Target {
    ANDROID,
    IOS,
}

fun isAndroid() = getCurrentTarget() == Target.ANDROID