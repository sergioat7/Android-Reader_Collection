/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.data.remote.di

import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val BASE_GOOGLE_ENDPOINT = "www.googleapis.com/books/v1"
private const val API_KEY = "key"

val networkModule = module {
    includes(platformModule)
    single<HttpClient> {
        val httpClientEngine = get<HttpClientEngine>()
        HttpClient(httpClientEngine) {
            engine {
                httpClientEngine.config
            }
        }.config {
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true },
                    contentType = ContentType.Application.Json,
                )
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = BASE_GOOGLE_ENDPOINT
                    parameters.append(API_KEY, get(named("api_key")))
                }
            }
        }
    }
    factoryOf(::BooksRemoteDataSource)
    factoryOf(::UserRemoteDataSource)
}