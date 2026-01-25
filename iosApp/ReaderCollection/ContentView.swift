//
//  ContentView.swift
//  ReaderCollection
//
//  Created by Sergio Aragonés on 18/1/26.
//

import App
import Lottie
import SwiftUI

struct ContentView: View {
    
    @State
    private var show = false
    
    var body: some View {
        
        ZStack {
            if show {
                ComposeView()
                    .ignoresSafeArea()
                    .transition(.opacity)
            } else {
                Animation(onFinish: {
                    show = true
                })
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background {
            Color("Secondary").ignoresSafeArea()
        }
        .animation(.default, value: show)
    }
}

struct Animation: View {
    
    var onFinish: () -> Void
    
    var body: some View {
        LottieView(animation: .named("landing_animation"))
            .playing()
            .configure({ lottieView in
                
                let primaryColor = UIColor(Color("Primary"))
                let secondaryColor = UIColor(Color("Secondary"))
                let roseBudColor = UIColor(Color("RoseBud"))
                
                ["01", "02", "03", "04"].forEach { path in
                    lottieView.setValueProvider(
                        ColorValueProvider(secondaryColor.lottieColorValue),
                        keypath: AnimationKeypath(keys: [path, "**", "Riempimento 1", "Color"])
                    )
                }
                
                lottieView.setValueProvider(
                    ColorValueProvider(roseBudColor.lottieColorValue),
                    keypath: AnimationKeypath(keys: ["obj_02", "**", "Riempimento 1", "Color"])
                )
                
                lottieView.setValueProvider(
                    ColorValueProvider(primaryColor.lottieColorValue),
                    keypath: AnimationKeypath(keys: ["ombra", "**", "Color"])
                )
                
                lottieView.setValueProvider(
                    ColorValueProvider(primaryColor.lottieColorValue),
                    keypath: AnimationKeypath(keys: ["**", "Traccia 1", "Color"])
                )
            })
            .animationDidFinish { completed in
                onFinish()
            }
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
