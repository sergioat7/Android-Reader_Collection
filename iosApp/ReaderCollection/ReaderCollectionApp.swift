//
//  ReaderCollectionApp.swift
//  ReaderCollection
//
//  Created by Sergio Aragonés on 18/1/26.
//

import Firebase
import SwiftUI

@main
struct ReaderCollectionApp: App {
    
    init() {
        FirebaseApp.configure()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
