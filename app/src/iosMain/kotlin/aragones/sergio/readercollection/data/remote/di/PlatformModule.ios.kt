/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.data.remote.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single<String>(named("api_key")) { "AIzaSyBYhDAgpA2AJfznO81l1zRHeWreYP26zTk" }
    single<HttpClientEngine> {
        Darwin.create {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}