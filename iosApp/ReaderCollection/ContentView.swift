//
//  ContentView.swift
//  ReaderCollection
//
//  Created by Sergio Aragonés on 18/1/26.
//

import App
import SwiftUI

struct ContentView: View {
    var body: some View {
            ComposeView().ignoresSafeArea()
        }
}

#Preview {
    ContentView()
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        LandingViewControllerKt.LandingViewController(
            firebaseProvider: FirebaseProviderIos.instance
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
